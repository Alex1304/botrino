/*
 * This file is part of the Botrino project and is licensed under the MIT license.
 *
 * Copyright (c) 2021 Alexandre Miranda
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
package botrino.interaction;

import botrino.api.config.ConfigContainer;
import botrino.api.config.object.I18nConfig;
import botrino.api.util.MatcherFunction;
import botrino.interaction.annotation.Acknowledge;
import botrino.interaction.annotation.ChatInputCommand;
import botrino.interaction.annotation.MessageCommand;
import botrino.interaction.annotation.UserCommand;
import botrino.interaction.config.InteractionConfig;
import botrino.interaction.context.*;
import botrino.interaction.cooldown.Cooldown;
import botrino.interaction.cooldown.CooldownException;
import botrino.interaction.listener.*;
import botrino.interaction.privilege.PrivilegeException;
import com.github.alex1304.rdi.finder.annotation.RdiFactory;
import com.github.alex1304.rdi.finder.annotation.RdiService;
import discord4j.common.util.Snowflake;
import discord4j.core.GatewayDiscordClient;
import discord4j.core.event.domain.interaction.*;
import discord4j.core.object.command.ApplicationCommand;
import discord4j.core.object.command.ApplicationCommandOption;
import discord4j.discordjson.json.ApplicationCommandData;
import discord4j.discordjson.json.ApplicationCommandOptionData;
import discord4j.discordjson.json.ApplicationCommandRequest;
import org.reactivestreams.Publisher;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.util.Logger;
import reactor.util.Loggers;
import reactor.util.annotation.Nullable;
import reactor.util.function.Tuple2;
import reactor.util.function.Tuples;

import java.time.Duration;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.stream.Collectors;

import static reactor.function.TupleUtils.function;

/**
 * The interaction service is in charge of registering commands, deploying them to Discord and listening to interactions
 * in order to propagate the events to listeners.
 */
@RdiService
public class InteractionService {

    private static final Logger LOGGER = Loggers.getLogger(InteractionService.class);

    private final InteractionConfig interactionConfig;
    private final GatewayDiscordClient gateway;
    private final Locale defaultLocale;

    private final Map<String, ApplicationCommandRequest> applicationCommandRequests = new HashMap<>();
    private final Map<ChatInputCommandKey, ChatInputInteractionListener> chatInputCommandListeners =
            new ConcurrentHashMap<>();
    private final Map<String, UserInteractionListener> userInteractionListeners = new ConcurrentHashMap<>();
    private final Map<String, MessageInteractionListener> messageInteractionListeners = new ConcurrentHashMap<>();
    private final Map<String, ComponentInteractionListener<?>> componentInteractions = new ConcurrentHashMap<>();
    private final Map<ContextKey, Map<String, ComponentInteractionListener<?>>> componentInteractionsSingleUse =
            new ConcurrentHashMap<>();

    private final Map<InteractionListener, Cooldown> cooldownPerCommand = new ConcurrentHashMap<>();
    private InteractionErrorHandler errorHandler;
    private InteractionEventProcessor eventProcessor;

    /**
     * @param configContainer -
     * @param gateway         -
     * @deprecated This is the constructor used by the Botrino framework. Not intended for direct consumption by library
     * users. Use {@link #builder(InteractionConfig, GatewayDiscordClient)} instead.
     */
    @Deprecated
    @RdiFactory
    public InteractionService(ConfigContainer configContainer, GatewayDiscordClient gateway) {
        this(configContainer.get(InteractionConfig.class), gateway,
                Locale.forLanguageTag(configContainer.get(I18nConfig.class).defaultLocale()), null, null);
    }

    private InteractionService(InteractionConfig interactionConfig, GatewayDiscordClient gateway,
                               Locale defaultLocale, @Nullable InteractionErrorHandler errorHandler,
                               @Nullable InteractionEventProcessor eventProcessor) {
        this.interactionConfig = interactionConfig;
        this.gateway = gateway;
        this.defaultLocale = defaultLocale;
        this.errorHandler = errorHandler;
        this.eventProcessor = eventProcessor;
    }

