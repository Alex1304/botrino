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

import discord4j.core.object.component.LayoutComponent;
import discord4j.core.spec.EmbedCreateSpec;
import discord4j.core.spec.MessageCreateFields.File;
import discord4j.core.spec.MessageCreateSpec;
import discord4j.core.spec.MessageEditSpec;
import discord4j.discordjson.possible.Possible;
import discord4j.rest.util.AllowedMentions;
import reactor.util.annotation.Nullable;

import java.io.InputStream;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Allows to build a message template that can be easily converted to {@link MessageCreateSpec} and {@link
 * MessageEditSpec}.
 */
public final class MessageTemplate {

    private final String messageContent;
    private final List<EmbedCreateSpec> embeds;
    private final AllowedMentions allowedMentions;
    private final Map<String, InputStream> files;
    private final String nonce;
    private final boolean tts;
    private final List<LayoutComponent> components;

    private MessageTemplate(@Nullable String messageContent, List<EmbedCreateSpec> embeds,
                            @Nullable AllowedMentions allowedMentions, Map<String, InputStream> files,
                            @Nullable String nonce, boolean tts, List<LayoutComponent> components) {
        this.messageContent = messageContent;
        this.embeds = embeds;
        this.allowedMentions = allowedMentions;
        this.files = files;
        this.nonce = nonce;
        this.tts = tts;
        this.components = components;
    }

    /**
     * Creates a new builder for {@link MessageTemplate}.
     *
     * @return a new builder
     */
    public static Builder builder() {
        return new Builder();
    }

    /**
     * Converts this template to a {@link MessageCreateSpec}.
     *
     * @return a create spec
     */
    public MessageCreateSpec toCreateSpec() {
        return MessageCreateSpec.create()
                .withContent(messageContent != null ? Possible.of(messageContent) : Possible.absent())
                .withEmbeds(embeds)
                .withAllowedMentions(allowedMentions != null ? Possible.of(allowedMentions) : Possible.absent())
                .withFiles(files.entrySet().stream()
                        .map(entry -> File.of(entry.getKey(), entry.getValue()))
                        .collect(Collectors.toList()))
                .withNonce(nonce != null ? Possible.of(nonce) : Possible.absent())
                .withComponents(components)
                .withTts(tts);
    }


    /**
     * Converts this template to a {@link MessageEditSpec}.
     *
     * @return an edit spec
     */
    public MessageEditSpec toEditSpec() {
        return MessageEditSpec.create()
                .withContentOrNull(messageContent)
                .withEmbedsOrNull(embeds)
                .withComponentsOrNull(components);
    }

    public static final class Builder {

        private Map<String, InputStream> files = new HashMap<>();
        private String messageContent;
        private List<EmbedCreateSpec> embeds;
        private AllowedMentions allowedMentions;
        private String nonce;
        private boolean tts;
        private List<LayoutComponent> components;

        private Builder() {
        }

        /**
         * Initializes this builder with all the properties of the given {@link MessageTemplate}.
         *
         * @param messageTemplate the message template to copy properties from
         * @return this builder
         */
        public Builder from(MessageTemplate messageTemplate) {
            this.messageContent = messageTemplate.messageContent;
            this.files = new HashMap<>(messageTemplate.files);
            this.embeds = new ArrayList<>(messageTemplate.embeds);
            this.allowedMentions = messageTemplate.allowedMentions;
            this.nonce = messageTemplate.nonce;
            this.tts = messageTemplate.tts;
            this.components = new ArrayList<>(messageTemplate.components);
            return this;
        }

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
         * Adds an embed to the list of embeds for this template.
         *
         * @param embed the embed to add
         * @return this builder
         */
        public Builder addEmbed(EmbedCreateSpec embed) {
            this.embeds.add(embed);
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
        public Builder setNonce(@Nullable String nonce) {
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
            return new MessageTemplate(messageContent, embeds, allowedMentions, files, nonce, tts, components);
        }
    }
}
