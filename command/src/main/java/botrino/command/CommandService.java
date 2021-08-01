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
package botrino.command;

import botrino.api.config.ConfigContainer;
import botrino.api.config.object.I18nConfig;
import botrino.api.util.MatcherFunction;
import botrino.command.config.CommandConfig;
import botrino.command.context.*;
import botrino.command.cooldown.Cooldown;
import botrino.command.cooldown.CooldownException;
import botrino.command.privilege.PrivilegeException;
import com.github.alex1304.rdi.finder.annotation.RdiFactory;
import com.github.alex1304.rdi.finder.annotation.RdiService;
import discord4j.core.GatewayDiscordClient;
import discord4j.core.event.ReactiveEventAdapter;
import discord4j.core.event.domain.Event;
import discord4j.core.event.domain.interaction.*;
import discord4j.core.event.domain.message.MessageCreateEvent;
import org.reactivestreams.Publisher;
import reactor.core.publisher.Mono;
import reactor.util.Logger;
import reactor.util.Loggers;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import static reactor.function.TupleUtils.function;

@RdiService
public class CommandService {

    private static final Logger LOGGER = Loggers.getLogger(CommandService.class);

    private final CommandConfig commandConfig;
    private final GatewayDiscordClient gateway;
    private final Locale defaultLocale;

    private final MessageCommandTree messageCommands = new MessageCommandTree();
    private final Map<String, SlashCommand> slashCommands = new ConcurrentHashMap<>();
    private final Map<String, ButtonCommand> buttonCommands = new ConcurrentHashMap<>();
    private final Map<ContextKey, Map<String, ButtonCommand>> buttonCommandInteractions = new ConcurrentHashMap<>();
    private final Map<String, SelectMenuCommand> selectMenuCommands = new ConcurrentHashMap<>();
    private final Map<ContextKey, Map<String, SelectMenuCommand>> selectMenuCommandInteractions =
            new ConcurrentHashMap<>();

    private final Map<Command, Cooldown> cooldownPerCommand = new ConcurrentHashMap<>();
    private CommandErrorHandler errorHandler;
    private CommandEventProcessor eventProcessor;

    @RdiFactory
    public CommandService(ConfigContainer configContainer, GatewayDiscordClient gateway) {
        this.commandConfig = configContainer.get(CommandConfig.class);
        this.gateway = gateway;
        this.defaultLocale = Locale.forLanguageTag(configContainer.get(I18nConfig.class).defaultLocale());
    }

    /**
     * Get the message command instance for the given top level alias and subcommand aliases.
     *
     * <p>
     * For example, if a top-level command {@code foo} defines a subcommand {@code bar}, which itself defines a
     * subcommand {@code baz}:
     * <ul>
     *     <li>{@code getCommandAt("foo")} will return the {@code foo} command</li>
     *     <li>{@code getCommandAt("foo", "bar")} will list the {@code bar} command</li>
     *     <li>{@code getCommandAt("foo", "bar", "baz")} will list the {@code baz} command</li>
     * </ul>
     *
     * @param topLevelAlias     the alias of the top level command to look for
     * @param subcommandAliases the sequence of subcommand aliases to locate the command instance of a specific
     *                          subcommand
     * @return the command instance, if found
     */
    public Optional<MessageCommand> getCommandAt(String topLevelAlias, String... subcommandAliases) {
        Objects.requireNonNull(topLevelAlias);
        Objects.requireNonNull(subcommandAliases);
        return messageCommands.getCommandAt(topLevelAlias, subcommandAliases);
    }

    /**
     * Lists the message commands at the given path. A path is defined as a sequence of aliases identifying the command
     * for which subcommands should be listed. An empty path is equivalent to listing all commands at top level.
     * <p>
     * For example, if a top-level command {@code foo} defines a subcommand {@code bar}, which itself defines a
     * subcommand {@code baz}:
     * <ul>
     *     <li>{@code listCommands()} will list the {@code foo} command</li>
     *     <li>{@code listCommands("foo")} will list the {@code bar} command</li>
     *     <li>{@code listCommands("foo", "bar")} will list the {@code baz} command</li>
     * </ul>
     *
     * @param path a sequence of aliases identifying the message command for which subcommands should be listed, empty
     *             for listing top-level commands
     * @return a set of commands corresponding to the list result
     * @throws InvalidSyntaxException if {@code path} refers to a non-existing subcommand
     */
    public Set<MessageCommand> listMessageCommands(String... path) {
        Objects.requireNonNull(path);
        return messageCommands.listCommands(path);
    }

    void setErrorHandler(CommandErrorHandler errorHandler) {
        LOGGER.debug("Using error handler {}", errorHandler);
        this.errorHandler = errorHandler;
    }

    void setEventProcessor(CommandEventProcessor eventProcessor) {
        LOGGER.debug("Using event processor {}", eventProcessor);
        this.eventProcessor = eventProcessor;
    }

    public void register(MessageCommand command) {
        Objects.requireNonNull(command);
        messageCommands.addCommand(command);
        LOGGER.debug("Registered message command {}", command);
    }

