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

import java.util.ArrayDeque;
import java.util.HashMap;
import java.util.Locale;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@RdiService
public final class CommandService {

    private static final Logger LOGGER = Loggers.getLogger(CommandService.class);

    private final CommandConfig commandConfig;
    private final I18nConfig i18nConfig;
    private final GatewayDiscordClient gateway;

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
    }

    private Mono<Void> processEvent(MessageCreateEvent event) {
        var guildId = event.getGuildId();
        var prefixOfGuild = guildId.map(Snowflake::asLong)
                .map(prefixByGuild::get)
                .orElse(commandConfig.getCommandPrefix());
        var botId = event.getClient().getSelfId().asLong();
        var prefixes = Set.of("<@" + botId + ">", "<@!" + botId + ">", prefixOfGuild);
        var input = tokenize(prefixes, event.getMessage().getContent());
        if (input == null || input.getArguments().isEmpty()) {
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
                            if (isByBot && !command.allowUseByBots()) {
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
                            return command.getScope().isInScope(channel);
                        })
                        .flatMap(command -> {
                            var locale = guildId.map(Snowflake::asLong)
                                    .map(localeByGuild::get)
                                    .orElse(i18nConfig.getDefaultLocale());
                            var ctx = new CommandContext(event, input, locale, channel);
                            return command.getPrivilege().isGranted(ctx)
                                    .then(Mono.defer(() -> command.run(ctx)))
                                    .onErrorResume(e -> {
                                        if (e instanceof CommandFailedException) {
                                            return errorHandler.handleCommandFailed((CommandFailedException) e, ctx);
                                        }
                                        if (e instanceof PrivilegeException) {
                                            return errorHandler.handlePrivilege((PrivilegeException) e, ctx);
                                        }
                                        if (e instanceof BadSubcommandException) {
                                            return errorHandler.handleBadSubcommand((BadSubcommandException) e, ctx);
                                        }
                                        return errorHandler.handleDefault(e, ctx);
                                    })
                                    .onErrorResume(e -> Mono.fromRunnable(() -> LOGGER.error("An unhandled error " +
                                            "occurred when executing a command. Context: " + ctx, e)));
                        }));
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

    void addCommand(Command command) {
        commandTree.addCommand(command);
        LOGGER.debug("Added command {}", command);
    }

    void setErrorHandler(CommandErrorHandler errorHandler) {
        this.errorHandler = errorHandler;
    }

    Mono<Void> listenToCommands() {
        return gateway.on(MessageCreateEvent.class, this::processEvent)
                .then(Mono.fromRunnable(() -> LOGGER.info("Command listener completed")));
    }

    @Nullable
    private CommandInput tokenize(Set<String> prefixes, String input) {
        // Extracting prefix
        String prefixUsed = null;
        for (var p : prefixes) {
            if (input.toLowerCase().startsWith(p.toLowerCase())) {
                input = input.substring(p.length());
                prefixUsed = p;
                break;
            }
        }
        if (prefixUsed == null) {
            return null;
        }
        // Extracting the tokens
        var tokens = new ArrayDeque<String>();
        var buffer = new StringBuilder();
        var inQuotes = false;
        var escaping = false;
        for (var c : input.strip().toCharArray()) {
            if (!escaping) {
                if (c == '\\') {
                    escaping = true;
                    continue;
                } else if (c == '"') {
                    inQuotes = !inQuotes;
                    continue;
                }
            }
            if (!inQuotes) {
                if (Character.isWhitespace(c)) {
                    if (buffer.length() > 0) {
                        tokens.add(buffer.toString());
                        buffer.delete(0, buffer.length());
                    }
                } else {
                    buffer.append(c);
                }
            } else {
                buffer.append(c);
            }
            escaping = false;
        }
        if (buffer.length() != 0) {
            tokens.add(buffer.toString());
        }
        // Separating tokens into flags and args
        var flags = new HashMap<String, String>();
        var args = new ArrayDeque<String>();
        var flagPrefix = commandConfig.getFlagPrefix();
        while (!tokens.isEmpty()) {
            var token = tokens.remove();
            if (token.startsWith(flagPrefix) && token.length() > flagPrefix.length()) {
                var split = token.substring(flagPrefix.length()).split("=", 2);
                if (split.length == 1) {
                    flags.put(split[0], "");
                } else {
                    flags.put(split[0], split[1]);
                }
            } else {
                args.add(token);
            }
        }
        return new CommandInput(input, prefixUsed, args, flags);
    }

}
