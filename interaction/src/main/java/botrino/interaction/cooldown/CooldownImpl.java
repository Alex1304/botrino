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
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

final class CooldownImpl implements Cooldown {

    static final Cooldown UNBOUNDED = new CooldownImpl(1, Duration.ZERO);

    private final int totalPermits;
    private final Duration resetInterval;
    private final ConcurrentHashMap<Long, Bucket> buckets = new ConcurrentHashMap<>();

    CooldownImpl(int totalPermits, Duration resetInterval) {
        this.totalPermits = totalPermits;
        this.resetInterval = resetInterval;
    }

    @Override
    public void fire(long userId) {
        buckets.computeIfAbsent(userId, k -> new Bucket()).fire();
    }

    @Override
    public int getTotalPermits() {
        return totalPermits;
    }

    @Override
    public Duration getResetInterval() {
        return resetInterval;
    }

    @Override
    public Remaining remaining(long userId) {
        return buckets.computeIfAbsent(userId, k -> new Bucket()).remaining();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CooldownImpl cooldown = (CooldownImpl) o;
        return totalPermits == cooldown.totalPermits && resetInterval.equals(cooldown.resetInterval);
    }

    @Override
    public int hashCode() {
        return Objects.hash(totalPermits, resetInterval);
    }

    @Override
    public String toString() {
        return "Cooldown{" +
                "totalPermits=" + totalPermits +
                ", resetInterval=" + DurationUtils.format(resetInterval) +
                '}';
    }

    private final class Bucket {

        private final Object lock = new Object();
        private final long[] permitHistory;
        private int tail, head, count;

        private Bucket() {
            this.permitHistory = new long[totalPermits];
        }

        private void fire() {
            synchronized (lock) {
                var remaining = remaining();
                if (remaining.getRemainingPermits() == 0) {
                    throw new CooldownException(totalPermits, resetInterval, remaining.getTimeLeftBeforeNextPermit());
                }
                permitHistory[head] = System.nanoTime();
                head = (head + 1) % permitHistory.length;
                count++;
            }
        }

        private Remaining remaining() {
            synchronized (lock) {
                var intervalNanos = resetInterval.toNanos();
                var now = System.nanoTime();
                while (count > 0 && now - permitHistory[tail] > intervalNanos) {
                    tail = (tail + 1) % permitHistory.length;
                    count--;
                }
                var permitsRemaining = totalPermits - count;
                var timeLeft = permitsRemaining == totalPermits ? Duration.ZERO
                        : Duration.ofNanos(resetInterval.toNanos() - System.nanoTime() + permitHistory[tail]);
                return new Remaining(permitsRemaining, timeLeft);
            }
        }
    }
}
