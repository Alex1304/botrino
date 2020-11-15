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
import discord4j.core.object.entity.GuildEmoji;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;

import static java.util.stream.Collectors.toUnmodifiableMap;

/**
 * Allows to conveniently load and retrieve emojis by name in the scope of specific guilds.
 */
public class EmojiManager {

    private final Set<Snowflake> guildIds;
    private final AtomicReference<Map<String, GuildEmoji>> emojiCache = new AtomicReference<>(Map.of());

    private EmojiManager(Set<Snowflake> guildIds) {
        this.guildIds = guildIds;
    }

    /**
     * Creates a new {@link EmojiManager} that can load emojis from the specified guilds. Emojis are not immediately
     * loaded, an explicit call to {@link #loadFromGateway(GatewayDiscordClient)} must be made.
     *
     * @param guildIds the IDs of the guilds to load emojis from
     * @return a new {@link EmojiManager}
     */
    public static EmojiManager create(Set<Snowflake> guildIds) {
        Objects.requireNonNull(guildIds);
        return new EmojiManager(guildIds);
    }

    /**
     * Loads all the emojis from the guilds specified when creating this manager, using the given gateway client.
     * Previously loaded emojis with be discarded and replaced with the newly loaded emojis. The replacement is atomic,
     * which means the old cache will always be accessible until new emojis have finished loading.
     *
     * <p>
     * Since indexing is done by emoji name, an error will occur if several emojis with the same name are found in the
     * guilds specified.
     *
     * @param gateway the gateway client to use to load emojis
     * @return a Mono completing when the load is successful. It will error with {@link IllegalStateException} if
     * several emojis with the same name are found. Any other error will be forwarded as-is through the mono.
     */
    public Mono<Void> loadFromGateway(GatewayDiscordClient gateway) {
        return Flux.fromIterable(guildIds)
                .flatMap(gateway::getGuildEmojis)
                .collect(toUnmodifiableMap(GuildEmoji::getName, Function.identity()))
                .doOnNext(emojiCache::set)
                .then();
    }

    /**
     * Gets the emoji with the given name. If more than one emoji with this name existed in the specified guilds, the
     * result is undefined.
     *
     * @param emojiName the name of the emoji to get
     * @return the emoji
     * @throws NoSuchElementException if the emoji does not exist or hasn't be loaded in this manager
     */
    public GuildEmoji get(String emojiName) {
        var emoji = emojiCache.get().get(emojiName);
        if (emoji == null) {
            throw new NoSuchElementException("No emoji found with name " + emojiName + ". If it exists, make sure it " +
                    "has been correctly loaded via loadFromGateway(...)");
        }
        return emoji;
    }
}
