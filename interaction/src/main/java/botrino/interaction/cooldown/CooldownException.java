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
package botrino.interaction.cooldown;

import botrino.api.util.DurationUtils;

import java.time.Duration;

/**
 * Thrown when an action is on cooldown.
 */
public class CooldownException extends RuntimeException {

    private final long permits;
    private final Duration resetInterval;
    private final Duration retryAfter;

    public CooldownException(long permits, Duration resetInterval, Duration retryAfter) {
        super("Action on cooldown. Retry after: " + DurationUtils.format(retryAfter));
        this.permits = permits;
        this.resetInterval = resetInterval;
        this.retryAfter = retryAfter;
    }

    /**
     * Gets the original number of permits of the cooldown.
     *
     * @return the permits
     */
    public final long getPermits() {
        return permits;
    }

    /**
     * Gets the original reset interval of the cooldown.
     *
     * @return the reset interval
     */
    public final Duration getResetInterval() {
        return resetInterval;
    }

    /**
     * Gets the duration after which it should be safe to execute the action again.
     *
     * @return a {@link Duration}
     */
    public final Duration getRetryAfter() {
        return retryAfter;
    }
}
