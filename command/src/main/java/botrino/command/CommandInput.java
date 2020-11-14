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
package botrino.command;

import java.util.*;

/**
 * Represents the tokenized input of a command sent in chat.
 */
public final class CommandInput {

    private final String raw;
    private final String prefix;
    private final ArrayDeque<String> args;
    private final Map<String, String> flagMap;

    CommandInput(String raw, String prefix, ArrayDeque<String> args, Map<String, String> flagMap) {
        this.raw = raw;
        this.prefix = prefix;
        this.args = args;
        this.flagMap = flagMap;
    }

    /**
     * Gets the raw, non-tokenized input of the command.
     *
     * @return the raw input
     */
    public String getRaw() {
        return raw;
    }

    /**
     * Gets the prefix used in the original input.
     *
     * @return the prefix
     */
    public String getPrefix() {
        return prefix;
    }

    /**
     * Gets the arguments of the command.
     *
     * @return the arguments
     */
    public List<String> getArguments() {
        return List.copyOf(args);
    }

    /**
     * Gets the argument of the command, with the size of the list adjusted to an arbitrary count.
     *
     * <ul>
     * <li>If args.size() == count, directly returns the arguments</li>
     * <li>If args.size() &gt; count, merges the last arguments together until args.size() == count.</li>
     * <li>If args.size() &lt; count, pads with nulls until args.size() == count</li>
     * </ul>
     *
     * @param expectedCount the expected argument count
     * @return the adjusted list of arguments
     */
    public List<String> getArguments(int expectedCount) {
        var args = List.copyOf(this.args);
        if (args.size() == expectedCount) {
            return args;
        }
        if (args.size() > expectedCount) {
            var mergedTokens = new ArrayDeque<>(args);
            while (mergedTokens.size() > 1 && mergedTokens.size() > expectedCount) {
                var lastArg = mergedTokens.removeLast();
                var beforeLastArg = mergedTokens.removeLast();
                mergedTokens.addLast(beforeLastArg + " " + lastArg);
            }
            return List.copyOf(mergedTokens);
        }
        var padded = new ArrayList<>(args);
        while (padded.size() < expectedCount) {
            padded.add(null);
        }
        return List.copyOf(padded);
    }

    ArrayDeque<String> getMutableArgs() {
        return args;
    }

    /**
     * Gets the value of the flag with the given name. If the flag has no value, the value is an empty string. If the
     * flag is not present at all, and empty Optional is returned.
     *
     * @param flagName the name of the flag to look for
     * @return the value of the flag, or empty optional if flag is not present
     */
    public Optional<String> getFlag(String flagName) {
        return Optional.ofNullable(flagMap.get(flagName));
    }

    @Override
    public String toString() {
        return "CommandInput{" +
                "raw='" + raw + '\'' +
                ", prefix='" + prefix + '\'' +
                ", args=" + args +
                ", flagMap=" + flagMap +
                '}';
    }
}