    /**
     * Initializes a new builder to create an {@link InteractionService} instance.
     *
     * @param config  the configuration for the service
     * @param gateway the gateway discord client
     * @return a new builder
     */
    public static Builder builder(InteractionConfig config, GatewayDiscordClient gateway) {
        return new Builder(config, gateway);
    }

    private static boolean hasCommandChanged(ApplicationCommandData oldCommand, ApplicationCommandRequest newCommand) {
        return !oldCommand.type().toOptional().orElse(1).equals(newCommand.type().toOptional().orElse(1))
                || !oldCommand.description().equals(newCommand.description().toOptional().orElse(""))
                || !oldCommand.defaultPermission().toOptional().orElse(true)
                .equals(newCommand.defaultPermission().toOptional().orElse(true)
                        || !oldCommand.options().toOptional().orElse(List.of())
                        .equals(newCommand.options().toOptional().orElse(List.of())));
    }

    private static <K, L extends InteractionListener> L findApplicationCommandListener(Map<K, L> listeners, K key) {
        final var listener = listeners.get(key);
        if (listener == null) {
            throw new AssertionError(key + " does not match any listener");
        }
        return listener;
    }

    void setErrorHandler(InteractionErrorHandler errorHandler) {
        LOGGER.debug("Using error handler {}", errorHandler);
        this.errorHandler = errorHandler;
    }

    void setEventProcessor(InteractionEventProcessor eventProcessor) {
        LOGGER.debug("Using event processor {}", eventProcessor);
        this.eventProcessor = eventProcessor;
    }

    /**
     * Registers a new chat input command. This variant is suited for commands that don't have subcommands and that
     * directly define a @{@link ChatInputCommand} annotation. To register chat input commands with
     * subcommands/subcommand groups, use the overload {@link #registerChatInputCommand(Object, Collection)} instead.
     *
     * @param listener the listener to register as a chat input command
     * @throws IllegalArgumentException if the given listener does not have a @{@link ChatInputCommand} annotation.
     */
    public void registerChatInputCommand(ChatInputInteractionListener listener) {
        registerChatInputCommand(listener, List.of());
    }

