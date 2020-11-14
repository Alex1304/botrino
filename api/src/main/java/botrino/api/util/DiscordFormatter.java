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

import discord4j.core.object.entity.Role;
import discord4j.core.object.entity.channel.GuildChannel;
import discord4j.core.object.entity.channel.TextChannel;

/**
 * Contains utility methods to format a Discord entity into a user-friendly String.
 */
public final class DiscordFormatter {

    private DiscordFormatter() {
    }

    /**
     * Formats a role to the following format: {@literal @}name (id)
     *
     * @param role the role to format
     * @return the formatted user
     */
    public static String formatRole(Role role) {
        return "@" + role.getName() + " (" + role.getId().asString() + ")";
    }

    /**
     * Formats a channel to the channel mention if text channel, or name (id) if voice channel or category.
     *
     * @param channel the channel to format
     * @return the formatted channel
     */
    public static String formatGuildChannel(GuildChannel channel) {
        return channel instanceof TextChannel ? channel.getMention()
                : channel.getName() + " (" + channel.getId().asString() + ")";
    }
}
