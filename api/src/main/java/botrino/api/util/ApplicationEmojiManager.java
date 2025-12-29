/*
 * This file is part of the Botrino project and is licensed under the MIT license.
 *
 * Copyright (c) 2025 Alexandre Miranda
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

import discord4j.core.GatewayDiscordClient;
import discord4j.core.object.entity.ApplicationEmoji;
import discord4j.core.object.entity.ApplicationInfo;
import reactor.core.publisher.Mono;
import reactor.util.Logger;
import reactor.util.Loggers;

import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;

import static java.util.stream.Collectors.toUnmodifiableMap;

/**
 * Convenience class to load application emojis and cache them for synchronous use.
 * <p>
 * The cache indexes emojis by case-insensitive names. If multiple emojis with the same name are found, it will accept
 * one of them (often the first one but no guarantee) and a warning will be logged.
 */
public final class ApplicationEmojiManager {

    private static final Logger LOGGER = Loggers.getLogger(ApplicationEmojiManager.class);

    private final AtomicReference<Map<String, ApplicationEmoji>> emojiCache;
    private final GatewayDiscordClient gateway;

    private ApplicationEmojiManager(Map<String, ApplicationEmoji> emojiCache, GatewayDiscordClient gateway) {
        this.emojiCache = new AtomicReference<>(emojiCache);
        this.gateway = gateway;
    }

    /**
     * Creates a new {@link ApplicationEmojiManager} that loads emojis for the current application. Emojis present at
     * the time of subscription will be cached and ready to be used via {@link #get(String)} synchronously. If multiple
     * emojis with the same name are found, it will accept one of them (often the first one but no guarantee) and a
     * warning will be logged.
     *
     * @return a Mono emitting a new {@link ApplicationEmojiManager} with cached emojis.
     * @see #reload()
     */
    public static Mono<ApplicationEmojiManager> load(GatewayDiscordClient gateway) {
        Objects.requireNonNull(gateway);
        return loadApplicationEmojis(gateway).map(emojis -> new ApplicationEmojiManager(emojis, gateway));
    }

    /**
     * Reloads application emojis with the latest data from the API and updates the cache. In case of error during
     * reload, the cache is left untouched.
     *
     * @return a Mono completing empty after the cache is updated.
     */
    public Mono<Void> reload() {
        return loadApplicationEmojis(gateway).doOnNext(emojiCache::set).then();
    }

    /**
     * Gets an emoji previously cached via {@link #load(GatewayDiscordClient)} and {@link #reload()} with the given
     * name. The name is not case-sensitive.
     *
     * @param emojiName the name of the emoji to get (case-insensitive)
     * @return the emoji
     * @throws NoSuchElementException if an emoji with the given name does not exist
     */
    public ApplicationEmoji get(String emojiName) {
        Objects.requireNonNull(emojiName);
        var emoji = emojiCache.get().get(emojiName.toLowerCase());
        if (emoji == null) {
            throw new NoSuchElementException("No emoji found with name " + emojiName);
        }
        return emoji;
    }

    private static Mono<Map<String, ApplicationEmoji>> loadApplicationEmojis(GatewayDiscordClient gateway) {
        return gateway.getApplicationInfo()
                .flatMapMany(ApplicationInfo::getEmojis)
                .collect(toUnmodifiableMap(emoji -> emoji.getName().toLowerCase(), Function.identity(), (a, b) -> {
                    LOGGER.warn("Emojis with duplicate names detected ({}), unexpected outcome may occur", a.getName());
                    return a;
                }));
    }
}
