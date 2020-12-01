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

import botrino.api.i18n.Translator;
import reactor.util.annotation.Nullable;

import java.util.*;

/**
 * Holds the documentation for a specific command.
 *
 * @see CommandDocumentation#builder(Locale)
 */
public final class CommandDocumentation {

    private static final CommandDocumentation EMPTY = builder(null).build();

    private final DocumentationLocaleAdapter docLocaleAdapter;
    private final String description;
    private final String syntax;
    private final String body;
    private final List<FlagInformation> flags;
    private final boolean unlisted;

    private CommandDocumentation(Translator translator, String description,
                                 String syntax, String body,
                                 List<FlagInformation> flags, boolean unlisted) {
        this.docLocaleAdapter = new DocumentationLocaleAdapter(
                Objects.requireNonNullElse(translator, Locale::getDefault));
        this.description = Objects.requireNonNullElse(description, "");
        this.syntax = Objects.requireNonNullElse(syntax, "");
        this.body = Objects.requireNonNullElse(body, "");
        this.flags = Collections.unmodifiableList(flags);
        this.unlisted = unlisted;
    }

    /**
     * Returns an empty documentation.
     *
     * @return an empty {@link CommandDocumentation}
     */
    public static CommandDocumentation empty() {
        return EMPTY;
    }

    /**
     * Creates a new command documentation builder, with the locale in which the documentation should be translated.
     *
     * @param locale the locale, or null to use default locale
     * @return a new builder
     */
    public static Builder builder(@Nullable Locale locale) {
        return new Builder(locale);
    }

    /**
     * Gets the description of the command.
     *
     * @return the description
     */
    public String getDescription() {
        return docLocaleAdapter.adapt(description);
    }

    /**
     * Gets the syntax of the command relevant for this entry.
     *
     * @return the syntax
     */
    public String getSyntax() {
        return docLocaleAdapter.adapt(syntax);
    }

    /**
     * Gets the body of this entry, typically explaining more in-depth about the command's functionality.
     *
     * @return the description
     */
    public String getBody() {
        return docLocaleAdapter.adapt(body);
    }

    /**
     * Gets information on the flags that can be used with this part of the command.
     *
     * @return the information on available flags
     */
    public List<FlagInformation> getFlags() {
        return flags;
    }

    /**
     * Gets whether the documentation should be unlisted.
     *
     * @return true if unlisted, false otherwise
     */
    public boolean isUnlisted() {
        return unlisted;
    }

    public static class Builder {

        private final List<FlagInformation> flags = new ArrayList<>();
        private final Locale locale;
        private String description;
        private String syntax;
        private String body;
        private boolean unlisted;

        private Builder(@Nullable Locale locale) {
            this.locale = locale;
        }

        /**
         * Sets the description of the command.
         *
         * @param description the description, or null if description is not provided
         * @return this builder
         */
        public Builder setDescription(@Nullable String description) {
            this.description = description;
            return this;
        }

        /**
         * Sets the syntax of the command.
         *
         * @param syntax the syntax, or null if syntax is not provided
         * @return this builder
         */
        public Builder setSyntax(@Nullable String syntax) {
            this.syntax = syntax;
            return this;
        }

        /**
         * Sets the body of the documentation, typically explaining more in-depth about the command's functionality.
         *
         * @param body the body, or null if body is not provided
         * @return this builder
         */
        public Builder setBody(@Nullable String body) {
            this.body = body;
            return this;
        }

        /**
         * Adds information on a flag that is applicable to this command.
         *
         * @param flagInfo the flag information
         * @return this builder
         */
        public Builder addFlag(FlagInformation flagInfo) {
            Objects.requireNonNull(flagInfo);
            flags.add(flagInfo);
            return this;
        }

        /**
         * Sets whether the documentation should be unlisted for this command.
         *
         * @param unlisted whether the documentation should be unlisted
         * @return this builder
         */
        public Builder setUnlisted(boolean unlisted) {
            this.unlisted = unlisted;
            return this;
        }

        /**
         * Builds a new {@link CommandDocumentation} based on the current state of this builder.
         *
         * @return a new {@link CommandDocumentation}
         */
        public CommandDocumentation build() {
            return new CommandDocumentation(Translator.to(locale), description, syntax, body, flags, unlisted);
        }
    }
}
