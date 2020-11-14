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
package botrino.api.config.bot;

import botrino.api.config.ConfigEntry;
import com.google.gson.*;
import discord4j.core.object.presence.Activity;
import discord4j.discordjson.json.ActivityUpdateRequest;
import discord4j.discordjson.json.gateway.StatusUpdate;

import java.lang.reflect.Type;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;

public final class BotConfigEntry implements ConfigEntry<BotConfig> {

    private static final String ACTIVITY_TYPE = "activity_type";
    private static final String ACTIVITY_TEXT = "activity_text";
    private static final String STATUS = "status";

    @Override
    public String define(GsonBuilder gsonBuilder) {
        gsonBuilder.registerTypeAdapter(StatusUpdate.class, new StatusUpdateSerializer());
        gsonBuilder.registerTypeAdapter(StatusUpdate.class, new StatusUpdateDeserializer());
        return "bot";
    }

    private static class StatusUpdateSerializer implements JsonSerializer<StatusUpdate> {

        @Override
        public JsonElement serialize(StatusUpdate src, Type typeOfSrc, JsonSerializationContext context) {
            var o = new JsonObject();
            var activity = src.activities()
                    .filter(Predicate.not(List::isEmpty))
                    .map(l -> l.get(0))
                    .orElse(null);
            if (activity != null) {
                o.addProperty(ACTIVITY_TYPE, Activity.Type.of(activity.type()).toString().toLowerCase());
                o.addProperty(ACTIVITY_TEXT, activity.name());
            }
            o.addProperty(STATUS, src.status());
            return null;
        }
    }

    private static class StatusUpdateDeserializer implements JsonDeserializer<StatusUpdate> {

        @Override
        public StatusUpdate deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) {
            JsonObject o = json.getAsJsonObject();
            var builder = StatusUpdate.builder();
            if (o.has(ACTIVITY_TYPE) && o.has(ACTIVITY_TEXT)) {
                builder.activities(List.of(ActivityUpdateRequest.builder()
                        .type(Activity.Type.valueOf(o.get(ACTIVITY_TYPE).getAsString().toUpperCase()).getValue())
                        .name(o.get(ACTIVITY_TEXT).getAsString())
                        .build()));
            } else {
                builder.activities(Optional.empty());
            }
            builder.status(o.get(STATUS).getAsString()).afk(false);
            return builder.build();
        }
    }
}
