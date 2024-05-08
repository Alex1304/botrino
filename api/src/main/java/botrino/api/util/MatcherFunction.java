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

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Predicate;

/**
 * Utility class to perform pattern matching. It can be useful for example when implementing {@link BotrinoExtension} to
 * match the actual type of the classes and services discovered, or when implementing error handlers to match the actual
 * subclass of Throwable.
 *
 * <p>
 * {@link MatcherFunction} instances are mutable and not thread safe. They are not intended to be reused, but rather
 * written inline inside the implementation of your methods.
 *
 * <p>
 * This variant is suited for executing code that returns a value. If the code doesn't return a value, use
 * {@link MatcherConsumer} instead.
 *
 * @param <R> the return type of the matcher
 */
public final class MatcherFunction<R> implements Function<Object, Optional<R>> {

    private final List<MatchStatement<R>> matchStatements = new ArrayList<>();

    private MatcherFunction() {
    }

    /**
     * Initializes an empty {@link MatcherFunction}.
     *
     * @param <R> the return type of the matcher
     * @return a new {@link MatcherFunction}
     */
    public static <R> MatcherFunction<R> create() {
        return new MatcherFunction<>();
    }

    /**
     * Appends a match statement to this matcher. If the object matches the predicate, {@link #apply(Object)} will
     * return the result of this function. If not, the object will be passed to the next statement, and so on.
     *
     * @param predicate the predicate that the object should match
     * @param function  the function that returns the desired result if this statement is matched
     * @return this matcher
     */
    public MatcherFunction<R> match(Predicate<Object> predicate, Function<Object, ? extends R> function) {
        Objects.requireNonNull(predicate);
        Objects.requireNonNull(function);
        matchStatements.add(new MatchStatement<>(predicate, function));
        return this;
    }

    /**
     * Specialization of {@link #match(Predicate, Function)} when the predicate consists of testing if the object is an
     * instance of a certain type. This method allows for type safety in the matcher function.
     *
     * @param type     the type the object should be an instance of
     * @param function the function that returns the desired result if the object is an instance of the given type
     * @param <U>      the target type of the object
     * @return this matcher
     */
    @SuppressWarnings("unchecked")
    public <U> MatcherFunction<R> matchType(Class<U> type, Function<? super U, ? extends R> function) {
        return match(type::isInstance, o -> function.apply((U) o));
    }

    /**
     * Specialization of {@link #matchType(Class, Function)} that accepts an extra predicate to test after the type is
     * matched.
     *
     * @param type      the type the object should be an instance of
     * @param predicate the predicate to test after the type is matched
     * @param function  the function that returns the desired result if the object is an instance of the given type AND
     *                  if the given predicate matches as well
     * @param <U>       the target type of the object
     * @return this matcher
     */
    @SuppressWarnings("unchecked")
    public <U> MatcherFunction<R> matchType(Class<U> type, Predicate<? super U> predicate,
                                            Function<? super U, ? extends R> function) {
        return match(o -> type.isInstance(o) && predicate.test(type.cast(o)), o -> function.apply((U) o));
    }

    /**
     * Applies this matcher to the given object. It will check each match statement in the order they were declared, and
     * will return the result of the first match. If no statement is matched, this method returns
     * {@link Optional#empty()}.
     *
     * @param obj the object to apply this matcher on
     * @return an {@link Optional} that returns the result of the first matched statement, or empty if no statement were
     * matched.
     */
    @Override
    public Optional<R> apply(Object obj) {
        for (var statement : matchStatements) {
            if (statement.predicate.test(obj)) {
                return Optional.of(statement.function.apply(obj));
            }
        }
        return Optional.empty();
    }

    private record MatchStatement<R>(Predicate<Object> predicate, Function<Object, ? extends R> function) {
    }
}