    /**
     * Registers a new chat input command. This variant is suited for commands that have subcommands or subcommand
     * groups, which define separate listeners for each. To register chat input commands without subcommands/subcommand
     * groups, it may be more straightforward to use the overload
     * {@link #registerChatInputCommand(ChatInputInteractionListener)}
     * instead.
     *
     * @param annotatedObject the object which class is annotated with @{@link ChatInputCommand}
     * @param listeners       if the command defines subcommands, this list is expected to contain the instances of the
     *                        listeners for each subcommand. It may contain at most one instance per concrete type.
     * @throws IllegalArgumentException if the given object does not have a @{@link ChatInputCommand} annotation
     * @throws IllegalStateException    if the given object doesn't have subcommands AND doesn't implement {@link
     *                                  ChatInputInteractionListener}. Also thrown if more than one instance with the
     *                                  same concrete type is given in the listeners list, or if an instance is missing
     *                                  from that list.
     */
    public void registerChatInputCommand(Object annotatedObject,
                                         Collection<? extends ChatInputInteractionListener> listeners) {
        final var builder = ApplicationCommandRequest.builder();
        final var annot = annotatedObject.getClass().getAnnotation(ChatInputCommand.class);
        if (annot == null) {
            throw new IllegalArgumentException("Missing @ChatInputCommand annotation");
        }
        builder.name(annot.name());
        builder.description(annot.description());
        builder.defaultPermission(annot.defaultPermission());
        builder.type(ApplicationCommand.Type.CHAT_INPUT.getValue());
        if (annot.subcommandGroups().length == 0 && annot.subcommands().length == 0) {
            if (!(annotatedObject instanceof ChatInputInteractionListener)) {
                throw new IllegalStateException(annotatedObject.getClass().getName() + " is annotated with " +
                        "@ChatInputCommand and does not declare subcommands, but doesn't implement " +
                        "ChatInputInteractionListener.");
            }
            final var listener = (ChatInputInteractionListener) annotatedObject;
            final var key = new ChatInputCommandKey(annot.name(), null, null);
            chatInputCommandListeners.put(key, listener);
            LOGGER.debug("Registered chat input command listener {}", key);
            builder.options(listener.options());
        } else {
            final var listenerMap = listeners.stream()
                    .collect(Collectors.toMap(Object::getClass, Function.identity()));
            for (var i = -1; i == -1 || i < annot.subcommandGroups().length; i++) {
                final var subcommandGroup = i == -1 ? null : annot.subcommandGroups()[i];
                final var subcommands = subcommandGroup == null ? annot.subcommands() : subcommandGroup.subcommands();
                final var subcommandsData = Arrays.stream(subcommands)
                        .map(subcommand -> {
                            final var listener = listenerMap.get(subcommand.listener());
                            final var key = new ChatInputCommandKey(
                                    annot.name(),
                                    subcommandGroup == null ? null : subcommandGroup.name(),
                                    subcommand.name());
                            if (listener == null) {
                                throw new IllegalStateException("No instance of " + subcommand.listener().getName() +
                                        " was provided for subcommand '" + key + "'");
                            }
                            chatInputCommandListeners.put(key, listener);
                            LOGGER.debug("Registered chat input command listener {}", key);
                            return (ApplicationCommandOptionData) ApplicationCommandOptionData.builder()
                                    .name(subcommand.name())
                                    .description(subcommand.description())
                                    .type(ApplicationCommandOption.Type.SUB_COMMAND.getValue())
                                    .options(listener.options())
                                    .build();
                        })
                        .collect(Collectors.toUnmodifiableList());
                if (subcommandGroup != null) {
                    builder.addOption(ApplicationCommandOptionData.builder()
                            .name(subcommandGroup.name())
                            .description(subcommandGroup.description())
                            .type(ApplicationCommandOption.Type.SUB_COMMAND_GROUP.getValue())
                            .options(subcommandsData)
                            .build());
                } else {
                    builder.addAllOptions(subcommandsData);
                }
            }
        }
        applicationCommandRequests.put(annot.name(), builder.build());
    }

    /**
     * Registers a new user context menu command.
     *
     * @param listener the listener to register. Must have a @{@link UserCommand} annotation.
     * @throws IllegalArgumentException if the given listener does not have a @{@link UserCommand} annotation.
     */
    public void registerUserCommand(UserInteractionListener listener) {
        Objects.requireNonNull(listener);
        final var annot = listener.getClass().getAnnotation(UserCommand.class);
        if (annot == null) {
            throw new IllegalArgumentException("Missing @UserCommand annotation");
        }
        userInteractionListeners.put(annot.value(), listener);
        applicationCommandRequests.put(annot.value(), ApplicationCommandRequest.builder()
                .name(annot.value())
                .type(ApplicationCommand.Type.USER.getValue())
                .defaultPermission(annot.defaultPermission())
                .build());
        LOGGER.debug("Registered user interaction listener {}", listener);
    }

    /**
     * Registers a new message context menu command.
     *
     * @param listener the listener to register. Must have a @{@link MessageCommand} annotation.
     * @throws IllegalArgumentException if the given listener does not have a @{@link MessageCommand} annotation.
     */
    public void registerMessageCommand(MessageInteractionListener listener) {
        Objects.requireNonNull(listener);
        final var annot = listener.getClass().getAnnotation(MessageCommand.class);
        if (annot == null) {
            throw new IllegalArgumentException("Missing @MessageCommand annotation");
        }
        messageInteractionListeners.put(annot.value(), listener);
        applicationCommandRequests.put(annot.value(), ApplicationCommandRequest.builder()
                .name(annot.value())
                .type(ApplicationCommand.Type.MESSAGE.getValue())
                .defaultPermission(annot.defaultPermission())
                .build());
        LOGGER.debug("Registered message interaction listener {}", listener);
    }

    /**
     * Registers a new component listener as a command.
     *
     * @param listener the listener to register
     */
    public void registerComponentCommand(ComponentInteractionListener<?> listener) {
        Objects.requireNonNull(listener);
        componentInteractions.put(listener.customId(), listener);
        LOGGER.debug("Registered component interaction listener {}", listener);
    }

