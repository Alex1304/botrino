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
package botrino.command;

import botrino.command.privilege.PrivilegeException;
import reactor.core.publisher.Mono;

/**
 * Interface to implement in order to handle command errors.
 */
public interface CommandErrorHandler {

    /**
     * Recover from a {@link CommandFailedException}, for example by sending an informative message back to the user.
     *
     * @param e   the exception
     * @param ctx the context of the command that failed
     * @return a Mono completing when handling is done. Rethrowing an exception there will drop it and log it at error
     * level.
     */
    Mono<Void> handleCommandFailed(CommandFailedException e, CommandContext ctx);

    /**
     * Recover from a {@link PrivilegeException}, for example by sending a message back to the user saying they don't
     * have the permissions to use the command.
     *
     * @param e   the exception
     * @param ctx the context of the command that failed
     * @return a Mono completing when handling is done. Rethrowing an exception there will drop it and log it at error
     * level.
     */
    Mono<Void> handlePrivilege(PrivilegeException e, CommandContext ctx);

    /**
     * Recover from a {@link BadSubcommandException}, typically occurring when a user attempts to use a subcommand that
     * does not exist or is missing for a command.
     *
     * @param e   the exception
     * @param ctx the context of the command that failed
     * @return a Mono completing when handling is done. Rethrowing an exception there will drop it and log it at error
     * level.
     */
    Mono<Void> handleBadSubcommand(BadSubcommandException e, CommandContext ctx);

    /**
     * Recover from any {@link Throwable} that are not handled by other handlers, indicating that something went wrong
     * when executing the command.
     *
     * @param e   the exception
     * @param ctx the context of the command that failed
     * @return a Mono completing when handling is done. Rethrowing an exception there will drop it and log it at error
     * level.
     */
    Mono<Void> handleDefault(Throwable e, CommandContext ctx);
}
