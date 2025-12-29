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
import botrino.interaction.listener.ChatInputInteractionListener;
import discord4j.common.util.Snowflake;
import discord4j.core.event.domain.interaction.ChatInputInteractionEvent;
import discord4j.core.object.command.ApplicationCommandInteractionOption;
import discord4j.core.object.command.ApplicationCommandInteractionOptionValue;
import discord4j.core.object.command.ApplicationCommandOption;
import discord4j.core.object.entity.User;
import discord4j.core.object.entity.channel.Channel;
import discord4j.discordjson.json.ApplicationCommandOptionChoiceData;
import discord4j.discordjson.json.ApplicationCommandOptionData;
import discord4j.rest.http.client.ClientException;
import org.jspecify.annotations.Nullable;
import reactor.core.publisher.Mono;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

/**
 * Allows to define a grammar for a chat input command. It is done by specifying a value class which fields correspond
 * to the expected options. This class is then instantiated and populated with the actual values at runtime.
 */
public final class ChatInputCommandGrammar<T> {

    private final Class<T> valueClass;

    private ChatInputCommandGrammar(Class<T> valueClass) {
        this.valueClass = valueClass;
    }

    /**
     * Creates a new {@link ChatInputCommandGrammar} that will inject option values into an instance of the specified
     * class. The class must have a public no-arg constructor.
     *
     * @param valueClass the class where option values are going to be injected
     * @param <T>        the type of the class
     * @return a new {@link ChatInputCommandGrammar}
     */
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
     * value class with the value of the options injected into its fields.
     *
     * @param event the chat input interaction event
     * @return a {@link Mono} emitting the instance of the value class with the actual option values
     */
    public Mono<T> resolve(ChatInputInteractionEvent event) {
        return valueClass.isRecord() ? resolveRecordClass(event) : resolveValueClass(event);
    }

    private Mono<T> resolveValueClass(ChatInputInteractionEvent event) {
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

    private Mono<T> resolveRecordClass(ChatInputInteractionEvent event) {
        return Mono.defer(() -> {
            final var publishers = new ArrayList<Mono<?>>();
            for (final var component : valueClass.getRecordComponents()) {
                final var optionAnnot = component.getAnnotation(Option.class);
                if (optionAnnot == null) {
                    continue;
                }
                publishers.add(Mono.justOrEmpty(getOptionByName(event, optionAnnot.name()))
                        .flatMap(option -> Mono.justOrEmpty(option.getValue())
                                .flatMap(value -> extractOptionValue(option.getType(), value,
                                        event.getInteraction().getGuildId().orElse(null)))
                                .map(Optional::of))
                        .switchIfEmpty(Mono.just(Optional.empty())));
            }
            if (publishers.isEmpty()) {
                return Mono.just(ConfigUtils.instantiateRecord(valueClass.asSubclass(Record.class))).cast(valueClass);
            }
            return Mono.zip(publishers,
                    params -> ConfigUtils.instantiateRecord(valueClass.asSubclass(Record.class),
                            Arrays.stream(params).map(opt -> ((Optional<?>) opt).orElse(null)).toArray()))
                    .cast(valueClass);
        });
    }

    private Mono<?> extractOptionValue(ApplicationCommandOption.Type type,
                                       ApplicationCommandInteractionOptionValue option, @Nullable Snowflake guildId) {
        return switch (type) {
            case STRING -> Mono.just(option.asString());
            case INTEGER -> Mono.just(option.asLong());
            case NUMBER -> Mono.just(option.asDouble());
            case BOOLEAN -> Mono.just(option.asBoolean());
            case USER -> guildId == null ? option.asUser() : option.getClient()
                    .getMemberById(guildId, option.asSnowflake())
                    .cast(User.class)
                    .onErrorResume(ClientException.isStatusCode(404), e -> option.asUser());
            case CHANNEL -> option.asChannel();
            case ROLE -> option.asRole();
            case MENTIONABLE -> Mono.just(option.asSnowflake());
            case ATTACHMENT -> Mono.just(option.asAttachment());
            default -> throw new IllegalArgumentException("Unknown type");
        };
    }

    /**
     * Converts the option metadata contained in this grammar into a list of {@link ApplicationCommandOptionData} that
     * can be passed directly as a return value of {@link ChatInputInteractionListener#options()}.
     *
     * @return a list of {@link ApplicationCommandOptionData}
     */
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
            if (optionAnnot.channelTypes().length > 0) {
                builder.channelTypes(Arrays.stream(optionAnnot.channelTypes())
                        .map(Channel.Type::getValue)
                        .toList());
            }
            if (optionAnnot.choices().length > 0) {
                builder.choices(Arrays.stream(optionAnnot.choices())
                        .<ApplicationCommandOptionChoiceData>map(choice -> ApplicationCommandOptionChoiceData.builder()
                                .name(choice.name())
                                .value(selectValue(field.getType(), choice))
                                .build())
                        .toList());
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
        throw new IllegalArgumentException("@Choice annotation cannot be used on type " + type.getName());
    }

    /**
     * Defines metadata for a command option.
     */
    @Target({ElementType.FIELD, ElementType.RECORD_COMPONENT})
    @Retention(RetentionPolicy.RUNTIME)
    public @interface Option {
        /**
         * The type of option.
         *
         * @return the type
         */
        ApplicationCommandOption.Type type();

        /**
         * The name of the option.
         *
         * @return the name
         */
        String name();

        /**
         * The description of the option.
         *
         * @return the description
         */
        String description();

        /**
         * Whether the option is required, default <code>false</code>.
         *
         * @return a boolean
         */
        boolean required() default false;

        /**
         * The choices for this option, default empty.
         *
         * @return an array of choices
         */
        Choice[] choices() default {};

        /**
         * The channel types accepted, if this option is of type {@link ApplicationCommandOption.Type#CHANNEL}. Default
         * empty.
         *
         * @return an array of channel types
         */
        Channel.Type[] channelTypes() default {};
    }

    /**
     * Defines metadata for an option choice.
     */
    @Target(ElementType.FIELD)
    @Retention(RetentionPolicy.RUNTIME)
    public @interface Choice {

        /**
         * The name of the option choice.
         *
         * @return the name
         */
        String name();

        /**
         * The string value for the option choice. Only applicable if the option is of type
         * {@link ApplicationCommandOption.Type#STRING}.
         *
         * @return the string value
         */
        String stringValue() default "";

        /**
         * The long value for the option choice. Only applicable if the option is of type
         * {@link ApplicationCommandOption.Type#INTEGER}.
         *
         * @return the long value
         */
        long longValue() default 0L;

        /**
         * The double value for the option choice. Only applicable if the option is of type
         * {@link ApplicationCommandOption.Type#NUMBER}.
         *
         * @return the double value
         */
        double doubleValue() default 0d;
    }
}