    /**
     * Registers a new component listener that is going to be executed only once. It can only be executed by the same
     * user in the same channel as a previous interaction which context is specified. This is generally called
     * indirectly via {@link InteractionContext#awaitComponentInteraction(ComponentInteractionListener)}.
     *
     * @param listener      the listener to register
     * @param parentContext the context for the parent interaction
     */
    public void registerSingleUseComponentListener(ComponentInteractionListener<?> listener,
                                                   InteractionContext parentContext) {
        Objects.requireNonNull(listener);
        Objects.requireNonNull(parentContext);
        componentInteractionsSingleUse.computeIfAbsent(ContextKey.from(parentContext), k -> new ConcurrentHashMap<>())
                .put(listener.customId(), listener);
        LOGGER.debug("Registered single use component interaction listener {}", listener);
    }

    /**
     * Gets the "await component timeout" value from the underlying {@link InteractionConfig}.
     *
     * @return a {@link Duration}
     */
    public Duration getAwaitComponentTimeout() {
        return Duration.ofSeconds(interactionConfig.awaitComponentTimeoutSeconds());
    }

    /**
     * Runs the service. Upon subscription, it will start by deploying the commands to Discord, either globally or in a
     * specific guild according to the config, then it will start listening to the interaction events coming from
     * gateway. It never completes until the underlying {@link GatewayDiscordClient#getEventDispatcher()} terminates.
     *
     * @return a Mono that completes when the event dispatcher terminates.
     */
    public Mono<Void> run() {
        return uploadCommands().then(gateway
                .on(InteractionCreateEvent.class, event -> event.getInteraction().getChannel()
                        .flatMap(channel -> eventProcessor.filter(event)
                                .filter(Boolean::booleanValue)
                                .flatMap(__ -> eventProcessor.computeLocale(event).defaultIfEmpty(defaultLocale))
                                .map(locale -> MatcherFunction.<CommandRunner>create()
                                        .matchType(ChatInputInteractionEvent.class, ev -> new ChatInputCommandRunner(
                                                new ChatInputInteractionContext(this, locale, ev, channel)))
                                        .matchType(UserInteractionEvent.class, ev -> new UserCommandRunner(
                                                new UserInteractionContext(this, locale, ev, channel)))
                                        .matchType(MessageInteractionEvent.class, ev -> new MessageCommandRunner(
                                                new MessageInteractionContext(this, locale, ev, channel)))
                                        .matchType(ButtonInteractionEvent.class, ev -> new ComponentCommandRunner(
                                                new ButtonInteractionContext(this, locale, ev, channel)))
                                        .matchType(SelectMenuInteractionEvent.class, ev -> new ComponentCommandRunner(
                                                new SelectMenuInteractionContext(this, locale, ev, channel)))
                                        .apply(event))
                                .flatMap(Mono::justOrEmpty)
                                .flatMap(runner -> runner.run()
                                        .onErrorResume(t -> Mono.from(
                                                executeErrorHandler(t, errorHandler, runner.ctx())).then())
                                        .onErrorResume(t -> Mono.fromRunnable(
                                                () -> LOGGER.error("An unhandled error occurred when executing an " +
                                                        "interaction. Context: " + runner.ctx(), t))))))
                .then(Mono.fromRunnable(() -> LOGGER.info("Command listener completed"))));
    }

    private Mono<Tuple2<ComponentInteractionListener<?>, Boolean>>
    findComponentListener(ContextKey key, ComponentInteractionEvent event) {
        return Mono.justOrEmpty(componentInteractionsSingleUse.getOrDefault(key, new ConcurrentHashMap<>())
                        .remove(event.getCustomId()))
                .doOnNext(listener -> {
                    LOGGER.debug("Consumed single use component interaction listener {}", listener);
                    componentInteractionsSingleUse.computeIfPresent(key, (k, v) -> v.isEmpty() ? null : v);
                })
                .<Tuple2<ComponentInteractionListener<?>, Boolean>>map(listener -> Tuples.of(listener, true))
                .switchIfEmpty(Mono.defer(() ->
                        Mono.justOrEmpty(Tuples.of(componentInteractions.get(event.getCustomId()), false))));
    }

