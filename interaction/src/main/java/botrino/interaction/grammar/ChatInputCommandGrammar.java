/*
 * This file is part of the Botrino project and is licensed under the MIT license.
 *
 * Copyright (c) 2020 Alexandre Miranda
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package botrino.interaction.grammar;

import botrino.api.util.ConfigUtils;
import discord4j.common.util.Snowflake;
import discord4j.core.event.domain.interaction.ChatInputInteractionEvent;
import discord4j.core.object.command.ApplicationCommandInteractionOption;
import discord4j.core.object.command.ApplicationCommandInteractionOptionValue;
import discord4j.core.object.command.ApplicationCommandOption;
import discord4j.discordjson.json.ApplicationCommandOptionChoiceData;
import discord4j.discordjson.json.ApplicationCommandOptionData;
import reactor.core.publisher.Mono;
import reactor.util.annotation.Nullable;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Allows to define a grammar for a chat input command. It is done by specifying a value class which fields
 * correspond to the expected options. This class is then instantiated and populated with the actual values at runtime.
 */
public final class ChatInputCommandGrammar<T> {

    private final Class<T> valueClass;

    private ChatInputCommandGrammar(Class<T> valueClass) {
        this.valueClass = valueClass;
    }

    public static <T> ChatInputCommandGrammar<T> of(Class<T> valueClass) {
        return new ChatInputCommandGrammar<>(valueClass);
    }

    private static Optional<ApplicationCommandInteractionOption> getOptionByName(ChatInputInteractionEvent event,
                                                                                 String name) {
        return event.getOptions().stream()
                .filter(opt -> opt.getType() == ApplicationCommandOption.Type.SUB_COMMAND_GROUP)
                .findAny()
                .flatMap(gr -> gr.getOptions().stream()
                        .filter(opt -> opt.getType() == ApplicationCommandOption.Type.SUB_COMMAND)
                        .findAny()
                        .map(opt -> opt.getOption(name)))
                .or(() -> event.getOptions().stream()
                        .filter(opt -> opt.getType() == ApplicationCommandOption.Type.SUB_COMMAND)
                        .findAny()
                        .map(opt -> opt.getOption(name)))
                .orElseGet(() -> event.getOption(name));
    }

    /**
     * Resolves this grammar against the given {@link ChatInputInteractionEvent}. It will construct an instance of the
     * value class with the value of the options of the correct type.
     *
     * @param event the chat input interaction event
     * @return a {@link Mono} emitting the instance of the value class with the actual option values
     */
    public Mono<T> resolve(ChatInputInteractionEvent event) {
        return Mono.defer(() -> {
            final var instance = ConfigUtils.instantiate(valueClass);
            final var publishers = new ArrayList<Mono<?>>();
            for (final var field : valueClass.getDeclaredFields()) {
                final var optionAnnot = field.getAnnotation(Option.class);
                if (optionAnnot == null) {
                    continue;
                }
                publishers.add(Mono.justOrEmpty(getOptionByName(event, optionAnnot.name()))
                        .flatMap(option -> Mono.justOrEmpty(option.getValue())
                                .flatMap(value -> extractOptionValue(option.getType(), value,
                                        event.getInteraction().getGuildId().orElse(null))))
                        .doOnNext(object -> {
                            try {
                                field.setAccessible(true);
                                field.set(instance, object);
                            } catch (IllegalAccessException e) {
                                throw new RuntimeException(e);
                            }
                        })
                );
            }
            return Mono.when(publishers).thenReturn(instance);
        });
    }

    private Mono<?> extractOptionValue(ApplicationCommandOption.Type type,
                                       ApplicationCommandInteractionOptionValue option, @Nullable Snowflake guildId) {
        switch (type) {
            case STRING:
                return Mono.just(option.asString());
            case INTEGER:
                return Mono.just(option.asLong());
            case NUMBER:
                return Mono.just(option.asDouble());
            case BOOLEAN:
                return Mono.just(option.asBoolean());
            case USER:
                return guildId == null ? option.asUser() : option.getClient()
                        .getMemberById(guildId, option.asSnowflake());
            case CHANNEL:
                return option.asChannel();
            case ROLE:
                return option.asRole();
            case MENTIONABLE:
                return Mono.just(option.asSnowflake());
            default:
                throw new IllegalArgumentException("Unknown type");
        }
    }

    public List<ApplicationCommandOptionData> toOptions() {
        final var list = new ArrayList<ApplicationCommandOptionData>();
        for (final var field : valueClass.getDeclaredFields()) {
            final var optionAnnot = field.getAnnotation(Option.class);
            if (optionAnnot == null) {
                continue;
            }
            final var builder = ApplicationCommandOptionData.builder();
            builder.type(optionAnnot.type().getValue());
            builder.name(optionAnnot.name());
            builder.description(optionAnnot.description());
            builder.required(optionAnnot.required());
            if (optionAnnot.choices().length > 0) {
                builder.choices(Arrays.stream(optionAnnot.choices())
                        .map(choice -> ApplicationCommandOptionChoiceData.builder()
                                .name(choice.name())
                                .value(selectValue(field.getType(), choice))
                                .build())
                        .collect(Collectors.toList()));
            }
            list.add(builder.build());
        }
        return list;
    }

    private Object selectValue(Class<?> type, Choice choice) {
        if (type == String.class) {
            return choice.stringValue();
        }
        if (type == long.class || type == Long.class) {
            return choice.longValue();
        }
        if (type == double.class || type == Double.class) {
            return choice.doubleValue();
        }
        throw new IllegalArgumentException("@Choices annotation cannot be used on type " + type.getName());
    }

    @Target(ElementType.FIELD)
    @Retention(RetentionPolicy.RUNTIME)
    public @interface Option {
        ApplicationCommandOption.Type type();

        String name();

        String description();

        boolean required() default false;

        Choice[] choices() default {};
    }

    @Target(ElementType.FIELD)
    @Retention(RetentionPolicy.RUNTIME)
    public @interface Choice {
        String name();

        String stringValue() default "";

        long longValue() default 0L;

        double doubleValue() default 0d;
    }
}