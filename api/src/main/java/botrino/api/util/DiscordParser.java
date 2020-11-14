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
package botrino.api.util;

import discord4j.common.util.Snowflake;
import discord4j.core.GatewayDiscordClient;
import discord4j.core.object.entity.Guild;
import discord4j.core.object.entity.Role;
import discord4j.core.object.entity.User;
import discord4j.core.object.entity.channel.GuildChannel;
import reactor.core.publisher.Mono;

import java.util.NoSuchElementException;

/**
 * Contains utility methods to parse a user input into a Discord entity.
 */
public final class DiscordParser {

    private DiscordParser() {
    }

    /**
     * Parses the input into a Discord user. Emits {@link NoSuchElementException} if not found.
     *
     * @param gateway the gateway client used to make requests to Discord
     * @param str     the input
     * @return a Mono emitting the found user
     */
    public static Mono<User> parseUser(GatewayDiscordClient gateway, String str) {
        return Mono.just(str)
                .map(Snowflake::of)
                .onErrorResume(e -> Mono.just(str.substring(2, str.length() - 1))
                        .map(Snowflake::of))
                .onErrorResume(e -> Mono.just(str.substring(3, str.length() - 1))
                        .map(Snowflake::of))
                .flatMap(userId -> gateway.getUserById(userId).single())
                .onErrorResume(e -> gateway.getUsers()
                        .filter(user -> user.getTag().startsWith(str))
                        .next()
                        .single());
    }

    /**
     * Parses the input into a Discord role. Emits {@link NoSuchElementException} if not found. =
     *
     * @param gateway the gateway client used to make requests to Discord
     * @param guildId the ID of the guild the desired role belongs to
     * @param str     the input
     * @return a Mono emitting the found role
     */
    public static Mono<Role> parseRole(GatewayDiscordClient gateway, Snowflake guildId, String str) {
        return Mono.just(str)
                .map(Snowflake::of)
                .onErrorResume(e -> Mono.just(str.substring(3, str.length() - 1))
                        .map(Snowflake::of))
                .flatMap(roleId -> gateway.getRoleById(guildId, roleId).single())
                .onErrorResume(e -> gateway
                        .getGuildById(guildId)
                        .flatMapMany(Guild::getRoles)
                        .filter(r -> r.getName().toLowerCase().startsWith(str.toLowerCase()))
                        .next()
                        .single());
    }

    /**
     * Parses the input into a Discord channel. Emits {@link NoSuchElementException} if not found.
     *
     * @param gateway the gateway client used to make requests to Discord
     * @param guildId the ID of the guild the desired channel belongs to
     * @param str     the input
     * @return a Mono emitting the found channel
     */
    public static Mono<GuildChannel> parseGuildChannel(GatewayDiscordClient gateway, Snowflake guildId, String str) {
        return Mono.just(str)
                .map(Snowflake::of)
                .onErrorResume(e -> Mono.just(str.substring(2, str.length() - 1))
                        .map(Snowflake::of))
                .flatMap(channelId -> gateway.getChannelById(channelId).single())
                .ofType(GuildChannel.class)
                .onErrorResume(e -> gateway.getGuildById(guildId)
                        .flatMapMany(Guild::getChannels)
                        .filter(r -> r.getName().toLowerCase().startsWith(str.toLowerCase()))
                        .next()
                        .single());
    }

}
