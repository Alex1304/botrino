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

/**
 * Thrown when the rate limit for a certain action has been broken.
 */
public final class RateLimitException extends RuntimeException {

    private final RateLimit originalRateLimit;
    private final Duration retryAfter;

    public RateLimitException(RateLimit originalRateLimit, Duration retryAfter) {
        super("Rate limit of " + originalRateLimit + " reached. Retry after: " + DurationUtils.format(retryAfter));
        this.originalRateLimit = originalRateLimit;
        this.retryAfter = retryAfter;
    }

    /**
     * Gets the original rate limit that was applied to the command.
     *
     * @return the original rate limit
     */
    public RateLimit getOriginalRateLimit() {
        return originalRateLimit;
    }

    /**
     * Gets the duration after which it should be safe to execute the action again.
     *
     * @return a {@link Duration}
     */
    public Duration getRetryAfter() {
        return retryAfter;
    }
}
