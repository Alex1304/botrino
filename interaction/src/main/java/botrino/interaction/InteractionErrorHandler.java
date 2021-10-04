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
package botrino.interaction;

import botrino.interaction.context.InteractionContext;
import botrino.interaction.cooldown.CooldownException;
import botrino.interaction.privilege.PrivilegeException;
import org.reactivestreams.Publisher;
import reactor.core.publisher.Mono;

/**
 * Interface to implement in order to handle interaction errors.
 */
public interface InteractionErrorHandler {

    /**
     * A {@link InteractionErrorHandler} that rethrows all errors without doing anything.
     */
    InteractionErrorHandler NO_OP = new InteractionErrorHandler() {
        @Override
        public String toString() {
            return "InteractionErrorHandler.NO_OP";
        }
    };

    /**
     * Recover from a {@link InteractionFailedException}, for example by sending an informative message back to the
     * user.
     *
     * @param e   the exception
     * @param ctx the context of the interaction that failed
     * @return a Publisher completing when handling is done. Rethrowing an exception there will drop it and log it at
     * error level.
     */
    default Publisher<?> handleInteractionFailed(InteractionFailedException e, InteractionContext ctx) {
        return Mono.error(e);
    }

    /**
     * Recover from a {@link PrivilegeException}, for example by sending a message back to the user saying they don't
     * have the permissions to interact.
     *
     * @param e   the exception
     * @param ctx the context of the interaction that failed
     * @return a Publisher completing when handling is done. Rethrowing an exception there will drop it and log it at
     * error level.
     */
    default Publisher<?> handlePrivilege(PrivilegeException e, InteractionContext ctx) {
        return Mono.error(e);
    }

    /**
     * Recover from a {@link CooldownException}, typically occurring when a user breaks the cooldown for an
     * interaction.
     *
     * @param e   the exception
     * @param ctx the context of the interaction that failed
     * @return a Publisher completing when handling is done. Rethrowing an exception there will drop it and log it at
     * error level.
     */
    default Publisher<?> handleCooldown(CooldownException e, InteractionContext ctx) {
        return Mono.error(e);
    }

    /**
     * Recover from any {@link Throwable} that are not handled by other handlers, indicating that something went wrong
     * when handling the interaction.
     *
     * @param t   the throwable
     * @param ctx the context of the interaction that failed
     * @return a Publisher completing when handling is done. Rethrowing an exception there will drop it and log it at
     * error level.
     */
    default Publisher<?> handleDefault(Throwable t, InteractionContext ctx) {
        return Mono.error(t);
    }
}
