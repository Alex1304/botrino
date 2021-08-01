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
import botrino.interaction.config.InteractionConfig;
import botrino.interaction.context.ButtonContext;
import botrino.interaction.context.InteractionContext;
import botrino.interaction.context.SelectMenuContext;
import botrino.interaction.context.SlashCommandContext;
import botrino.interaction.cooldown.Cooldown;
import botrino.interaction.cooldown.CooldownException;
import botrino.interaction.privilege.PrivilegeException;
import com.github.alex1304.rdi.finder.annotation.RdiFactory;
import com.github.alex1304.rdi.finder.annotation.RdiService;
import discord4j.core.GatewayDiscordClient;
import discord4j.core.event.domain.interaction.*;
import reactor.core.publisher.Mono;
import reactor.util.Logger;
import reactor.util.Loggers;

import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@RdiService
public class InteractionService {

    private static final Logger LOGGER = Loggers.getLogger(InteractionService.class);

    private final InteractionConfig interactionConfig;
    private final GatewayDiscordClient gateway;
    private final Locale defaultLocale;

    private final Map<String, SlashCommand> slashCommands = new ConcurrentHashMap<>();
    private final Map<String, ButtonInteraction<?>> buttonInteractions = new ConcurrentHashMap<>();
    private final Map<ContextKey, Map<String, ButtonInteraction<?>>> buttonInteractionsSingleUse =
            new ConcurrentHashMap<>();
    private final Map<String, SelectMenuInteraction<?>> selectMenuInteractions = new ConcurrentHashMap<>();
    private final Map<ContextKey, Map<String, SelectMenuInteraction<?>>> selectMenuInteractionsSingleUse =
            new ConcurrentHashMap<>();

    private final Map<Interaction, Cooldown> cooldownPerCommand = new ConcurrentHashMap<>();
    private InteractionErrorHandler errorHandler;
    private InteractionEventProcessor eventProcessor;

    @RdiFactory
    public InteractionService(ConfigContainer configContainer, GatewayDiscordClient gateway) {
        this.interactionConfig = configContainer.get(InteractionConfig.class);
        this.gateway = gateway;
        this.defaultLocale = Locale.forLanguageTag(configContainer.get(I18nConfig.class).defaultLocale());
    }

    void setErrorHandler(InteractionErrorHandler errorHandler) {
        LOGGER.debug("Using error handler {}", errorHandler);
        this.errorHandler = errorHandler;
    }

    void setEventProcessor(InteractionEventProcessor eventProcessor) {
        LOGGER.debug("Using event processor {}", eventProcessor);
        this.eventProcessor = eventProcessor;
    }

    public void register(SlashCommand interaction) {
        Objects.requireNonNull(interaction);
        slashCommands.put(interaction.data().name(), interaction);
        LOGGER.debug("Registered slash command {}", interaction);
    }

    public void register(ButtonInteraction<?> interaction) {
        Objects.requireNonNull(interaction);
        buttonInteractions.put(interaction.customId(), interaction);
        LOGGER.debug("Registered button interaction {}", interaction);
    }

    public void register(SelectMenuInteraction<?> interaction) {
        Objects.requireNonNull(interaction);
        selectMenuInteractions.put(interaction.customId(), interaction);
        LOGGER.debug("Registered select menu interaction {}", interaction);
    }

    public void registerSingleUse(ButtonInteraction<?> interaction, InteractionContext parentContext) {
        Objects.requireNonNull(interaction);
        Objects.requireNonNull(parentContext);
        buttonInteractionsSingleUse.computeIfAbsent(ContextKey.from(parentContext), k -> new ConcurrentHashMap<>())
                .put(interaction.customId(), interaction);
        LOGGER.debug("Registered single use button interaction {}", interaction);
    }

    public void registerSingleUse(SelectMenuInteraction<?> interaction, InteractionContext parentContext) {
        Objects.requireNonNull(interaction);
        Objects.requireNonNull(parentContext);
        selectMenuInteractionsSingleUse.computeIfAbsent(ContextKey.from(parentContext), k -> new ConcurrentHashMap<>())
                .put(interaction.customId(), interaction);
        LOGGER.debug("Registered single use select menu interaction {}", interaction);
    }

