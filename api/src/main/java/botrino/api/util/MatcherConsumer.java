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
package botrino.api.util;

import botrino.api.extension.BotrinoExtension;
import reactor.util.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Predicate;

/**
 * Utility class to perform pattern matching. It can be useful for example when implementing {@link BotrinoExtension} to
 * match the actual type of the classes and services discovered, or when implementing error handlers to match the actual
 * subclass of Throwable.
 *
 * <p>
 * {@link MatcherConsumer} instances are mutable and not thread safe. They are not intended to be reused, but rather
 * written inline inside the implementation of your methods.
 *
 * <p>
 * This variant is suited for executing code that doesn't return a value. If the code returns a value, use
 * {@link MatcherFunction} instead.
 */
public final class MatcherConsumer implements Consumer<Object> {

    private final List<MatchStatement> matchStatements = new ArrayList<>();
    private Consumer<Object> fallback;
    private boolean allowMultipleMatches;

    private MatcherConsumer() {
    }

    /**
     * Initializes an empty {@link MatcherConsumer}.
     *
     * @return a new {@link MatcherConsumer}
     */
    public static MatcherConsumer create() {
        return new MatcherConsumer();
    }

    /**
     * Appends a match statement to this matcher. If the object matches the predicate, {@link #accept(Object)} will
     * execute the given consumer. If not, the object will be passed to the next statement, and so on.
     *
     * @param predicate the predicate that the object should match
     * @param consumer  the consumer to execute if this statement is matched
     * @return this matcher
     */
    public MatcherConsumer match(Predicate<Object> predicate, Consumer<Object> consumer) {
        Objects.requireNonNull(predicate);
        Objects.requireNonNull(consumer);
        matchStatements.add(new MatchStatement(predicate, consumer));
        return this;
    }

    /**
     * Specialization of {@link #match(Predicate, Consumer)} when the predicate consists of testing if the object is an
     * instance of a certain type. This method allows for type safety in the matcher consumer.
     *
     * @param type     the type the object should be an instance of
     * @param consumer the consumer to execute if the object is an instance of the given type
     * @param <U>      the target type of the object
     * @return this matcher
     */
    @SuppressWarnings("unchecked")
    public <U> MatcherConsumer matchType(Class<U> type, Consumer<? super U> consumer) {
        return match(type::isInstance, o -> consumer.accept((U) o));
    }

    /**
     * Specialization of {@link #matchType(Class, Consumer)} that accepts an extra predicate to test after the type is
     * matched.
     *
     * @param type      the type the object should be an instance of
     * @param predicate the predicate to test after the type is matched
     * @param consumer  the consumer to execute if the object is an instance of the given type AND if the given
     *                  predicate matches as well
     * @param <U>       the target type of the object
     * @return this matcher
     */
    @SuppressWarnings("unchecked")
    public <U> MatcherConsumer matchType(Class<U> type, Predicate<? super U> predicate, Consumer<? super U> consumer) {
        return match(o -> type.isInstance(o) && predicate.test(type.cast(o)), o -> consumer.accept((U) o));
    }

    /**
     * Sets a fallback consumer to execute in case no statement is matched.
     *
     * @param fallback the fallback consumer to set, or null for no-op
     * @return this matcher
     */
    public MatcherConsumer fallback(@Nullable Consumer<Object> fallback) {
        this.fallback = fallback;
        return this;
    }

    /**
     * Sets whether to allow multiple matches. If set to true, all statements that matches their predicate will be
     * executed. If false, only the first match will be executed, which is the default behavior to be consistent with
     * {@link MatcherFunction}. Note that if set to true, any consumer throwing an exception will prevent following ones
     * from being executed even if they match.
     *
     * @param allowMultipleMatches whether to allow multiple matches
     * @return this matcher
     */
    public MatcherConsumer allowMultipleMatches(boolean allowMultipleMatches) {
        this.allowMultipleMatches = allowMultipleMatches;
        return this;
    }

    /**
     * Applies this matcher to the given object. It will check each match statement in the order they were declared, and
     * will execute the consumer of the first match. If no statement is matched, it will execute the one given via
     * {@link #fallback(Consumer)}, or no-op if not provided.
     *
     * @param obj the object to apply this matcher on
     */
    @Override
    public void accept(Object obj) {
        var matched = false;
        for (var statement : matchStatements) {
            if (statement.predicate.test(obj)) {
                matched = true;
                statement.consumer.accept(obj);
                if (!allowMultipleMatches) {
                    return;
                }
            }
        }
        if (!matched && fallback != null) {
            fallback.accept(obj);
        }
    }

    private record MatchStatement(Predicate<Object> predicate, Consumer<Object> consumer) {
    }
}
