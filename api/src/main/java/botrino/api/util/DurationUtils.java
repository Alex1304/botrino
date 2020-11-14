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

import java.time.Duration;

/**
 * Utilities to manipulate Duration objects.
 */
public final class DurationUtils {

    private DurationUtils() {
        throw new AssertionError();
    }

    /**
     * Formats a Duration into a human readable String.
     *
     * @param time the duration to format
     * @return the formatted duration
     */
    public static String format(Duration time) {
        var result = (time.toDaysPart() > 0 ? time.toDaysPart() + "d " : "")
                + (time.toHoursPart() > 0 ? time.toHoursPart() + "h " : "")
                + (time.toMinutesPart() > 0 ? time.toMinutesPart() + "min " : "")
                + (time.toSecondsPart() > 0 ? time.toSecondsPart() + "s " : "")
                + (time.toMillisPart() > 0 ? time.toMillisPart() + "ms " : "");
        return result.isEmpty() ? "0ms" : result.substring(0, result.length() - 1);
    }
}