    public void register(SlashCommand command) {
        Objects.requireNonNull(command);
        slashCommands.put(command.data().name(), command);
        LOGGER.debug("Registered slash command {}", command);
    }

    public void register(ButtonCommand command) {
        Objects.requireNonNull(command);
        buttonCommands.put(command.customId(), command);
        LOGGER.debug("Registered button command {}", command);
    }

    public void register(SelectMenuCommand command) {
        Objects.requireNonNull(command);
        selectMenuCommands.put(command.customId(), command);
        LOGGER.debug("Registered select menu command {}", command);
    }

    public void registerAsInteraction(ButtonCommand command, CommandContext parentContext) {
        Objects.requireNonNull(command);
        Objects.requireNonNull(parentContext);
        buttonCommandInteractions.computeIfAbsent(ContextKey.from(parentContext), k -> new ConcurrentHashMap<>())
                .put(command.customId(), command);
        LOGGER.debug("Registered button command as interaction {}", command);
    }

    public void registerAsInteraction(SelectMenuCommand command, CommandContext parentContext) {
        Objects.requireNonNull(command);
        Objects.requireNonNull(parentContext);
        selectMenuCommandInteractions.computeIfAbsent(ContextKey.from(parentContext), k -> new ConcurrentHashMap<>())
                .put(command.customId(), command);
        LOGGER.debug("Registered select menu command as interaction {}", command);
    }

    Mono<Void> handleCommands() {
        final var uploadSlashCommands = gateway.rest().getApplicationId()
                .flatMap(id -> Mono.justOrEmpty(commandConfig.slashCommandsGuildId())
                        .flatMap(guildId -> gateway.rest().getApplicationService()
                                .bulkOverwriteGuildApplicationCommand(id, guildId, slashCommands.values().stream()
                                        .map(SlashCommand::data)
                                        .collect(Collectors.toList()))
                                .then()
                                .thenReturn(guildId))
                        .switchIfEmpty(gateway.rest().getApplicationService()
                                .bulkOverwriteGlobalApplicationCommand(id, slashCommands.values().stream()
                                        .map(SlashCommand::data)
                                        .collect(Collectors.toList()))
                                .then()
                                .thenReturn(0L)));
        return uploadSlashCommands.then(gateway.on(new ReactiveEventAdapter() {
            @Override
            public Publisher<?> onMessageCreate(MessageCreateEvent event) {
                final var user = event.getMessage().getAuthor().orElse(null);
                if (user == null || user.isBot()) {
                    return Mono.empty();
                }
                return event.getMessage().getChannel().flatMap(channel -> eventProcessor.filter(event, channel, user)
                        .filter(Boolean::booleanValue)
                        .flatMap(__ -> Mono.zip(
                                eventProcessor.prefixForMessageEvent(event)
                                        .defaultIfEmpty(commandConfig.prefix()),
                                eventProcessor.localeForEvent(event, channel, user)
                                        .defaultIfEmpty(defaultLocale)))
                        .flatMap(function((prefix, locale) -> {
                            var botId = event.getClient().getSelfId().asLong();
                            var prefixes = Set.of("<@" + botId + ">", "<@!" + botId + ">", prefix);
                            var messageContent = event.getMessage().getContent();
                            // Extracting prefix
                            String prefixUsed = null;
                            for (var p : prefixes) {
                                if (messageContent.toLowerCase().startsWith(p.toLowerCase())) {
                                    messageContent = messageContent.substring(p.length());
                                    prefixUsed = p;
                                    break;
                                }
                            }
                            if (prefixUsed == null) {
                                return Mono.empty();
                            }
                            var input = TokenizedInput.tokenize(messageContent);
                            if (input.getArguments().isEmpty()) {
                                return Mono.empty();
                            }
                            var initialArgs = input.getArguments();
                            var mutableArgs = input.getMutableArgs();
                            input.setTrigger(initialArgs.subList(0, initialArgs.size() - mutableArgs.size()));
                            var ctx = new MessageCommandContext(CommandService.this, locale, channel, event,
                                    prefixUsed, input);
                            final var cmd = messageCommands.getCommandAt(mutableArgs);
                            return Mono.justOrEmpty(cmd)
                                    .filter(command -> command.scope().isInScope(channel))
                                    .flatMap(command -> preCheckCommand(ctx, command)
                                            .then(Mono.defer(() -> command.run(ctx)))
                                            .onErrorResume(t -> executeErrorHandler(t, errorHandler, ctx))
                                            .onErrorResume(t -> Mono.fromRunnable(() -> LOGGER.error("An unhandled " +
                                                    "error occurred when executing a command. Context: " + ctx, t))));
                        })));
            }

            @Override
            public Publisher<?> onInteractionCreate(InteractionCreateEvent event) {
                final var user = event.getInteraction().getUser();
                return event.getInteraction().getChannel().flatMap(channel -> eventProcessor
                        .filter(event, channel, user)
                        .filter(Boolean::booleanValue)
                        .flatMap(__ -> eventProcessor.localeForEvent(event, channel, user)
                                .defaultIfEmpty(defaultLocale))
                        .map(locale -> MatcherFunction.<CommandRunner>create()
                                .matchType(SlashCommandEvent.class, ev -> new SlashCommandRunner(
                                        new SlashCommandContext(CommandService.this, locale, ev, channel)))
                                .matchType(ButtonInteractEvent.class, ev -> new ButtonCommandRunner(
                                        new ButtonCommandContext(CommandService.this, locale, ev, channel)))
                                .matchType(SelectMenuInteractEvent.class, ev -> new SelectMenuCommandRunner(
                                        new SelectMenuCommandContext(CommandService.this, locale, ev, channel)))
                                .apply(event))
                        .flatMap(Mono::justOrEmpty)
                        .flatMap(runner -> runner.run()
                                .onErrorResume(t -> executeErrorHandler(t, errorHandler, runner.ctx()))
                                .onErrorResume(t -> Mono.fromRunnable(() -> LOGGER.error("An unhandled " +
                                        "error occurred when executing a command. Context: " + runner.ctx(), t)))));
            }

            @Override
            public Publisher<?> hookOnEvent(Event event) {
                if (event instanceof MessageCreateEvent) {
                    return onMessageCreate((MessageCreateEvent) event);
                }
                if (event instanceof InteractionCreateEvent) {
                    return onInteractionCreate((InteractionCreateEvent) event);
                }
                return Mono.empty();
            }
        }).then(Mono.fromRunnable(() -> LOGGER.info("Command listener completed"))));
    }

