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
package botrino.command;

import botrino.api.config.ConfigContainer;
import botrino.api.config.ConfigException;
import botrino.api.config.object.I18nConfig;
import botrino.api.util.MatcherFunction;
import botrino.command.config.CommandConfig;
import botrino.command.cooldown.Cooldown;
import botrino.command.cooldown.CooldownException;
import botrino.command.menu.InteractiveMenuFactory;
import botrino.command.menu.PaginationControls;
import botrino.command.privilege.PrivilegeException;
import com.github.alex1304.rdi.finder.annotation.RdiFactory;
import com.github.alex1304.rdi.finder.annotation.RdiService;
import discord4j.common.util.Snowflake;
import discord4j.core.GatewayDiscordClient;
import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.object.reaction.ReactionEmoji;
import reactor.core.publisher.Mono;
import reactor.util.Logger;
import reactor.util.Loggers;

import java.time.Duration;
import java.util.Locale;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

import static reactor.function.TupleUtils.function;

/**
 * Service that centralizes the management of commands.
 */
@RdiService
public final class CommandService {

    private static final Logger LOGGER = Loggers.getLogger(CommandService.class);

    private final CommandConfig commandConfig;
    private final GatewayDiscordClient gateway;
    private final InteractiveMenuFactory interactiveMenuFactory;
    private final Locale defaultLocale;
    private final CommandTree commandTree = new CommandTree();
    private final ConcurrentHashMap<Command, Cooldown> cooldownPerCommand = new ConcurrentHashMap<>();
    private CommandErrorHandler errorHandler;
    private CommandEventProcessor eventProcessor;

    @RdiFactory
    public CommandService(ConfigContainer configContainer, GatewayDiscordClient gateway) {
        this.commandConfig = configContainer.get(CommandConfig.class);
        this.gateway = gateway;
        this.interactiveMenuFactory = InteractiveMenuFactory.of(
                commandConfig.paginationControls()
                        .map(paginationControlsConfig -> PaginationControls.of(
                                paginationControlsConfig.nextEmoji()
                                        .map(CommandService::configToEmoji)
                                        .orElse(PaginationControls.DEFAULT_NEXT_EMOJI),
                                paginationControlsConfig.previousEmoji()
                                        .map(CommandService::configToEmoji)
                                        .orElse(PaginationControls.DEFAULT_PREVIOUS_EMOJI),
                                paginationControlsConfig.closeEmoji()
                                        .map(CommandService::configToEmoji)
                                        .orElse(PaginationControls.DEFAULT_CLOSE_EMOJI)
                        ))
                        .orElse(PaginationControls.getDefault()),
                Duration.ofSeconds(commandConfig.menuTimeoutSeconds().orElse(600L)));
        this.defaultLocale = Locale.forLanguageTag(configContainer.get(I18nConfig.class).defaultLocale());
    }

    private static ReactionEmoji configToEmoji(CommandConfig.EmojiConfig config) {
        if (config.id().isPresent() && config.name().isPresent()) {
            return ReactionEmoji.custom(
                    Snowflake.of(config.id().getAsLong()),
                    config.name().get(),
                    config.animated().orElse(false));
        }
        if (config.unicode().isEmpty()) {
            throw new ConfigException(config + " does not correspond to a valid emoji type (custom or unicode)");
        }
        return ReactionEmoji.unicode(config.unicode().get());
    }

    /**
     * Obtain an {@link InteractiveMenuFactory} instance based on the configuration of this command service.
     *
     * @return an {@link InteractiveMenuFactory}
     */
    public InteractiveMenuFactory interactiveMenuFactory() {
        return interactiveMenuFactory;
    }

    /**
     * Adds a command to this command service at the top level. The subcommand tree is resolved and the root is
     * registered in the service so that it can be invoked via message create events. It is generally not necessary to
     * use this method as classes within bot modules implementing {@link Command} are automatically added, but it can be
     * useful when adding commands built with {@link Command#of(Set, Function)} or {@link Command#builder(Set,
     * Function)}.
     *
     * @param command the command to add
     * @throws IllegalStateException if one of the aliases defined by the command conflicts with a command already
     *                               added, or if there is an alias conflict within the nested subcommands.
     */
    public void addTopLevelCommand(Command command) {
        Objects.requireNonNull(command);
        commandTree.addCommand(command);
        LOGGER.debug("Added command {}", command);
    }

