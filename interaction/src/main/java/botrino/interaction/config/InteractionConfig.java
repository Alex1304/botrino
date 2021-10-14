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
package botrino.interaction.config;

import botrino.api.annotation.ConfigEntry;
import botrino.interaction.annotation.Acknowledge;
import botrino.interaction.context.InteractionContext;
import botrino.interaction.listener.ComponentInteractionListener;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import org.immutables.value.Value;

import java.util.Optional;

/**
 * The configuration for the interaction service.
 */
@Value.Immutable
@JsonDeserialize(as = ImmutableInteractionConfig.class)
@ConfigEntry("interaction")
public interface InteractionConfig {

    /**
     * Represents the acknowledgment mode used when "default" is selected. It currently corresponds to {@link
     * Acknowledge.Mode#DEFER}.
     */
    Acknowledge.Mode DEFAULT_ACK_MODE = Acknowledge.Mode.DEFER;

    /**
     * Initializes a new builder to create a {@link InteractionConfig} instance.
     *
     * @return a new builder
     */
    static ImmutableInteractionConfig.Builder builder() {
        return ImmutableInteractionConfig.builder();
    }

    /**
     * Creates an {@link InteractionConfig} with default values.
     *
     * @return an {@link InteractionConfig}
     */
    static ImmutableInteractionConfig withDefaults() {
        return ImmutableInteractionConfig.builder().build();
    }

    /**
     * Specifies the guild ID in which application commands should be deployed. This is recommended during development.
     * Leaving empty or setting to <code>null</code> will deploy commands globally.
     *
     * @return the application commands guild ID, if present
     */
    @JsonProperty("application_commands_guild_id")
    Optional<Long> applicationCommandsGuildId();

    /**
     * Specifies the way interactions should be acknowledged by default. Possible values are:
     * <ul>
     *     <li>DEFAULT: equivalent to DEFER.</li>
     *     <li>DEFER: automatically acknowledges all interactions with defer reply or defer edit as appropriate.
     *     This is the default behavior.</li>
     *     <li>DEFER_EPHEMERAL: similar to DEFER except the EPHEMERAL flag is set, meaning the next reply/edit will
     *     only be visible to the user who initiated the interaction.</li>
     *     <li>NONE: won't acknowledge any interaction automatically. In that case, you will be in charge of
     *     acknowledging interactions manually.</li>
     * </ul>
     * <p>
     * Acknowledgment mode may be overriden for specific commands via the {@link Acknowledge} annotation.
     *
     * @return the default acknowledgment mode
     */
    @JsonProperty("default_ack_mode")
    @Value.Default
    default String defaultACKMode() {
        return DEFAULT_ACK_MODE.name();
    }

    /**
     * The default timeout in seconds to apply on the
     * {@link InteractionContext#awaitComponentInteraction(ComponentInteractionListener)}
     * method. Since an interaction token is only valid for 15 minutes, it is not recommended to set this value beyond
     * 900. Default value is 600.
     *
     * @return the timeout in seconds
     */
    @JsonProperty("await_component_timeout_seconds")
    @Value.Default
    default int awaitComponentTimeoutSeconds() {
        return 600;
    }

    /**
     * Gets the enum value equivalent of {@link #defaultACKMode()}.
     *
     * @return the enum value
     */
    default Acknowledge.Mode defaultACKModeEnum() {
        final var value = Acknowledge.Mode.valueOf(defaultACKMode().toUpperCase());
        return value == Acknowledge.Mode.DEFAULT ? DEFAULT_ACK_MODE : value;
    }
}

