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
import botrino.api.config.i18n.I18nConfig;
import botrino.command.config.CommandConfig;
import botrino.command.menu.InteractiveMenuFactory;
import botrino.command.privilege.PrivilegeException;
import com.github.alex1304.rdi.finder.annotation.RdiFactory;
import com.github.alex1304.rdi.finder.annotation.RdiService;
import discord4j.common.util.Snowflake;
import discord4j.core.GatewayDiscordClient;
import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.object.entity.User;
import reactor.core.publisher.Mono;
import reactor.util.Logger;
import reactor.util.Loggers;
import reactor.util.annotation.Nullable;

import java.util.Locale;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

@RdiService
public final class CommandService {

    private static final Logger LOGGER = Loggers.getLogger(CommandService.class);

    private final CommandConfig commandConfig;
    private final I18nConfig i18nConfig;
    private final GatewayDiscordClient gateway;
    private final InteractiveMenuFactory interactiveMenuFactory;

    private final ConcurrentHashMap<Long, String> prefixByGuild = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<Long, Locale> localeByGuild = new ConcurrentHashMap<>();
    private final Set<Long> blacklist = ConcurrentHashMap.newKeySet();
    private final CommandTree commandTree = new CommandTree();
    private CommandErrorHandler errorHandler;

    @RdiFactory
    public CommandService(ConfigContainer configContainer, GatewayDiscordClient gateway) {
        this.commandConfig = configContainer.get(CommandConfig.class);
        this.i18nConfig = configContainer.get(I18nConfig.class);
        this.gateway = gateway;
        this.interactiveMenuFactory = InteractiveMenuFactory.of(commandConfig.getPaginationControls(),
                commandConfig.getMenuTimeout());
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
     * Sets a prefix specific for the given guild. If one was already set for the same guild, it is overwritten.
     *
     * @param guildId the guild id
     * @param prefix  the new prefix. May be null, in which case the prefix is removed.
     */
    public void setPrefixForGuild(long guildId, @Nullable String prefix) {
        if (prefix == null) {
            prefixByGuild.remove(guildId);
            LOGGER.debug("Removed prefix for guild {}", guildId);
            return;
        }
        prefixByGuild.put(guildId, prefix);
        LOGGER.debug("Changed prefix for guild {}: {}", guildId, prefix);
    }

    /**
     * Sets a locale specific for the given guild. If one was already set for the same guild, it is overwritten.
     *
     * @param guildId the guild id
     * @param locale  the new locale. May be null, in which case the locale is removed.
     */
    public void setLocaleForGuild(long guildId, @Nullable Locale locale) {
        if (locale == null) {
            localeByGuild.remove(guildId);
            LOGGER.debug("Removed locale for guild {}", guildId);
            return;
        }
        localeByGuild.put(guildId, locale);
        LOGGER.debug("Changed locale for guild {}: {}", guildId, locale);
    }

    /**
     * Adds a command to this command service. The subcommand tree is resolved and the root is registered in the service
     * so that it can be invoked via message create events. It is generally not necessary to use this method as classes
     * within bot modules implementing {@link Command} are automatically added, but it can be useful when adding
     * commands built with {@link Command#of(Set, Function)} or {@link Command#builder(Set, Function)}.
     *
     * @param command the command to add
     * @throws IllegalStateException if one of the aliases defined by the command conflicts with a command already
     *                               added, or if there is an alias conflict within the nested subcommands.
     */
    public void addCommand(Command command) {
        Objects.requireNonNull(command);
        commandTree.addCommand(command);
        LOGGER.debug("Added command {}", command);
    }

    void setErrorHandler(CommandErrorHandler errorHandler) {
        LOGGER.debug("Using error handler {}", errorHandler);
        this.errorHandler = errorHandler;
    }

    Mono<Void> listenToCommands() {
        return gateway.on(MessageCreateEvent.class, this::processEvent)
                .then(Mono.fromRunnable(() -> LOGGER.info("Command listener completed")));
    }

    private Mono<Void> processEvent(MessageCreateEvent event) {
        var guildId = event.getGuildId();
        var prefixOfGuild = guildId.map(Snowflake::asLong)
                .map(prefixByGuild::get)
                .orElse(commandConfig.getPrefix());
        var botId = event.getClient().getSelfId().asLong();
        var prefixes = Set.of("<@" + botId + ">", "<@!" + botId + ">", prefixOfGuild);
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
        return event.getMessage().getChannel()
                .onErrorResume(e -> Mono.fromRunnable(() -> LOGGER.warn("Error when retrieving channel instance " +
                        "for message create event " + event, e)))
                .flatMap(channel -> Mono.justOrEmpty(commandTree.findForInput(input))
                        .filter(command -> {
                            var isByBot = event.getMessage().getAuthor().map(User::isBot).orElse(true);
                            var authorId = event.getMessage().getAuthor().map(User::getId);
                            var channelId = event.getMessage().getChannelId();
                            if (isByBot && command.ignoreBots()) {
                                LOGGER.debug("Ignoring command due to author being a bot account: {}", command);
                                return false;
                            }
                            if (authorId.map(id -> blacklist.contains(id.asLong())).orElse(false)) {
                                LOGGER.debug("Ignoring command due to AUTHOR being blacklisted: {}", command);
                                return false;
                            }
                            if (guildId.map(id -> blacklist.contains(id.asLong())).orElse(false)) {
                                LOGGER.debug("Ignoring command due to GUILD being blacklisted: {}", command);
                                return false;
                            }
                            if (blacklist.contains(channelId.asLong())) {
                                LOGGER.debug("Ignoring command due to CHANNEL being blacklisted: {}", command);
                                return false;
                            }
                            return command.scope().isInScope(channel);
                        })
                        .flatMap(command -> {
                            var locale = guildId.map(Snowflake::asLong)
                                    .map(localeByGuild::get)
                                    .orElse(i18nConfig.getDefaultLocale());
                            var ctx = new CommandContext(event, f_prefixUsed, input, locale, channel);
                            return command.privilege().isGranted(ctx)
                                    .then(Mono.defer(() -> command.run(ctx)))
                                    .onErrorResume(t -> executeErrorHandler(t, command.errorHandler(), ctx))
                                    .onErrorResume(t -> executeErrorHandler(t, errorHandler, ctx))
                                    .onErrorResume(t -> Mono.fromRunnable(() -> LOGGER.error("An unhandled error " +
                                            "occurred when executing a command. Context: " + ctx, t)));
                        }));
    }

    private Mono<Void> executeErrorHandler(Throwable t, CommandErrorHandler errorHandler, CommandContext ctx) {
        if (t instanceof CommandFailedException) {
            return errorHandler.handleCommandFailed((CommandFailedException) t, ctx);
        }
        if (t instanceof PrivilegeException) {
            return errorHandler.handlePrivilege((PrivilegeException) t, ctx);
        }
        if (t instanceof BadSubcommandException) {
            return errorHandler.handleBadSubcommand((BadSubcommandException) t, ctx);
        }
        return errorHandler.handleDefault(t, ctx);
    }
}