    private Mono<Void> preCheck(InteractionContext ctx, InteractionListener listener) {
        return ackIfConfigured(listener, ctx.event())
                .then(Mono.defer(() -> listener.privilege().checkGranted(ctx)))
                .then(Mono.fromRunnable(() -> cooldownPerCommand.computeIfAbsent(listener,
                                InteractionListener::cooldown)
                        .fire(ctx.user().getId().asLong())));
    }

    private Mono<Void> ackIfConfigured(InteractionListener listener, InteractionCreateEvent event) {
        return Mono.defer(() -> {
            final var annot = listener.getClass().getAnnotation(Acknowledge.class);
            final var ackMode = annot != null && annot.value() != Acknowledge.Mode.DEFAULT ?
                    annot.value() : interactionConfig.defaultACKModeEnum();
            if (ackMode != Acknowledge.Mode.NONE) {
                final var ephemeral = ackMode == Acknowledge.Mode.DEFER_EPHEMERAL;
                if (event instanceof ComponentInteractionEvent) {
                    return ((ComponentInteractionEvent) event).deferEdit().withEphemeral(ephemeral);
                }
                return event.deferReply().withEphemeral(ephemeral);
            }
            return Mono.empty();
        });
    }

    private Publisher<?> executeErrorHandler(Throwable t, InteractionErrorHandler errorHandler,
                                             InteractionContext ctx) {
        return MatcherFunction.<Publisher<?>>create()
                .matchType(InteractionFailedException.class, e -> errorHandler.handleInteractionFailed(e, ctx))
                .matchType(PrivilegeException.class, e -> errorHandler.handlePrivilege(e, ctx))
                .matchType(CooldownException.class, e -> errorHandler.handleCooldown(e, ctx))
                .apply(t)
                .orElseGet(() -> errorHandler.handleDefault(t, ctx));
    }

    private Mono<Void> uploadCommands() {
        return gateway.rest().getApplicationId()
                .flatMapMany(applicationId -> getExistingCommands(applicationId)
                        .collectMap(ApplicationCommandData::name)
                        .flatMap(existingCommands -> {
                            final var toAdd = applicationCommandRequests.values().stream()
                                    .filter(r -> !existingCommands.containsKey(r.name()))
                                    .collect(Collectors.toUnmodifiableList());
                            final var toRemove = existingCommands.values().stream()
                                    .filter(r -> !applicationCommandRequests.containsKey(r.name()))
                                    .map(r -> Snowflake.asLong(r.id()))
                                    .collect(Collectors.toUnmodifiableList());
                            final var toUpdate = existingCommands.values().stream()
                                    .filter(r -> applicationCommandRequests.containsKey(r.name())
                                            && hasCommandChanged(r, applicationCommandRequests.get(r.name())))
                                    .map(r -> Tuples.of(Snowflake.asLong(r.id()),
                                            applicationCommandRequests.get(r.name())))
                                    .collect(Collectors.toUnmodifiableList());
                            final var publishers = new ArrayList<Publisher<?>>();
                            if (!toAdd.isEmpty()) {
                                publishers.add(Flux.fromIterable(toAdd)
                                        .flatMap(request -> createCommand(applicationId, request)));
                            }
                            if (!toRemove.isEmpty()) {
                                publishers.add(Flux.fromIterable(toRemove)
                                        .flatMap(commandId -> removeCommand(applicationId, commandId)));
                            }
                            if (!toUpdate.isEmpty()) {
                                publishers.add(Flux.fromIterable(toUpdate)
                                        .flatMap(function((commandId, request) -> editCommand(applicationId,
                                                commandId, request))));
                            }
                            return Mono.when(publishers);
                        }))
                .then();
    }

    private Flux<ApplicationCommandData> getExistingCommands(long applicationId) {
        return interactionConfig.applicationCommandsGuildId()
                .map(guildId -> gateway.rest().getApplicationService()
                        .getGuildApplicationCommands(applicationId, guildId))
                .orElseGet(() -> gateway.rest().getApplicationService()
                        .getGlobalApplicationCommands(applicationId));
    }

