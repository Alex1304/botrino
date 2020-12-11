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
package botrino.api.config;

import botrino.api.config.object.BotConfig;
import discord4j.core.DiscordClient;
import discord4j.core.GatewayDiscordClient;
import discord4j.core.object.presence.Presence;
import discord4j.core.shard.MemberRequestFilter;
import discord4j.gateway.intent.IntentSet;
import reactor.core.publisher.Mono;

/**
 * Allows to customize the Discord client and the login process.
 */
public interface LoginHandler {

    /**
     * Static variant of {@link LoginHandler#login(ConfigContainer)} to be used internally by the RDI container. This
     * method is not meant to be used directly in your application.
     *
     * @param loginHandler    the login handler
     * @param configContainer the container holding all the configuration for the bot
     * @return a Mono that connects to Discord upon subscription and emits the resulting {@link GatewayDiscordClient}
     */
    static Mono<GatewayDiscordClient> login(LoginHandler loginHandler, ConfigContainer configContainer) {
        return loginHandler.login(configContainer);
    }

    /**
     * Constructs a {@link GatewayDiscordClient} based on the information given by the configuration container.
     *
     * @param configContainer the container holding all the configuration for the bot
     * @return a Mono that connects to Discord upon subscription and emits the resulting {@link GatewayDiscordClient}
     */
    default Mono<GatewayDiscordClient> login(ConfigContainer configContainer) {
        var config = configContainer.get(BotConfig.class);
        var discordClient = DiscordClient.create(config.token());
        return discordClient.gateway()
                .setInitialStatus(shard -> config.presence()
                        .map(BotConfig.StatusConfig::toStatusUpdate)
                        .orElseGet(Presence::online))
                .setEnabledIntents(config.enabledIntents().stream().boxed()
                        .map(IntentSet::of)
                        .findAny()
                        .orElseGet(IntentSet::nonPrivileged))
                .setMemberRequestFilter(MemberRequestFilter.none())
                .login()
                .single();
    }
}
