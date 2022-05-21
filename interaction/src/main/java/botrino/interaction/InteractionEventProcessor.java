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
package botrino.interaction;

import discord4j.core.event.domain.interaction.InteractionCreateEvent;
import discord4j.core.object.command.Interaction;
import reactor.core.publisher.Mono;

import java.util.Locale;

/**
 * Allows to perform some preliminary processing on the interaction events received from the interaction service
 * subscription. This includes filtering events early, and determining the locale adapted to the event.
 */
public interface InteractionEventProcessor {

    /**
     * A {@link InteractionEventProcessor} that does not filter any events and that always uses default locale.
     */
    InteractionEventProcessor NO_OP = new InteractionEventProcessor() {
        @Override
        public String toString() {
            return "InteractionEventProcessor.NO_OP";
        }
    };

    /**
     * Allows to ignore events in some arbitrary situations. If the filter doesn't pass, the event is effectively
     * dropped and no listener will be executed.
     *
     * @param event the event to filter
     * @return a {@link Mono} emitting {@code true} if the event should be accepted, and either {@code false} or empty
     * if the event should be dropped. If an error occurs, it will be logged then the event will be dropped.
     */
    default Mono<Boolean> filter(InteractionCreateEvent event) {
        return Mono.just(true);
    }

    /**
     * Determines the locale to use for interactions following the given event. By default, it applies the locale of the
     * user as returned by {@link Interaction#getUserLocale()}
     *
     * @param event the event to find the locale for
     * @return a {@link Mono} emitting the locale appropriate for the event. Empty will use the default locale as
     * returned by {@link Locale#getDefault()}. If an error occurs, it will be logged then the event will be dropped.
     */
    default Mono<Locale> computeLocale(InteractionCreateEvent event) {
        return Mono.just(Locale.forLanguageTag(event.getInteraction().getUserLocale()));
    }
}
