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
package botrino.api.config.object;

import botrino.api.annotation.ConfigEntry;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import discord4j.core.object.presence.Activity;
import discord4j.core.object.presence.ClientActivity;
import discord4j.core.object.presence.ClientPresence;
import discord4j.core.object.presence.Status;
import org.immutables.value.Value;

import java.util.Optional;
import java.util.OptionalLong;

@Value.Immutable
@JsonDeserialize(as = ImmutableBotConfig.class)
@ConfigEntry("bot")
public interface BotConfig {

    String token();

    Optional<StatusConfig> presence();

    @JsonProperty("enabled_intents")
    OptionalLong enabledIntents();

    @Value.Immutable
    @JsonDeserialize(as = ImmutableStatusConfig.class)
    interface StatusConfig {

        @JsonProperty("activity_type")
        Optional<String> activityType();

        @JsonProperty("activity_text")
        Optional<String> activityText();

        @JsonProperty("streaming_url")
        Optional<String> streamingUrl();

        String status();

        default ClientPresence toPresence() {
            var status = Status.valueOf(status().toUpperCase());
            var activity = activityType()
                    .map(String::toUpperCase)
                    .map(Activity.Type::valueOf)
                    .orElse(Activity.Type.UNKNOWN);
            var text = activityText().orElse(null);
            var url = streamingUrl().orElse("http://127.0.0.1");
            ClientActivity clientActivity = null;
            if (text != null) {
                switch (activity) {
                    case PLAYING:
                        clientActivity = ClientActivity.playing(text);
                        break;
                    case COMPETING:
                        clientActivity = ClientActivity.competing(text);
                        break;
                    case LISTENING:
                        clientActivity = ClientActivity.listening(text);
                        break;
                    case STREAMING:
                        clientActivity = ClientActivity.streaming(text, url);
                        break;
                    case WATCHING:
                        clientActivity = ClientActivity.watching(text);
                        break;
                }
            }
            switch (status) {
                case IDLE:
                    return clientActivity == null ? ClientPresence.idle() :
                            ClientPresence.idle(clientActivity);
                case DO_NOT_DISTURB:
                    return clientActivity == null ? ClientPresence.doNotDisturb() :
                            ClientPresence.doNotDisturb(clientActivity);
                case INVISIBLE:
                    return ClientPresence.invisible();
                default:
                    return clientActivity == null ? ClientPresence.online() :
                            ClientPresence.online(clientActivity);
            }
        }
    }
}
