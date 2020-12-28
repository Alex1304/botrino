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
import reactor.core.publisher.Mono;

import java.util.function.BinaryOperator;

/**
 * Represents a requirement to fulfill in order to execute a command.
 */
@FunctionalInterface
public interface Privilege {

    private static Mono<PrivilegeException> evaluate(Privilege p, CommandContext ctx) {
        return p.checkGranted(ctx)
                .cast(PrivilegeException.class)
                .onErrorResume(PrivilegeException.class, Mono::just);
    }

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
     * Returns a {@link Privilege} that evaluates as granted only if both this privilege and the other one are granted.
     * This privilege is evaluated first, and if it fails, the other one is not tested. In case of failure, the
     * first exception will be forwarded.
     *
     * @param other the other privilege
     * @return a new {@link Privilege}
     */
    default Privilege and(Privilege other) {
        return ctx -> evaluate(this, ctx)
                .switchIfEmpty(evaluate(other, ctx))
                .flatMap(Mono::error);
    }

    /**
     * Returns a {@link Privilege} that evaluates as granted if at least one of this privilege or the other one is
     * granted. If this privilege is granted, the other one is not tested. If both privileges fail, the resulting {@link
     * PrivilegeException} will be determined by the given aggregator function.
     *
     * @param other               the other privilege
     * @param exceptionAggregator a function that determines the {@link PrivilegeException} to emit if both evaluations
     *                            fail, based on the two original exceptions
     * @return a new {@link Privilege}
     */
    default Privilege or(Privilege other, BinaryOperator<PrivilegeException> exceptionAggregator) {
        return ctx -> evaluate(this, ctx)
                .flatMap(ex1 -> evaluate(other, ctx)
                        .map(ex2 -> exceptionAggregator.apply(ex1, ex2))
                        .flatMap(Mono::error));
    }
}
