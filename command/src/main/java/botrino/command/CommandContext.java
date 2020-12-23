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

import botrino.api.i18n.Translator;
import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.object.entity.User;
import discord4j.core.object.entity.channel.MessageChannel;

import java.util.Locale;
import java.util.NoSuchElementException;

public final class CommandContext implements Translator {

    private final MessageCreateEvent event;
    private final String prefixUsed;
    private final TokenizedInput input;
    private final Locale locale;
    private final MessageChannel channel;

    CommandContext(MessageCreateEvent event, String prefixUsed, TokenizedInput input, Locale locale,
                   MessageChannel channel) {
        this.event = event;
        this.prefixUsed = prefixUsed;
        this.input = input;
        this.locale = locale;
        this.channel = channel;
    }

    /**
     * Gets the message create event associated to this command.
     *
     * @return the event
     */
    public MessageCreateEvent event() {
        return event;
    }

    /**
     * Gets the prefix used in the original input.
     *
     * @return the prefix used
     */
    public String getPrefixUsed() {
        return prefixUsed;
    }

    /**
     * Gets the tokenized input of the command in this context, allowing convenient access to command prefix, arguments
     * and flags.
     *
     * @return the command input
     */
    public TokenizedInput input() {
        return input;
    }

    /**
     * Gets the author of the message that created this context. It is a convenient way to do
     * <pre>
     * event().getMessage().getAuthor().orElseThrow();
     * </pre>
     *
     * @return the author
     * @throws NoSuchElementException if the author is unavailable. It can happen if the associated command allows use
     *                                by bot accounts and the author is a webhook, otherwise it should never happen.
     */
    public User author() {
        return event.getMessage().getAuthor().orElseThrow();
    }

    /**
     * Gets the channel of the message that created this context. The channel was cached beforehand, so it can return a
     * MessageChannel instance directly as opposed to a Mono of it.
     *
     * @return the author
     */
    public MessageChannel channel() {
        return channel;
    }

    @Override
    public Locale getLocale() {
        return locale;
    }

    @Override
    public String toString() {
        return "CommandContext{" +
                "event=" + event +
                ", prefixUsed=" + prefixUsed +
                ", input=" + input +
                ", locale=" + locale +
                '}';
    }
}
