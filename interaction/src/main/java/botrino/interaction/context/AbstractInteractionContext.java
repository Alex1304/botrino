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
package botrino.interaction.context;

import botrino.interaction.InteractionService;
import botrino.interaction.RetryableInteractionException;
import botrino.interaction.annotation.Acknowledge;
import botrino.interaction.listener.ComponentInteractionListener;
import discord4j.core.event.domain.interaction.InteractionCreateEvent;
import discord4j.core.object.entity.User;
import discord4j.core.object.entity.channel.MessageChannel;
import org.reactivestreams.Publisher;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Sinks;
import reactor.util.retry.Retry;

import java.time.Duration;
import java.util.Locale;

abstract class AbstractInteractionContext<E extends InteractionCreateEvent> implements InteractionContext {

    private final InteractionService interactionService;
    private final Locale locale;
    private final E event;
    private final MessageChannel channel;

    public AbstractInteractionContext(InteractionService interactionService, Locale locale, E event,
                                      MessageChannel channel) {
        this.interactionService = interactionService;
        this.locale = locale;
        this.event = event;
        this.channel = channel;
    }

    @Override
    public final Locale getLocale() {
        return locale;
    }

    @Override
    public final <R> Mono<R> awaitComponentInteraction(Acknowledge.Mode ack,
                                                       ComponentInteractionListener<R> componentInteraction) {
        return Mono.defer(() -> {
            final var sink = Sinks.<R>one();
            ComponentInteractionProxy<R> proxy;
            switch (ack) {
                case NONE:
                    proxy = new NoAckComponentInteractionProxy<>(componentInteraction, sink);
                    break;
                case DEFER:
                    proxy = new AckComponentInteractionProxy<>(componentInteraction, sink);
                    break;
                case DEFER_EPHEMERAL:
                    proxy = new AckEphemeralComponentInteractionProxy<>(componentInteraction, sink);
                    break;
                default:
                    proxy = new ComponentInteractionProxy<>(componentInteraction, sink);
            }
            interactionService.registerSingleUseComponentListener(proxy, this);
            return sink.asMono();
        }).retryWhen(Retry.indefinitely().filter(RetryableInteractionException.class::isInstance))
                .timeout(interactionService.getAwaitComponentTimeout());
    }

    @Override
    public E event() {
        return event;
    }

    @Override
    public final MessageChannel channel() {
        return channel;
    }

    @Override
    public final User user() {
        return event().getInteraction().getUser();
    }

    @Override
    public Duration getAwaitComponentTimeout() {
        return interactionService.getAwaitComponentTimeout();
    }

    private static class ComponentInteractionProxy<R> implements ComponentInteractionListener<R> {

        private final ComponentInteractionListener<R> delegate;
        private final Sinks.One<R> sink;

        private ComponentInteractionProxy(ComponentInteractionListener<R> delegate, Sinks.One<R> sink) {
            this.delegate = delegate;
            this.sink = sink;
        }

        @Override
        public String customId() {
            return delegate.customId();
        }

        @Override
        public Publisher<R> run(ComponentInteractionContext ctx) {
            return Mono.from(delegate.run(ctx))
                    .doOnSuccess(value -> {
                        if (value == null) {
                            sink.emitEmpty(Sinks.EmitFailureHandler.FAIL_FAST);
                        } else {
                            sink.emitValue(value, Sinks.EmitFailureHandler.FAIL_FAST);
                        }
                    })
                    .doOnError(t -> sink.emitError(t, Sinks.EmitFailureHandler.FAIL_FAST));
        }

        @Override
        public String toString() {
            return "ComponentInteractionProxy{" +
                    "delegate=" + delegate +
                    '}';
        }
    }

    @Acknowledge(Acknowledge.Mode.DEFER)
    private final static class AckComponentInteractionProxy<R> extends ComponentInteractionProxy<R> {

        private AckComponentInteractionProxy(ComponentInteractionListener<R> delegate, Sinks.One<R> sink) {
            super(delegate, sink);
        }
    }

    @Acknowledge(Acknowledge.Mode.NONE)
    private final static class NoAckComponentInteractionProxy<R> extends ComponentInteractionProxy<R> {

        private NoAckComponentInteractionProxy(ComponentInteractionListener<R> delegate, Sinks.One<R> sink) {
            super(delegate, sink);
        }
    }

    @Acknowledge(Acknowledge.Mode.DEFER_EPHEMERAL)
    private final static class AckEphemeralComponentInteractionProxy<R> extends ComponentInteractionProxy<R> {

        private AckEphemeralComponentInteractionProxy(ComponentInteractionListener<R> delegate, Sinks.One<R> sink) {
            super(delegate, sink);
        }
    }
}
