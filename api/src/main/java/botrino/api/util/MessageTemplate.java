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

import discord4j.common.util.Snowflake;
import discord4j.core.spec.EmbedCreateSpec;
import discord4j.core.spec.MessageCreateSpec;
import discord4j.core.spec.MessageEditSpec;
import discord4j.rest.util.AllowedMentions;
import reactor.util.annotation.Nullable;

import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.function.Consumer;

/**
 * Allows to build a message template that can be easily converted to {@link MessageCreateSpec} and {@link
 * MessageEditSpec} consumers.
 */
public final class MessageTemplate {

    private final String messageContent;
    private final Consumer<EmbedCreateSpec> embedSpec;
    private final AllowedMentions allowedMentions;
    private final Map<String, InputStream> files;
    private final Snowflake nonce;
    private final boolean tts;

    private MessageTemplate(String messageContent, Consumer<EmbedCreateSpec> embedSpec,
                            AllowedMentions allowedMentions, Map<String, InputStream> files, Snowflake nonce,
                            boolean tts) {
        this.messageContent = messageContent;
        this.embedSpec = embedSpec;
        this.allowedMentions = allowedMentions;
        this.files = files;
        this.nonce = nonce;
        this.tts = tts;
    }

    /**
     * Converts this template to a consumer of {@link MessageCreateSpec}.
     *
     * @return a create spec consumer
     */
    public Consumer<MessageCreateSpec> toCreateSpec() {
        return spec -> {
            if (messageContent != null) {
                spec.setContent(messageContent);
            }
            if (embedSpec != null) {
                spec.setEmbed(embedSpec);
            }
            if (allowedMentions != null) {
                spec.setAllowedMentions(allowedMentions);
            }
            files.forEach(spec::addFile);
            if (nonce != null) {
                spec.setNonce(nonce);
            }
            spec.setTts(tts);
        };
    }


    /**
     * Converts this template to a consumer of {@link MessageEditSpec}.
     *
     * @return an edit spec consumer
     */
    public Consumer<MessageEditSpec> toEditSpec() {
        return spec -> {
            if (messageContent != null) {
                spec.setContent(messageContent);
            }
            if (embedSpec != null) {
                spec.setEmbed(embedSpec);
            }
        };
    }

    public static class Builder {

        private final Map<String, InputStream> files = new HashMap<>();
        private String messageContent;
        private Consumer<EmbedCreateSpec> embedSpec;
        private AllowedMentions allowedMentions;
        private Snowflake nonce;
        private boolean tts;

        /**
         * Sets the message content for this template.
         *
         * @param messageContent the message content
         * @return this builder
         */
        public Builder setMessageContent(@Nullable String messageContent) {
            this.messageContent = messageContent;
            return this;
        }

        /**
         * Sets the embed for this template.
         *
         * @param embedSpec the embed spec
         * @return this builder
         */
        public Builder setEmbed(@Nullable Consumer<EmbedCreateSpec> embedSpec) {
            this.embedSpec = embedSpec;
            return this;
        }

        /**
         * Sets the allowed mentions for this template. It is only relevant for message creation and not on edit.
         *
         * @param allowedMentions the allowed mentions
         * @return this builder
         */
        public Builder setAllowedMentions(@Nullable AllowedMentions allowedMentions) {
            this.allowedMentions = allowedMentions;
            return this;
        }

        /**
         * Adds a file to attach to the message of this template. It is only relevant for message creation and not on
         * edit.
         *
         * @param fileName the name of the file
         * @param file     the input stream providing the binary data of the file
         * @return this builder
         */
        public Builder addFile(String fileName, InputStream file) {
            Objects.requireNonNull(fileName);
            Objects.requireNonNull(file);
            files.put(fileName, file);
            return this;
        }

        /**
         * Adds a file to attach to the message of this template. The file will automatically be marked as spoiler. It
         * is only relevant for message creation and not on edit.
         *
         * @param fileName the name of the file
         * @param file     the input stream providing the binary data of the file
         * @return this builder
         */
        public Builder addFileSpoiler(String fileName, InputStream file) {
            return addFile("SPOILER_" + fileName, file);
        }

        /**
         * Sets the nonce for the message of this template. It is only relevant for message creation and not on edit.
         *
         * @param nonce the nonce
         * @return this builder
         */
        public Builder setNonce(@Nullable Snowflake nonce) {
            this.nonce = nonce;
            return this;
        }

        /**
         * Sets whether the message of this template should be text-to-speech. It is only relevant for message creation
         * and not on edit.
         *
         * @param tts the tts state
         * @return this builder
         */
        public Builder setTts(boolean tts) {
            this.tts = tts;
            return this;
        }

        /**
         * Builds the {@link MessageTemplate} based on the current state of this builder.
         *
         * @return a new {@link MessageTemplate}
         */
        public MessageTemplate build() {
            return new MessageTemplate(messageContent, embedSpec, allowedMentions, files, nonce, tts);
        }
    }
}
