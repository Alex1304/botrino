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

import botrino.command.context.CommandContext;
import discord4j.common.util.Snowflake;
import discord4j.core.object.entity.Member;
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
     * Returns a {@link Privilege} that denies everything. It will emit a generic {@link PrivilegeException}.
     *
     * @return a {@link Privilege}
     */
    public static Privilege denied() {
        return DENIED;
    }

    /**
     * Returns a {@link Privilege} that denies everything. It will emit the specified {@link PrivilegeException}
     * instance.
     *
     * @param exception a function specifying the {@link PrivilegeException} instance to emit in case of failure
     * @return a {@link Privilege}
     */
    public static Privilege denied(Function<? super CommandContext, ? extends PrivilegeException> exception) {
        return ctx -> Mono.error(() -> exception.apply(ctx));
    }

    /**
     * Builds a {@link Privilege} that performs a check against the user's effective permissions in the current channel.
     * If the permission check fails, the specified function will determine the {@link PrivilegeException} to emit. If
     * the failure is due to the privilege being checked outside of a guild or due to the inability to retrieve the
     * member, a generic {@link PrivilegeException} will be emitted instead.
     *
     * @param exception           a function specifying the {@link PrivilegeException} instance to emit in case of
     *                            failure
     * @param permissionPredicate the predicate that checks for permissions
     * @return a {@link Privilege}
     */
    public static Privilege checkPermissions(Function<? super CommandContext, ? extends PrivilegeException> exception,
                                             Predicate<? super PermissionSet> permissionPredicate) {
        return ctx -> Mono.just(ctx.channel())
                .ofType(GuildMessageChannel.class)
                .switchIfEmpty(Mono.error(PrivilegeException::new))
                .filterWhen(channel -> channel.getEffectivePermissions(ctx.user().getId())
                        .map(permissionPredicate::test))
                .switchIfEmpty(Mono.error(() -> exception.apply(ctx)))
                .then();
    }

    /**
     * Builds a {@link Privilege} that performs a check against the user's effective permissions in the current channel.
     * If the permission check fails, a generic {@link PrivilegeException} will be emitted. To customize the {@link
     * PrivilegeException} instance, use {@link #checkPermissions(Function, Predicate)} instead.
     *
     * @param permissionPredicate the predicate that checks for permissions
     * @return a {@link Privilege}
     */
    public static Privilege checkPermissions(Predicate<? super PermissionSet> permissionPredicate) {
        return checkPermissions(ctx -> new PrivilegeException(), permissionPredicate);
    }

    /**
     * Builds a {@link Privilege} that performs a check against the member's roles in the current guild. If the role
     * check fails, the specified function will determine the {@link PrivilegeException} to emit. If the failure is due
     * to the privilege being checked outside of a guild or due to the inability to retrieve the member, a generic
     * {@link PrivilegeException} will be emitted instead.
     *
     * @param exception     a function specifying the {@link PrivilegeException} instance to emit in case of failure
     * @param rolePredicate the predicate that checks for roles
     * @return a {@link Privilege}
     */
    public static Privilege checkRoles(Function<? super CommandContext, ? extends PrivilegeException> exception,
                                       Predicate<? super Set<Snowflake>> rolePredicate) {
        return ctx -> Mono.justOrEmpty(ctx.channel())
                .ofType(GuildMessageChannel.class)
                .flatMap(channel -> channel.getClient().getMemberById(channel.getGuildId(), ctx.user().getId()))
                .switchIfEmpty(Mono.error(PrivilegeException::new))
                .map(Member::getRoleIds)
                .filter(rolePredicate)
                .switchIfEmpty(Mono.error(() -> exception.apply(ctx)))
                .then();
    }

    /**
     * Builds a {@link Privilege} that performs a check against the member's roles in the current guild. If the role
     * check fails, a generic {@link PrivilegeException} will be emitted. To customize the {@link PrivilegeException}
     * instance, use {@link #checkRoles(Function, Predicate)} instead.
     *
     * @param rolePredicate the predicate that checks for roles
     * @return a {@link Privilege}
     */
    public static Privilege checkRoles(Predicate<? super Set<Snowflake>> rolePredicate) {
        return checkRoles(ctx -> new PrivilegeException(), rolePredicate);
    }

    /**
     * Builds a {@link Privilege} that checks if the author is the owner of the current guild. If the check fails, the
     * specified function will determine the {@link PrivilegeException} to emit. If the failure is due to the privilege
     * being checked outside of a guild or due to the inability to retrieve the guild, a generic {@link
     * PrivilegeException} will be emitted instead.
     *
     * @param exception a function specifying the {@link PrivilegeException} instance to emit in case of failure
     * @return a {@link Privilege}
     */
    public static Privilege guildOwner(Function<? super CommandContext, ? extends PrivilegeException> exception) {
        return ctx -> Mono.justOrEmpty(ctx.channel())
                .ofType(GuildMessageChannel.class)
                .flatMap(channel -> channel.getClient().getMemberById(channel.getGuildId(), ctx.user().getId()))
                .switchIfEmpty(Mono.error(PrivilegeException::new))
                .filterWhen(member -> member.getGuild()
                        .map(guild -> guild.getOwnerId().equals(member.getId()))
                        .switchIfEmpty(Mono.error(PrivilegeException::new)))
                .switchIfEmpty(Mono.error(() -> exception.apply(ctx)))
                .then();
    }

    /**
     * Builds a {@link Privilege} that checks if the author is the owner of the current guild. If the check fails, a
     * generic {@link PrivilegeException} will be emitted. To customize the {@link PrivilegeException} instance, use
     * {@link #guildOwner(Function)} instead.
     *
     * @return a {@link Privilege}
     */
    public static Privilege guildOwner() {
        return guildOwner(ctx -> new PrivilegeException());
    }
}
