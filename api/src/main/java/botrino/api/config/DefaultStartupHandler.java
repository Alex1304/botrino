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
 * Startup handler used by default when none is specified. It keeps all default settings of the discord client,
 * just adding the status and the intents specified in the config.
 */
public final class DefaultStartupHandler implements StartupHandler {

    @Override
    public Mono<GatewayDiscordClient> login(ConfigContainer configContainer) {
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
