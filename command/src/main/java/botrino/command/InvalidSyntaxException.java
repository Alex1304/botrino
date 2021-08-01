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

import reactor.util.annotation.Nullable;

import java.util.Optional;

/**
 * Thrown when a user inputs bad arguments or a bad subcommand when running a command. It may carry additional
 * information on the error that happened when resolving the argument.
 */
public final class InvalidSyntaxException extends RuntimeException {

    private final String badArgumentName;
    private final String badArgumentValue;

    public InvalidSyntaxException(@Nullable String badArgumentName, @Nullable String badArgumentValue,
                                  @Nullable Throwable cause) {
        super(cause);
        this.badArgumentName = badArgumentName;
        this.badArgumentValue = badArgumentValue;
    }

    /**
     * Gets the name of the argument that was deemed as invalid. Empty optional means that the faulty argument is
     * actually a subcommand (missing or wrong subcommand).
     *
     * @return the bad argument name, or empty if it was a bad subcommand
     */
    public Optional<String> getBadArgumentName() {
        return Optional.ofNullable(badArgumentName);
    }

    /**
     * Gets the value of the argument that was deemed as invalid. Empty optional means that the argument was expected
     * but not specified by the user.
     *
     * @return the bad argument, if present
     */
    public Optional<String> getBadArgumentValue() {
        return Optional.ofNullable(badArgumentValue);
    }
}
