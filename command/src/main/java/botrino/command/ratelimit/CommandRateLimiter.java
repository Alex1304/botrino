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

import botrino.command.Command;

import java.time.Duration;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

public final class CommandRateLimiter {

    private final ConcurrentHashMap<CommandUser, RateLimitBucket> buckets = new ConcurrentHashMap<>();

    public void permit(long userId, Command command) {
        buckets.computeIfAbsent(new CommandUser(userId, command), k -> new RateLimitBucket(k.command.rateLimit()))
                .permit();
    }

    private final static class RateLimitBucket {

        private final Object lock = new Object();
        private final long[] permitHistory;
        private final RateLimit rateLimit;
        private int tail, head, count;

        private RateLimitBucket(RateLimit rateLimit) {
            this.permitHistory = new long[(int) Math.min(Integer.MAX_VALUE, rateLimit.getPermits())];
            this.rateLimit = rateLimit;
        }

        private void permit() {
            synchronized (lock) {
                var intervalNanos = rateLimit.getResetInterval().toNanos();
                var now = System.nanoTime();
                while (count > 0 && now - permitHistory[tail] > intervalNanos) {
                    tail = (tail + 1) % permitHistory.length;
                    count--;
                }
                if (count >= rateLimit.getPermits()) {
                    throw new RateLimitException(Duration.ofNanos(intervalNanos - now + permitHistory[tail]));
                }
                permitHistory[head] = now;
                head = (head + 1) % permitHistory.length;
                count++;
            }
        }
    }

    private static class CommandUser {

        private final long userId;
        private final Command command;

        public CommandUser(long userId, Command command) {
            this.userId = userId;
            this.command = command;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            CommandUser that = (CommandUser) o;
            return userId == that.userId && command == that.command;
        }

        @Override
        public int hashCode() {
            return Objects.hash(userId, command);
        }
    }
}