    private <C extends InteractionCommand> Mono<C> findInteractionCommand(Map<ContextKey, Map<String, C>> interactions,
                                                                          Map<String, C> regularMap,
                                                                          ContextKey key,
                                                                          ComponentInteractEvent event) {
        return Mono.justOrEmpty(interactions.getOrDefault(key, new ConcurrentHashMap<>()).remove(event.getCustomId()))
                .doOnNext(__ -> interactions.computeIfPresent(key, (k, v) -> v.isEmpty() ? null : v))
                .switchIfEmpty(Mono.justOrEmpty(regularMap.get(event.getCustomId())));
    }

    private Mono<Void> preCheckCommand(CommandContext ctx, Command command) {
        return command.privilege().checkGranted(ctx)
                .then(Mono.fromRunnable(() -> cooldownPerCommand.computeIfAbsent(command, Command::cooldown)
                        .fire(ctx.user().getId().asLong())));
    }

    private Mono<Void> executeErrorHandler(Throwable t, CommandErrorHandler errorHandler, CommandContext ctx) {
        return MatcherFunction.<Mono<Void>>create()
                .matchType(CommandFailedException.class, e -> errorHandler.handleCommandFailed(e, ctx))
                .matchType(InvalidSyntaxException.class, e -> errorHandler.handleInvalidSyntax(e, ctx))
                .matchType(PrivilegeException.class, e -> errorHandler.handlePrivilege(e, ctx))
                .matchType(CooldownException.class, e -> errorHandler.handleCooldown(e, ctx))
                .apply(t)
                .orElseGet(() -> errorHandler.handleDefault(t, ctx));
    }

    private interface CommandRunner {

        Mono<Void> run();

        CommandContext ctx();
    }

    private final static class ContextKey {

        private final long channelId;
        private final long userId;

        private ContextKey(long channelId, long userId) {
            this.channelId = channelId;
            this.userId = userId;
        }

        private static ContextKey from(CommandContext ctx) {
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

    private final class SlashCommandRunner implements CommandRunner {

        private final SlashCommandContext ctx;

        private SlashCommandRunner(SlashCommandContext ctx) {
            this.ctx = ctx;
        }

        @Override
        public Mono<Void> run() {
            return Mono.justOrEmpty(slashCommands.get(ctx.event().getCommandName()))
                    .flatMap(command -> preCheckCommand(ctx, command).then(Mono.defer(() -> command.run(ctx))));
        }

        @Override
        public CommandContext ctx() {
            return ctx;
        }
    }

    private final class ButtonCommandRunner implements CommandRunner {

        private final ButtonCommandContext ctx;

        private ButtonCommandRunner(ButtonCommandContext ctx) {
            this.ctx = ctx;
        }

        @Override
        public Mono<Void> run() {
            return findInteractionCommand(buttonCommandInteractions, buttonCommands, ContextKey.from(ctx), ctx.event())
                    .flatMap(command -> preCheckCommand(ctx, command).then(Mono.defer(() -> command.run(ctx))));
        }

        @Override
        public CommandContext ctx() {
            return ctx;
        }
    }

    private final class SelectMenuCommandRunner implements CommandRunner {

        private final SelectMenuCommandContext ctx;

        private SelectMenuCommandRunner(SelectMenuCommandContext ctx) {
            this.ctx = ctx;
        }

        @Override
        public Mono<Void> run() {
            return findInteractionCommand(selectMenuCommandInteractions, selectMenuCommands,
                    ContextKey.from(ctx), ctx.event())
                    .flatMap(command -> preCheckCommand(ctx, command).then(Mono.defer(() -> command.run(ctx))));
        }

        @Override
        public CommandContext ctx() {
            return ctx;
        }
    }
}
