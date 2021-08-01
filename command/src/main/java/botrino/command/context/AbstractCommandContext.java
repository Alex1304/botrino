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
package botrino.command.context;

import botrino.command.CommandService;
import botrino.command.RetryableInteractionException;
import botrino.command.InteractionCommand;
import discord4j.core.event.domain.Event;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Sinks;
import reactor.util.retry.Retry;

import java.util.Locale;
import java.util.function.Function;

abstract class AbstractCommandContext<E extends Event> implements CommandContext {

    private final CommandService commandService;
    private final Locale locale;
    private final E event;

    public AbstractCommandContext(CommandService commandService, Locale locale, E event) {
        this.commandService = commandService;
        this.locale = locale;
        this.event = event;
    }

    @Override
    public final Locale getLocale() {
        return locale;
    }

    @Override
    public final <T> Mono<T> awaitInteraction(Function<? super Sinks.One<T>, ? extends InteractionCommand> interaction) {
        return Mono.defer(() -> {
            final var sink = Sinks.<T>one();
            final var command = interaction.apply(sink);
            command.registerAsInteraction(commandService, this);
            return sink.asMono();
        }).retryWhen(Retry.indefinitely().filter(RetryableInteractionException.class::isInstance));
    }

    public final E event() {
        return event;
    }
}
