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

import botrino.api.annotation.ConfigEntry;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import org.immutables.value.Value;

import java.util.Optional;
import java.util.OptionalLong;

@Value.Immutable
@JsonDeserialize(as = ImmutableCommandConfig.class)
@ConfigEntry("command")
public interface CommandConfig {

    String prefix();

    @JsonProperty("pagination_controls")
    Optional<PaginationControlsConfig> paginationControls();

    @JsonProperty("menu_timeout_seconds")
    OptionalLong menuTimeoutSeconds();

    interface PaginationControlsConfig {

        @JsonProperty("next_emoji")
        Optional<EmojiConfig> nextEmoji();

        @JsonProperty("previous_emoji")
        Optional<EmojiConfig> previousEmoji();

        @JsonProperty("close_emoji")
        Optional<EmojiConfig> closeEmoji();
    }

    interface EmojiConfig {

        OptionalLong id();

        Optional<String> name();

        Optional<Boolean> animated();

        Optional<String> unicode();
    }
}