    private Mono<ApplicationCommandData> createCommand(long applicationId, ApplicationCommandRequest request) {
        return interactionConfig.applicationCommandsGuildId()
                .map(guildId -> gateway.rest().getApplicationService()
                        .createGuildApplicationCommand(applicationId, guildId, request))
                .orElseGet(() -> gateway.rest().getApplicationService()
                        .createGlobalApplicationCommand(applicationId, request));
    }

    private Mono<Void> removeCommand(long applicationId, long commandId) {
        return interactionConfig.applicationCommandsGuildId()
                .map(guildId -> gateway.rest().getApplicationService()
                        .deleteGuildApplicationCommand(applicationId, guildId, commandId))
                .orElseGet(() -> gateway.rest().getApplicationService()
                        .deleteGlobalApplicationCommand(applicationId, commandId));
    }

    private Mono<ApplicationCommandData> editCommand(long applicationId, long commandId,
                                                     ApplicationCommandRequest request) {
        return interactionConfig.applicationCommandsGuildId()
                .map(guildId -> gateway.rest().getApplicationService()
                        .modifyGuildApplicationCommand(applicationId, guildId, commandId, request))
                .orElseGet(() -> gateway.rest().getApplicationService()
                        .modifyGlobalApplicationCommand(applicationId, commandId, request));
    }

    private interface CommandRunner {

        Mono<Void> run();

        InteractionContext ctx();
    }

    private final static class ChatInputCommandKey {

        private final String name, subcommandGroup, subcommand;

        private ChatInputCommandKey(String name, @Nullable String subcommandGroup, @Nullable String subcommand) {
            this.name = name;
            this.subcommandGroup = subcommandGroup;
            this.subcommand = subcommand;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            ChatInputCommandKey that = (ChatInputCommandKey) o;
            return name.equals(that.name) && Objects.equals(subcommandGroup, that.subcommandGroup) &&
                    Objects.equals(subcommand, that.subcommand);
        }

        @Override
        public int hashCode() {
            return Objects.hash(name, subcommandGroup, subcommand);
        }

        @Override
        public String toString() {
            return '/' + name + (subcommandGroup != null ? ' ' + subcommandGroup : "") +
                    (subcommand != null ? ' ' + subcommand : "");
        }
    }

    private final static class ContextKey {

        private final long channelId;
        private final long userId;

        private ContextKey(long channelId, long userId) {
            this.channelId = channelId;
            this.userId = userId;
        }

        private static ContextKey from(InteractionContext ctx) {
            return new ContextKey(ctx.channel().getId().asLong(), ctx.user().getId().asLong());
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            ContextKey that = (ContextKey) o;
            return channelId == that.channelId && userId == that.userId;
        }

        @Override
        public int hashCode() {
            return Objects.hash(channelId, userId);
        }
    }

    public static final class Builder {

        private final InteractionConfig config;
        private final GatewayDiscordClient gateway;
        private Locale defaultLocale;
        private InteractionErrorHandler errorHandler;
        private InteractionEventProcessor eventProcessor;

        private Builder(InteractionConfig config, GatewayDiscordClient gateway) {
            this.config = config;
            this.gateway = gateway;
        }

        /**
         * Sets the default locale for interaction contexts.
         *
         * @param defaultLocale the default locale
         * @return this builder
         */
        public Builder setDefaultLocale(@Nullable Locale defaultLocale) {
            this.defaultLocale = defaultLocale;
            return this;
        }

        /**
         * Sets the error handler to apply in order to handle errors from the execution of listeners.
         *
         * @param errorHandler the error handler
         * @return this builder
         */
        public Builder setErrorHandler(@Nullable InteractionErrorHandler errorHandler) {
            this.errorHandler = errorHandler;
            return this;
        }

        /**
         * Sets the event processor to apply in order to filter events or adapt the locale according to the context.
         *
         * @param eventProcessor the event processor
         * @return this builder
         */
        public Builder setEventProcessor(@Nullable InteractionEventProcessor eventProcessor) {
            this.eventProcessor = eventProcessor;
            return this;
        }

