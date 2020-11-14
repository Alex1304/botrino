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
package botrino.command.privilege;

import botrino.api.i18n.Translator;
import botrino.command.Scope;
import discord4j.common.util.Snowflake;
import discord4j.core.object.entity.Member;
import discord4j.core.object.entity.User;
import discord4j.core.object.entity.channel.GuildMessageChannel;
import discord4j.rest.util.PermissionSet;
import reactor.core.publisher.Mono;

import java.util.Set;
import java.util.function.Function;
import java.util.function.Predicate;

/**
 * Provides predefined factories for commonly used {@link Privilege privileges}.
 */
public final class Privileges {

    private static final Privilege ALLOWED = ctx -> Mono.empty();
    private static final Privilege DENIED = ctx -> Mono.error(new PrivilegeException());

    private Privileges() {
        throw new AssertionError();
    }

    /**
     * Returns a {@link Privilege} that allows everything.
     *
     * @return a {@link Privilege}
     */
    public static Privilege allowed() {
        return ALLOWED;
    }

    /**
     * Returns a {@link Privilege} that denies everything.
     *
     * @return a {@link Privilege}
     */
    public static Privilege denied() {
        return DENIED;
    }

    /**
     * Returns a {@link Privilege} that denies everything, with a custom message.
     *
     * @param deniedMessageFactory a function that builds the message to include in the {@link PrivilegeException}
     * @return a {@link Privilege}
     */
    public static Privilege denied(Function<? super Translator, String> deniedMessageFactory) {
        return ctx -> Mono.error(new PrivilegeException(deniedMessageFactory.apply(ctx)));
    }

    /**
     * Builds a {@link Privilege} that performs a check against the user's effective permissions in the current channel.
     * The check will always fail if inside a DM channel, so this is relevant for commands in {@link Scope#GUILD_ONLY}.
     *
     * @param deniedMessageFactory a function that builds the message to include in the {@link PrivilegeException} in
     *                             case of failure
     * @param permissionPredicate  the predicate that checks for permissions
     * @return a {@link Privilege}
     */
    public static Privilege checkPermissions(Function<? super Translator, String> deniedMessageFactory,
                                             Predicate<? super PermissionSet> permissionPredicate) {
        return ctx -> Mono.justOrEmpty(ctx.channel())
                .ofType(GuildMessageChannel.class)
                .filterWhen(channel -> Mono.justOrEmpty(ctx.event().getMessage().getAuthor())
                        .map(User::getId)
                        .flatMap(id -> channel.getEffectivePermissions(id).map(permissionPredicate::test)))
                .switchIfEmpty(Mono.error(() -> new PrivilegeException(deniedMessageFactory.apply(ctx))))
                .then();
    }

    /**
     * Builds a {@link Privilege} that performs a check against the member's roles in the current guild. The check will
     * always fail if inside a DM channel, so this is relevant for commands in {@link Scope#GUILD_ONLY}.
     *
     * @param deniedMessageFactory a function that builds the message to include in the {@link PrivilegeException} in
     *                             case of failure
     * @param rolePredicate        the predicate that checks for roles
     * @return a {@link Privilege}
     */
    public static Privilege checkRoles(Function<? super Translator, String> deniedMessageFactory,
                                       Predicate<? super Set<Snowflake>> rolePredicate) {
        return ctx -> Mono.justOrEmpty(ctx.event().getMember())
                .map(Member::getRoleIds)
                .filter(rolePredicate)
                .switchIfEmpty(Mono.error(() -> new PrivilegeException(deniedMessageFactory.apply(ctx))))
                .then();
    }
}