    /**
     * Get the command instance for the given top level alias and subcommand aliases.
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
    public Optional<Command> getCommandAt(String topLevelAlias, String... subcommandAliases) {
        Objects.requireNonNull(topLevelAlias);
        Objects.requireNonNull(subcommandAliases);
        return commandTree.getCommandAt(topLevelAlias, subcommandAliases);
    }

    /**
     * Lists the commands at the given path. A path is defined as a sequence of aliases identifying the command for
     * which subcommands should be listed. An empty path is equivalent to listing all commands at top level.
     * <p>
     * For example, if a top-level command {@code foo} defines a subcommand {@code bar}, which itself defines a
     * subcommand {@code baz}:
     * <ul>
     *     <li>{@code listCommands()} will list the {@code foo} command</li>
     *     <li>{@code listCommands("foo")} will list the {@code bar} command</li>
     *     <li>{@code listCommands("foo", "bar")} will list the {@code baz} command</li>
     * </ul>
     *
     * @param path a sequence of aliases identifying the command for which subcommands should be listed, empty for
     *             listing top-level commands
     * @return a set of commands corresponding to the list result
     * @throws InvalidSyntaxException if {@code path} refers to a non-existing subcommand
     */
    public Set<Command> listCommands(String... path) {
        Objects.requireNonNull(path);
        return commandTree.listCommands(path);
    }

    void setErrorHandler(CommandErrorHandler errorHandler) {
        LOGGER.debug("Using error handler {}", errorHandler);
        this.errorHandler = errorHandler;
    }

    void setEventProcessor(CommandEventProcessor eventProcessor) {
        LOGGER.debug("Using event processor {}", eventProcessor);
        this.eventProcessor = eventProcessor;
    }

    Mono<Void> listenToCommands() {
        return gateway.on(MessageCreateEvent.class, event ->
                Mono.just(event)
                        .filterWhen(eventProcessor::filter)
                        .zipWhen(ev -> Mono.zip(
                                eventProcessor.prefixForEvent(ev).defaultIfEmpty(commandConfig.prefix()),
                                eventProcessor.localeForEvent(ev).defaultIfEmpty(defaultLocale)))
                        .flatMap(function((ev, t) -> processEvent(ev, t.getT1(), t.getT2()))))
                .then(Mono.fromRunnable(() -> LOGGER.info("Command listener completed")));
    }

    private Mono<Void> processEvent(MessageCreateEvent event, String prefix, Locale locale) {
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
        final var f_prefixUsed = prefixUsed;
        var input = TokenizedInput.tokenize(messageContent);
        if (input.getArguments().isEmpty()) {
            return Mono.empty();
        }
        var authorId = Snowflake.asLong(event.getMessage().getUserData().id());
        return event.getMessage().getChannel()
                .onErrorResume(e -> Mono.fromRunnable(() -> LOGGER.warn("Error when retrieving channel instance " +
                        "for message create event " + event, e)))
                .flatMap(channel -> Mono.justOrEmpty(commandTree.findForInput(input))
                        .filter(command -> command.scope().isInScope(channel))
                        .flatMap(command -> {
                            var ctx = new CommandContext(event, f_prefixUsed, input, locale, channel);
                            return command.privilege().checkGranted(ctx)
                                    .then(Mono.defer(() -> {
                                        cooldownPerCommand.computeIfAbsent(command, Command::cooldown).fire(authorId);
                                        return command.run(ctx);
                                    }))
                                    .onErrorResume(t -> executeErrorHandler(t, command.errorHandler(), ctx))
                                    .onErrorResume(t -> executeErrorHandler(t, errorHandler, ctx))
                                    .onErrorResume(t -> Mono.fromRunnable(() -> LOGGER.error("An unhandled error " +
                                            "occurred when executing a command. Context: " + ctx, t)));
                        }));
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
}
