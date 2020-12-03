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

import botrino.command.CommandContext;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

import static java.util.function.Predicate.not;

/**
 * Represents a requirement to fulfill in order to execute a command.
 */
@FunctionalInterface
public interface Privilege {

    /**
     * Checks if this privilege is granted according to the given context. If the privilege is not granted, it will
     * error with {@link PrivilegeException} possibly carrying details about the missing privilege.
     *
     * @param ctx the context to evaluate the privilege on
     * @return a {@link Mono} which completion indicates that the check was successful, or {@link PrivilegeException} if
     * not
     */
    Mono<Void> checkGranted(CommandContext ctx);

    /**
     * Checks if this privilege is granted according to the given context. Emits a boolean indicating whether the
     * privilege is granted or not. Use {@link #checkGranted(CommandContext)} if you need to access the details carried
     * by {@link PrivilegeException} about the missing privilege in case it is not granted.
     *
     * @param ctx the context to evaluate the privilege on
     * @return a {@link Mono} emitting true if granted, false if not. Any error occurring during the evaluation of the
     * privilege, except for {@link PrivilegeException}, will be forwarded as-is through the Mono.
     */
    default Mono<Boolean> isGranted(CommandContext ctx) {
        return checkGranted(ctx).thenReturn(true).onErrorReturn(PrivilegeException.class, false);
    }

    /**
     * Returns a {@link Privilege} that evaluates as granted only if both this privilege and the other one are
     * granted. Both privileges are evaluated concurrently, so that the resulting {@link PrivilegeException} will be a
     * concatenation of both reason lists if both evaluations fail.
     *
     * @param other the other privilege
     * @return a new {@link Privilege}
     */
    default Privilege and(Privilege other) {
        return ctx -> Flux.merge(evaluate(this, ctx), evaluate(other, ctx))
                .collectList()
                .filter(not(List::isEmpty))
                .flatMapMany(Flux::fromIterable)
                .flatMapIterable(PrivilegeException::getReasons)
                .collectList()
                .map(PrivilegeException::new)
                .flatMap(Mono::error)
                .then();
    }

    /**
     * Returns a {@link Privilege} that evaluates as granted if at least one of this privilege or the other one is
     * granted. Both privileges are evaluated concurrently, so that the resulting {@link PrivilegeException} will be a
     * concatenation of both reason lists if both evaluations fail.
     *
     * @param other the other privilege
     * @return a new {@link Privilege}
     */
    default Privilege or(Privilege other) {
        return ctx -> Flux.merge(evaluate(this, ctx), evaluate(other, ctx))
                .collectList()
                .filter(l -> l.size() == 2)
                .flatMapMany(Flux::fromIterable)
                .flatMapIterable(PrivilegeException::getReasons)
                .collectList()
                .map(PrivilegeException::new)
                .flatMap(Mono::error)
                .then();
    }

    private static Mono<PrivilegeException> evaluate(Privilege p, CommandContext ctx) {
        return p.checkGranted(ctx)
                .cast(PrivilegeException.class)
                .onErrorResume(PrivilegeException.class, Mono::just);
    }
}
