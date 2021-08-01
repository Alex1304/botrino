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
package botrino.command.grammar;

import botrino.api.util.DiscordParser;
import botrino.command.CommandContext;
import discord4j.core.object.entity.Role;
import discord4j.core.object.entity.User;
import discord4j.core.object.entity.channel.GuildChannel;
import reactor.core.publisher.Mono;

import java.util.function.Function;

/**
 * Transforms an argument of a command into a type that can be manipulated more conveniently in the code of a command .
 * Typically used in {@link CommandGrammar}, it is possible to define a reusable mapping for each argument, which can be
 * as simple as a string-to-integer conversion to a more complex retrieval operation such as a Discord role or a
 * channel.
 *
 * @param <T> the target type of the transformation
 */
@FunctionalInterface
public interface ArgumentMapper<T> {

    /**
     * An {@link ArgumentMapper} that maps a string argument to another target type via the given function. For basic
     * context-insensitive and synchronous transformations, this may be more convenient than implementing the lambda of
     * {@link ArgumentMapper} directly.
     *
     * @param transformer the function to transform the string
     * @param <T>         the target type of the transformation
     * @return an {@link ArgumentMapper}
     */
    static <T> ArgumentMapper<T> as(Function<String, ? extends T> transformer) {
        return (ctx, arg) -> Mono.just(transformer.apply(arg));
    }

    /**
     * An {@link ArgumentMapper} that doesn't perform any transformation to the original string.
     *
     * @return an {@link ArgumentMapper}
     */
    static ArgumentMapper<String> identity() {
        return as(Function.identity());
    }

    /**
     * An {@link ArgumentMapper} that parses the original string to an integer. This is equivalent to:
     * <pre>as(Integer::parseInt)</pre>
     *
     * @return an {@link ArgumentMapper}
     */
    static ArgumentMapper<Integer> asInteger() {
        return as(Integer::parseInt);
    }

    /**
     * An {@link ArgumentMapper} that parses the original string to a long. This is equivalent to:
     * <pre>as(Long::parseLong)</pre>
     *
     * @return an {@link ArgumentMapper}
     */
    static ArgumentMapper<Long> asLong() {
        return as(Long::parseLong);
    }

    /**
     * An {@link ArgumentMapper} that parses the original string to a double. This is equivalent to:
     * <pre>as(Double::parseDouble)</pre>
     *
     * @return an {@link ArgumentMapper}
     */
    static ArgumentMapper<Double> asDouble() {
        return as(Double::parseDouble);
    }

    /**
     * An {@link ArgumentMapper} that parses the original string to a boolean. This is equivalent to:
     * <pre>as(Boolean::parseBoolean)</pre>
     *
     * @return an {@link ArgumentMapper}
     */
    static ArgumentMapper<Boolean> asBoolean() {
        return as(Boolean::parseBoolean);
    }

    /**
     * An {@link ArgumentMapper} that attempts to retrieve a Discord {@link GuildChannel} that corresponds to the
     * original string, either via its ID, its name or its tag.
     *
     * @return an {@link ArgumentMapper}
     */
    static ArgumentMapper<GuildChannel> asGuildChannel() {
        return (ctx, arg) -> ctx.event().getGuildId()
                .map(guildId -> DiscordParser.parseGuildChannel(ctx.event().getClient(), guildId, arg))
                .orElseGet(() -> Mono.error(new IllegalStateException()));
    }

    /**
     * An {@link ArgumentMapper} that attempts to retrieve a Discord {@link Role} that corresponds to the original
     * string, either via its ID, its name or its tag.
     *
     * @return an {@link ArgumentMapper}
     */
    static ArgumentMapper<Role> asGuildRole() {
        return (ctx, arg) -> ctx.event().getGuildId()
                .map(guildId -> DiscordParser.parseRole(ctx.event().getClient(), guildId, arg))
                .orElseGet(() -> Mono.error(new IllegalStateException()));
    }

    /**
     * An {@link ArgumentMapper} that attempts to retrieve a Discord {@link User} that corresponds to the original
     * string, either via its ID, its name or its tag.
     *
     * @return an {@link ArgumentMapper}
     */
    static ArgumentMapper<User> asUser() {
        return (ctx, arg) -> DiscordParser.parseUser(ctx.event().getClient(), arg);
    }

    /**
     * Asynchronously transforms the given string argument to a target type. The transformation may be adapted to the
     * command context.
     *
     * @param ctx the command context
     * @param arg the argument to transform
     * @return a {@link Mono} emitting the result of the transformation
     */
    Mono<? extends T> map(CommandContext ctx, String arg);
}
