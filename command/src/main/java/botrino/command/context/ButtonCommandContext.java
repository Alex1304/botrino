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
import discord4j.core.event.domain.interaction.ButtonInteractEvent;
import discord4j.core.object.entity.channel.MessageChannel;

import java.util.Locale;

public final class ButtonCommandContext extends AbstractInteractionCommandContext<ButtonInteractEvent> {

    public ButtonCommandContext(CommandService commandService, Locale locale, ButtonInteractEvent event,
                                MessageChannel channel) {
        super(commandService, locale, event, channel);
    }

    @Override
    public String toString() {
        return "ButtonCommandContext{" +
                "locale=" + getLocale() + ", " +
                "event=" + event() + ", " +
                "channel=" + channel() + ", " +
                "user=" + user() +
                "}";
    }
}