        /**
         * Builds a new {@link InteractionService} based on the context of this builder.
         *
         * @return a new {@link InteractionService}
         */
        public InteractionService build() {
            final var defaultLocale = Objects.requireNonNullElse(this.defaultLocale, Locale.getDefault());
            final var errorHandler = Objects.requireNonNullElse(this.errorHandler, InteractionErrorHandler.NO_OP);
            final var eventProcessor = Objects.requireNonNullElse(this.eventProcessor, InteractionEventProcessor.NO_OP);
            return new InteractionService(config, gateway, defaultLocale, errorHandler, eventProcessor);
        }
    }

    private final class ChatInputCommandRunner implements CommandRunner {

        private final ChatInputInteractionContext ctx;

        public ChatInputCommandRunner(ChatInputInteractionContext ctx) {
            this.ctx = ctx;
        }

        @Override
        public Mono<Void> run() {
            final var name = ctx.event().getCommandName();
            final var key = ctx.event().getOptions().stream()
                    .filter(opt -> opt.getType() == ApplicationCommandOption.Type.SUB_COMMAND_GROUP)
                    .findAny()
                    .flatMap(gr -> gr.getOptions().stream()
                            .filter(opt -> opt.getType() == ApplicationCommandOption.Type.SUB_COMMAND)
                            .findAny()
                            .map(opt -> new ChatInputCommandKey(name, gr.getName(), opt.getName())))
                    .or(() -> ctx.event().getOptions().stream()
                            .filter(opt -> opt.getType() == ApplicationCommandOption.Type.SUB_COMMAND)
                            .findAny()
                            .map(opt -> new ChatInputCommandKey(name, null, opt.getName())))
                    .orElseGet(() -> new ChatInputCommandKey(name, null, null));
            final var listener = findApplicationCommandListener(chatInputCommandListeners, key);
            return preCheck(ctx, listener).then(Mono.defer(() -> Mono.from(listener.run(ctx)).then()));
        }

        @Override
        public InteractionContext ctx() {
            return ctx;
        }
    }

    private final class UserCommandRunner implements CommandRunner {

        private final UserInteractionContext ctx;

        public UserCommandRunner(UserInteractionContext ctx) {
            this.ctx = ctx;
        }

        @Override
        public Mono<Void> run() {
            final var listener = findApplicationCommandListener(userInteractionListeners, ctx.event().getCommandName());
            return preCheck(ctx, listener).then(Mono.defer(() -> Mono.from(listener.run(ctx)).then()));
        }

        @Override
        public InteractionContext ctx() {
            return ctx;
        }
    }

    private final class MessageCommandRunner implements CommandRunner {

        private final MessageInteractionContext ctx;

        private MessageCommandRunner(MessageInteractionContext ctx) {
            this.ctx = ctx;
        }

        @Override
        public Mono<Void> run() {
            final var listener = findApplicationCommandListener(messageInteractionListeners,
                    ctx.event().getCommandName());
            return preCheck(ctx, listener).then(Mono.defer(() -> Mono.from(listener.run(ctx)).then()));
        }

        @Override
        public InteractionContext ctx() {
            return ctx;
        }
    }

    private final class ComponentCommandRunner implements CommandRunner {

        private final ComponentInteractionContext ctx;

        private ComponentCommandRunner(ComponentInteractionContext ctx) {
            this.ctx = ctx;
        }

        @Override
        public Mono<Void> run() {
            final var key = ContextKey.from(ctx);
            return findComponentListener(key, ctx.event())
                    .flatMap(function((listener, isSingleUse) -> preCheck(ctx, listener)
                            .then(Mono.defer(() -> Mono.from(listener.run(ctx)).then()))
                            .onErrorResume(e -> isSingleUse ?
                                    Mono.fromRunnable(
                                            () -> LOGGER.warn("Suppressed error in single use listener", e)) :
                                    Mono.error(e))));
        }

        @Override
        public InteractionContext ctx() {
            return ctx;
        }
    }
}
