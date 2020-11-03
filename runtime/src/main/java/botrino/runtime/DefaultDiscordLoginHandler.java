package botrino.runtime;

import botrino.framework.config.ConfigContainer;
import botrino.framework.config.DiscordLoginHandler;
import botrino.framework.config.bot.BotConfig;
import discord4j.core.DiscordClient;
import discord4j.core.GatewayDiscordClient;
import discord4j.core.event.EventDispatcher;
import discord4j.core.retriever.EntityRetrievalStrategy;
import discord4j.core.shard.MemberRequestFilter;
import discord4j.gateway.intent.IntentSet;
import reactor.core.publisher.Mono;
import reactor.util.Logger;
import reactor.util.Loggers;
import reactor.util.concurrent.Queues;

public class DefaultDiscordLoginHandler implements DiscordLoginHandler {

    private static final Logger LOGGER = Loggers.getLogger(DefaultDiscordLoginHandler.class);

    @Override
    public Mono<GatewayDiscordClient> login(ConfigContainer configContainer) {
        var config = configContainer.get(BotConfig.class);
        var discordClient = DiscordClient.create(config.getToken());
        return discordClient.gateway()
                .setInitialStatus(shard -> config.getPresence())
                .setEventDispatcher(EventDispatcher.withLatestEvents(Queues.SMALL_BUFFER_SIZE))
                .setEntityRetrievalStrategy(EntityRetrievalStrategy.STORE_FALLBACK_REST)
                .setAwaitConnections(true)
                .setEnabledIntents(IntentSet.of(config.getEnabledIntents()))
                .setMemberRequestFilter(MemberRequestFilter.none())
                .login()
                .single();
    }
}
