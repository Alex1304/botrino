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
package botrino.interaction.util;

import botrino.interaction.config.InteractionConfig;
import botrino.interaction.context.InteractionContext;
import discord4j.core.object.component.Button;
import discord4j.core.object.entity.Message;
import discord4j.core.spec.MessageCreateSpec;
import reactor.core.publisher.Mono;
import reactor.util.Logger;
import reactor.util.Loggers;

import java.security.SecureRandom;
import java.util.Objects;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;

import static botrino.api.util.MessageUtils.toFollowupCreateSpec;
import static botrino.api.util.MessageUtils.toReplyEditSpec;
import static botrino.interaction.listener.ComponentInteractionListener.button;

/**
 * Utility class to create messages with a pagination system.
 */
public final class MessagePaginator {

    private static final Logger LOGGER = Loggers.getLogger(MessagePaginator.class);

    private static final String PREVIOUS_ID = "_previous";
    private static final String NEXT_ID = "_next";
    private static final String CLOSE_ID = "_close";
    private static final SecureRandom RANDOM = new SecureRandom();

    /**
     * Sends a message that can be interacted with in order to navigate through pages. It starts by sending the message
     * on the page number <code>0</code>, then updates the message automatically when the page changes or when the
     * paginator closes. It closes when the "close" button is pressed or when the timeout is reached. The timeout is the
     * one defined in {@link InteractionConfig#awaitComponentTimeoutSeconds()}.
     *
     * @param ctx              the interaction context
     * @param pageCount        the total number of pages, used as a hint to lock the "next page" button on last page
     * @param messageGenerator a function that accepts the current state of the paginator (current page, whether it's
     *                         active, etc) and returns a Mono emitting the spec of the message to send.
     * @return a Mono that completes when the paginator closes.
     */
    public static Mono<Void> paginate(InteractionContext ctx, int pageCount,
                                      Function<? super State, ? extends Mono<MessageCreateSpec>> messageGenerator) {
        return paginate(ctx, 0, pageCount, messageGenerator);
    }

    /**
     * Sends a message that can be interacted with in order to navigate through pages. It starts by sending the message
     * on the page number <code>initialPage</code>, then updates the message automatically when the page changes or when
     * the paginator closes. It closes when the "close" button is pressed or when the timeout is reached. The timeout is
     * the one defined in {@link InteractionConfig#awaitComponentTimeoutSeconds()}.
     *
     * @param ctx              the interaction context
     * @param initialPage      the page number that will be sent first. Page numbers start at 0 and end at
     *                         <code>pageCount - 1</code>.
     * @param pageCount        the total number of pages, used as a hint to lock the "next page" button on last page
     * @param messageGenerator a function that accepts the current state of the paginator (current page, whether it's
     *                         active, etc) and returns a Mono emitting the spec of the message to send.
     * @return a Mono that completes when the paginator closes.
     */
    public static Mono<Void> paginate(InteractionContext ctx, int initialPage, int pageCount,
                                      Function<? super State, ? extends Mono<MessageCreateSpec>> messageGenerator) {
        Objects.checkIndex(initialPage, pageCount);
        return Mono.defer(() -> {
            final var baseCustomId = Integer.toHexString(RANDOM.nextInt());
            LOGGER.debug("Starting paginator {}", baseCustomId);
            final var currentPage = new AtomicInteger();
            final var active = new AtomicBoolean(true);
            final var previous = button(baseCustomId + PREVIOUS_ID, btnCtx -> {
                if (!active.get()) {
                    return Mono.error(new IllegalStateException("inactive paginator"));
                }
                final var newPage = currentPage.decrementAndGet();
                if (newPage < 0) {
                    return Mono.error(new IllegalStateException("newPage < 0"));
                }
                return messageGenerator.apply(new State(newPage, pageCount, true, baseCustomId));
            });
            final var next = button(baseCustomId + NEXT_ID, btnCtx -> {
                if (!active.get()) {
                    return Mono.error(new IllegalStateException("inactive paginator"));
                }
                final var newPage = currentPage.incrementAndGet();
                if (newPage >= pageCount) {
                    return Mono.error(new IllegalStateException("newPage >= pageCount"));
                }
                return messageGenerator.apply(new State(newPage, pageCount, true, baseCustomId));
            });
            final var close = button(baseCustomId + CLOSE_ID, btnCtx -> {
                if (!active.compareAndSet(true, false)) {
                    return Mono.error(new IllegalStateException("inactive paginator"));
                }
                return messageGenerator.apply(new State(currentPage.get(), pageCount, false, baseCustomId));
            });
            return messageGenerator.apply(new State(initialPage, pageCount, true, baseCustomId))
                    .flatMap(message -> ctx.event().createFollowup(toFollowupCreateSpec(message)))
                    .map(Message::getId)
                    .flatMap(messageId -> Mono.firstWithValue(
                                    ctx.awaitComponentInteraction(previous),
                                    ctx.awaitComponentInteraction(next),
                                    ctx.awaitComponentInteraction(close))
                            .flatMap(message -> ctx.event().editFollowup(messageId, toReplyEditSpec(message)))
                            .repeat(active::get)
                            .timeout(ctx.getAwaitComponentTimeout())
                            .onErrorResume(TimeoutException.class, e -> Mono.fromRunnable(
                                    () -> LOGGER.debug("Paginator {} timed out", baseCustomId)))
                            .doOnNext(__ -> LOGGER.debug("Paginator {} terminated with success", baseCustomId))
                            .doOnError(e -> LOGGER.error("Paginator " + baseCustomId + " terminated with an error", e))
                            .doOnCancel(() -> LOGGER.debug("Paginator {} cancelled", baseCustomId))
                            .doFinally(signal -> messageGenerator
                                    .apply(new State(currentPage.get(), pageCount, false, baseCustomId))
                                    .flatMap(message -> ctx.event().editReply(toReplyEditSpec(message)))
                                    .subscribe(null, e -> LOGGER
                                            .error("Error in doFinally of paginator " + baseCustomId, e)))
                            .then());
        });
    }

