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

import reactor.core.publisher.Mono;

import java.util.Set;

/**
 * Represents a bot command that is generally only used via its subcommands.
 */
public interface ParentCommand extends Command {

    /**
     * A {@link ParentCommand} by default throws an exception if it isn't used without a subcommand.
     *
     * @param ctx the context
     * @return A {@link Mono} emitting {@link BadSubcommandException} if the command is used without subcommand,
     * although this behavior can always be overriden for specific commands.
     */
    @Override
    default Mono<Void> run(CommandContext ctx) {
        return Mono.error(new BadSubcommandException(ctx.input().getArguments().stream().findFirst().orElse(null)));
    }

    /**
     * Gets the subcommands for this command. In the case of a {@link ParentCommand}, the returned set is typically
     * non-empty.
     *
     * @return a set of subcommands
     */
    @Override
    Set<Command> subcommands();
}
