/*
 * This file is part of the Botrino project and is licensed under the MIT license.
 *
 * Copyright (c) 2021 Alexandre Miranda
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

import botrino.command.annotation.Alias;
import botrino.command.context.MessageCommandContext;
import reactor.core.publisher.Mono;

import java.util.Set;

public interface MessageCommand extends Command {

    Mono<Void> run(MessageCommandContext ctx);

    /**
     * Defines the aliases for this command.
     *
     * @return the set of aliases. If empty, the command will not be registered.
     */
    default Set<String> aliases() {
        var topLevelAnnot = getClass().getAnnotation(Alias.class);
        if (topLevelAnnot != null) {
            return Set.of(topLevelAnnot.value());
        }
        return Set.of();
    }

    /**
     * Defines the scope of this command.
     *
     * @return the scope
     */
    default Scope scope() {
        return Scope.ANYWHERE;
    }

    /**
     * Defines the subcommands for this command.
     *
     * @return the subcommands, may be empty
     */
    default Set<MessageCommand> subcommands() {
        return Set.of();
    }

    @Override
    default void register(CommandService commandService) {
        commandService.register(this);
    }
}