    Mono<Void> handleCommands() {
        final var uploadSlashCommands = gateway.rest().getApplicationId()
                .flatMap(id -> Mono.justOrEmpty(interactionConfig.slashCommandsGuildId())
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
        return uploadSlashCommands.then(gateway
                .on(InteractionCreateEvent.class, event -> event.getInteraction().getChannel()
                        .flatMap(channel -> eventProcessor.filter(event)
                                .filter(Boolean::booleanValue)
                                .flatMap(__ -> eventProcessor.computeLocale(event).defaultIfEmpty(defaultLocale))
                                .map(locale -> MatcherFunction.<CommandRunner>create()
                                        .matchType(SlashCommandEvent.class, ev -> new SlashCommandRunner(
                                                new SlashCommandContext(InteractionService.this, locale, ev, channel)))
                                        .matchType(ButtonInteractEvent.class, ev -> new ButtonCommandRunner(
                                                new ButtonContext(InteractionService.this, locale, ev, channel)))
                                        .matchType(SelectMenuInteractEvent.class, ev -> new SelectMenuCommandRunner(
                                                new SelectMenuContext(InteractionService.this, locale, ev, channel)))
                                        .apply(event))
                                .flatMap(Mono::justOrEmpty)
                                .flatMap(runner -> runner.run()
                                        .onErrorResume(t -> executeErrorHandler(t, errorHandler, runner.ctx()))
                                        .onErrorResume(t -> Mono.fromRunnable(
                                                () -> LOGGER.error("An unhandled error occurred when executing an " +
                                                        "interaction. Context: " + runner.ctx(), t))))))
                .then(Mono.fromRunnable(() -> LOGGER.info("Command listener completed"))));
    }

    private <C extends ComponentInteraction<?, ?>>
    Mono<C> findSingleUseInteraction(Map<ContextKey, Map<String, C>> interactions, Map<String, C> regularMap,
                                     ContextKey key, ComponentInteractEvent event) {
        return Mono.justOrEmpty(interactions.getOrDefault(key, new ConcurrentHashMap<>()).remove(event.getCustomId()))
                .doOnNext(__ -> interactions.computeIfPresent(key, (k, v) -> v.isEmpty() ? null : v))
                .switchIfEmpty(Mono.justOrEmpty(regularMap.get(event.getCustomId())));
    }

    private Mono<Void> preCheck(InteractionContext ctx, Interaction interaction) {
        return interaction.privilege().checkGranted(ctx)
                .then(Mono.fromRunnable(() -> cooldownPerCommand.computeIfAbsent(interaction, Interaction::cooldown)
                        .fire(ctx.user().getId().asLong())));
    }

    private Mono<Void> executeErrorHandler(Throwable t, InteractionErrorHandler errorHandler, InteractionContext ctx) {
        return MatcherFunction.<Mono<Void>>create()
                .matchType(InteractionFailedException.class, e -> errorHandler.handleInteractionFailed(e, ctx))
                .matchType(PrivilegeException.class, e -> errorHandler.handlePrivilege(e, ctx))
                .matchType(CooldownException.class, e -> errorHandler.handleCooldown(e, ctx))
                .apply(t)
                .orElseGet(() -> errorHandler.handleDefault(t, ctx));
    }

    private interface CommandRunner {

        Mono<Void> run();

        InteractionContext ctx();
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

    private final class SlashCommandRunner implements CommandRunner {

        private final SlashCommandContext ctx;

        private SlashCommandRunner(SlashCommandContext ctx) {
            this.ctx = ctx;
        }

        @Override
        public Mono<Void> run() {
            return Mono.justOrEmpty(slashCommands.get(ctx.event().getCommandName()))
                    .flatMap(command -> preCheck(ctx, command)
                            .then(Mono.defer(() -> Mono.from(command.run(ctx)).then())));
        }

        @Override
        public InteractionContext ctx() {
            return ctx;
        }
    }

    private final class ButtonCommandRunner implements CommandRunner {

        private final ButtonContext ctx;

        private ButtonCommandRunner(ButtonContext ctx) {
            this.ctx = ctx;
        }

        @Override
        public Mono<Void> run() {
            return findSingleUseInteraction(buttonInteractionsSingleUse, buttonInteractions,
                    ContextKey.from(ctx), ctx.event())
                    .flatMap(interaction -> preCheck(ctx, interaction)
                            .then(Mono.defer(() -> Mono.from(interaction.run(ctx)).then())));
        }

        @Override
        public InteractionContext ctx() {
            return ctx;
        }
    }

    private final class SelectMenuCommandRunner implements CommandRunner {

        private final SelectMenuContext ctx;

        private SelectMenuCommandRunner(SelectMenuContext ctx) {
            this.ctx = ctx;
        }

        @Override
        public Mono<Void> run() {
            return findSingleUseInteraction(selectMenuInteractionsSingleUse, selectMenuInteractions,
                    ContextKey.from(ctx), ctx.event())
                    .flatMap(interaction -> preCheck(ctx, interaction)
                            .then(Mono.defer(() -> Mono.from(interaction.run(ctx)).then())));
        }

        @Override
        public InteractionContext ctx() {
            return ctx;
        }
    }
}
