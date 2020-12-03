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
package botrino.command.ratelimit;

import botrino.api.util.DurationUtils;

import java.time.Duration;
import java.util.Objects;

/**
 * Represents the number of times an action can be executed within a specific time frame.
 */
public final class RateLimit {

    private static final RateLimit UNBOUNDED = new RateLimit(1L, Duration.ZERO);

    private final long permits;
    private final Duration resetInterval;

    private RateLimit(long permits, Duration resetInterval) {
        this.permits = permits;
        this.resetInterval = resetInterval;
    }

    /**
     * A {@link RateLimit} that gives unlimited permits.
     *
     * @return an unbounded {@link RateLimit}
     */
    public static RateLimit unbounded() {
        return UNBOUNDED;
    }

    /**
     * Creates a {@link RateLimit} with the given permits and reset interval values.
     *
     * @param permits       the number of times the action can be executed within the interval
     * @param resetInterval the interval after which the number of permits is reset for the action
     * @return a new {@link RateLimit}
     */
    public static RateLimit of(long permits, Duration resetInterval) {
        if (permits < 1) {
            throw new IllegalArgumentException("permits must be >= 1");
        }
        Objects.requireNonNull(resetInterval);
        return new RateLimit(permits, resetInterval);
    }

    /**
     * Gets the number of times the action can be executed within the interval.
     *
     * @return the permits
     */
    public long getPermits() {
        return permits;
    }

    /**
     * Gets the interval after which the number of permits is reset for the action.
     *
     * @return the reset interval
     */
    public Duration getResetInterval() {
        return resetInterval;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        RateLimit rateLimit = (RateLimit) o;
        return permits == rateLimit.permits && resetInterval.equals(rateLimit.resetInterval);
    }

    @Override
    public int hashCode() {
        return Objects.hash(permits, resetInterval);
    }

    @Override
    public String toString() {
        return "RateLimit{" +
                "permits=" + permits +
                ", resetInterval=" + DurationUtils.format(resetInterval) +
                '}';
    }
}
