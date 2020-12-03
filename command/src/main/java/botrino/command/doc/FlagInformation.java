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
package botrino.command.doc;

import reactor.util.annotation.Nullable;

import java.util.Objects;

/**
 * Holds information about a command flag.
 */
public final class FlagInformation {

    private final String valueFormat;
    private final String description;

    private FlagInformation(String valueFormat, String description) {
        this.valueFormat = Objects.requireNonNullElse(valueFormat, "");
        this.description = Objects.requireNonNullElse(description, "");
    }

    /**
     * Creates a new flag information builder.
     *
     * @return a new builder
     */
    public static Builder builder() {
        return new Builder();
    }

    /**
     * Gets a description of the value format.
     *
     * @return the value format
     */
    public String getValueFormat() {
        return valueFormat;
    }

    /**
     * Gets a description of what this flag does for the command.
     *
     * @return the description
     */
    public String getDescription() {
        return description;
    }

    public static class Builder {

        private String valueFormat;
        private String description;

        private Builder() {
        }

        /**
         * Sets the format of the value of the flag. Typically it indicates whether it expects a number, a string
         * value, etc
         *
         * @param valueFormat the value format, or null if not applicable
         * @return this builder
         */
        public Builder setValueFormat(@Nullable String valueFormat) {
            this.valueFormat = valueFormat;
            return this;
        }

        /**
         * Sets the description of the flag. Typically a one line text briefly explaining what the flag does.
         *
         * @param description the description of the flag, or null if not provided
         * @return this builder
         */
        public Builder setDescription(@Nullable String description) {
            this.description = description;
            return this;
        }

        /**
         * Builds a new {@link FlagInformation} based on the current state of this builder.
         *
         * @return a new {@link FlagInformation}
         */
        public FlagInformation build() {
            return new FlagInformation(valueFormat, description);
        }
    }
}
