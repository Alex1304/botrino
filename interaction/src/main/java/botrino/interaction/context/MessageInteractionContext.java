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
package botrino.interaction.context;

import botrino.interaction.InteractionService;
import discord4j.core.event.domain.interaction.MessageInteractionEvent;
import discord4j.core.object.entity.channel.MessageChannel;

import java.util.Locale;

/**
 * Provides contextual information on an interaction with a message context menu command.
 */
public class MessageInteractionContext extends AbstractInteractionContext<MessageInteractionEvent> {

    public MessageInteractionContext(InteractionService interactionService, Locale locale,
                                     MessageInteractionEvent event, MessageChannel channel) {
        super(interactionService, locale, event, channel);
    }

    @Override
    public String toString() {
        return "MessageInteractionContext{" +
                "locale=" + getLocale() + ", " +
                "event=" + event() + ", " +
                "channel=" + channel() + ", " +
                "user=" + user() +
                "}";
    }
}
