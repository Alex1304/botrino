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
package botrino.command.config;

import botrino.api.config.ConfigEntry;
import botrino.command.menu.PaginationControls;
import com.google.gson.*;
import discord4j.common.util.Snowflake;
import discord4j.core.object.reaction.ReactionEmoji;

import java.lang.reflect.Type;

public final class CommandConfigEntry implements ConfigEntry<CommandConfig> {

    private static final String NEXT = "next";
    private static final String PREVIOUS = "previous";
    private static final String CLOSE = "close";
    private static final String EMOJI_ID = "emoji_id";
    private static final String EMOJI_NAME = "emoji_name";
    private static final String ANIMATED = "animated";
    private static final String EMOJI_UNICODE = "emoji_unicode";

    @Override
    public String define(GsonBuilder gsonBuilder) {
        gsonBuilder.registerTypeAdapter(PaginationControls.class, new PaginationControlsDeserializer());
        return "command";
    }

    private static class PaginationControlsDeserializer implements JsonDeserializer<PaginationControls> {

        @Override
        public PaginationControls deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
            var o = json.getAsJsonObject();
            var next = PaginationControls.DEFAULT_NEXT_EMOJI;
            var previous = PaginationControls.DEFAULT_PREVIOUS_EMOJI;
            var close = PaginationControls.DEFAULT_CLOSE_EMOJI;
            if (o.has(NEXT)) {
                next = deserializeEmoji(NEXT, o.get(NEXT).getAsJsonObject());
            }
            if (o.has(PREVIOUS)) {
                previous = deserializeEmoji(PREVIOUS, o.get(PREVIOUS).getAsJsonObject());
            }
            if (o.has(CLOSE)) {
                close = deserializeEmoji(CLOSE, o.get(CLOSE).getAsJsonObject());
            }
            return PaginationControls.of(next, previous, close);
        }

        private ReactionEmoji deserializeEmoji(String name, JsonObject o) {
            if (o.has(EMOJI_ID) && o.has(EMOJI_NAME)) {
                return ReactionEmoji.custom(Snowflake.of(o.get(EMOJI_ID).getAsLong()),
                        o.get(EMOJI_NAME).getAsString(),
                        o.has(ANIMATED) && o.get(ANIMATED).getAsBoolean());
            } else if (o.has(EMOJI_UNICODE)) {
                return ReactionEmoji.unicode(o.get(EMOJI_UNICODE).getAsString());
            }
            throw new JsonParseException("pagination_controls: could not determine whether " + name + " is a custom " +
                    "or a unicode emoji");
        }
    }
}