    /**
     * Holds the state of a paginator.
     */
    public static final class State {

        private final int page;
        private final int pageCount;
        private final boolean active;
        private final String baseCustomId;

        private State(int page, int pageCount, boolean active, String baseCustomId) {
            this.page = page;
            this.pageCount = pageCount;
            this.active = active;
            this.baseCustomId = baseCustomId;
        }

        /**
         * Gets the current page number. First page is 0, last page is {@link #getPageCount()} - 1.
         *
         * @return the current page
         */
        public int getPage() {
            return page;
        }

        /**
         * Gets the total number of pages.
         *
         * @return the page count
         */
        public int getPageCount() {
            return pageCount;
        }

        /**
         * Gets whether the paginator is active. If false, it means the paginator has closed either by user action or by
         * timeout.
         *
         * @return a boolean
         */
        public boolean isActive() {
            return active;
        }

        /**
         * Builds a button that goes to previous page when pressed. The custom ID is provided through the function, you
         * are in charge of constructing the {@link Button} instance out of it. The paginator will control whether it's
         * disabled or not according to this state.
         *
         * @param buttonFactory a function that accepts a custom ID and that returns a {@link Button}
         * @return a new {@link Button}
         */
        public Button previousButton(Function<String, Button> buttonFactory) {
            return buttonFactory.apply(baseCustomId + PREVIOUS_ID).disabled(!active || page == 0);
        }

        /**
         * Builds a button that goes to next page when pressed. The custom ID is provided through the function, you are
         * in charge of constructing the {@link Button} instance out of it. The paginator will control whether it's
         * disabled or not according to this state.
         *
         * @param buttonFactory a function that accepts a custom ID and that returns a {@link Button}
         * @return a new {@link Button}
         */
        public Button nextButton(Function<String, Button> buttonFactory) {
            return buttonFactory.apply(baseCustomId + NEXT_ID).disabled(!active || page == pageCount - 1);
        }

        /**
         * Builds a button that closes the paginator when pressed. The custom ID is provided through the function, you
         * are in charge of constructing the {@link Button} instance out of it. The paginator will control whether it's
         * disabled or not according to this state.
         *
         * @param buttonFactory a function that accepts a custom ID and that returns a {@link Button}
         * @return a new {@link Button}
         */
        public Button closeButton(Function<String, Button> buttonFactory) {
            return buttonFactory.apply(baseCustomId + CLOSE_ID).disabled(!active);
        }
    }
}
