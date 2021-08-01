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

import botrino.api.config.object.I18nConfig;
import botrino.command.config.CommandConfig;
import discord4j.core.event.domain.Event;
import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.object.entity.User;
import discord4j.core.object.entity.channel.MessageChannel;
import reactor.core.publisher.Mono;

import java.util.Locale;

/**
 * Allows to perform some preliminary processing on the message create events received from the command service
 * subscription. This includes filtering events early, and determining the prefix and the locale adapted to the event.
 */
public interface CommandEventProcessor {

    /**
     * A {@link CommandEventProcessor} that only filters bot accounts and that always uses the default prefix and
     * locale.
     */
    CommandEventProcessor NO_OP = new CommandEventProcessor() {
        @Override
        public String toString() {
            return "CommandEventProcessor.NO_OP";
        }
    };

    /**
     * Allows to ignore events in some arbitrary situations. If the filter doesn't pass, the event is effectively
     * dropped and no command will be executed.
     *
     * @param event the event to filter
     * @param channel the channel where the event comes from
     * @param user the user who triggered the event
     * @return a {@link Mono} emitting {@code true} if the event should be accepted, and either {@code false} or empty
     * if the event should be dropped. If an error occurs, it will be logged then the context will be dropped.
     */
    default Mono<Boolean> filter(Event event, MessageChannel channel, User user) {
        return Mono.just(true);
    }

    /**
     * Determines the prefix to use in order for the given message event to be recognized as a command. By default, it
     * completes empty which indicates to use the default prefix as defined by {@link CommandConfig#prefix()}.
     *
     * @param event the event to find the prefix for
     * @return a {@link Mono} emitting the prefix appropriate for the message event. Empty will use the default prefix.
     * If an error occurs, it will be logged then the event will be dropped.
     */
    default Mono<String> prefixForMessageEvent(MessageCreateEvent event) {
        return Mono.empty();
    }

    /**
     * Determines the locale to use for interactions following the given event. By default, it completes empty which
     * indicates to use the default locale as defined by {@link I18nConfig#defaultLocale()}.
     *
     * @param event the event to find the locale for
     * @param channel the channel where the event comes from
     * @param user the user who triggered the event
     * @return a {@link Mono} emitting the locale appropriate for the event. Empty will use the default locale. If an
     * error occurs, it will be logged then the event will be dropped.
     */
    default Mono<Locale> localeForEvent(Event event, MessageChannel channel, User user) {
        return Mono.empty();
    }
}
