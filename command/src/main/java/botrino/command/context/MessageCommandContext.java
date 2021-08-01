/*
 * This file is part of the Botrino project and is licensed under the MIT license.
 *
 * Copyright (c) 2021 Alexandre Miranda
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
package botrino.command.context;

import botrino.command.CommandService;
import botrino.command.TokenizedInput;
import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.object.entity.User;
import discord4j.core.object.entity.channel.MessageChannel;

import java.util.Locale;

public final class MessageCommandContext extends AbstractCommandContext<MessageCreateEvent> {

    private final MessageChannel channel;
    private final String prefixUsed;
    private final TokenizedInput input;

    public MessageCommandContext(CommandService commandService, Locale locale, MessageChannel channel,
                                 MessageCreateEvent event, String prefixUsed, TokenizedInput input) {
        super(commandService, locale, event);
        this.channel = channel;
        this.prefixUsed = prefixUsed;
        this.input = input;
    }

    @Override
    public MessageChannel channel() {
        return channel;
    }

    @Override
    public User user() {
        return event().getMessage().getAuthor().orElseThrow(IllegalStateException::new);
    }

    public String prefixUsed() {
        return prefixUsed;
    }

    public TokenizedInput input() {
        return input;
    }

    @Override
    public String toString() {
        return "MessageCommandContext{" +
                "locale=" + getLocale() + ", " +
                "event=" + event() + ", " +
                "channel=" + channel() + ", " +
                "user=" + user() + ", " +
                "prefixUsed=" + prefixUsed + ", " +
                "input=" + input +
                "}";
    }
}
