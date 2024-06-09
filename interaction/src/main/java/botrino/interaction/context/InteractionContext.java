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

import botrino.api.i18n.Translator;
import botrino.interaction.InteractionService;
import botrino.interaction.annotation.Acknowledge;
import botrino.interaction.config.InteractionConfig;
import botrino.interaction.listener.ComponentInteractionListener;
import discord4j.core.event.domain.interaction.DeferrableInteractionEvent;
import discord4j.core.event.domain.interaction.ModalSubmitInteractionEvent;
import discord4j.core.object.entity.User;
import discord4j.core.object.entity.channel.MessageChannel;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.List;
import java.util.concurrent.TimeoutException;

import static botrino.interaction.listener.ComponentInteractionListener.*;

/**
 * Provides contextual information on an interaction.
 */
public interface InteractionContext extends Translator {

    /**
     * The event object for this interaction.
     *
     * @return the event
     */
    DeferrableInteractionEvent event();

    /**
     * The channel where the interaction took place.
     *
     * @return the channel
     */
    MessageChannel channel();

    /**
     * The user who initiated the interaction.
     *
     * @return the user
     */
    User user();

    /**
     * Registers a {@link ComponentInteractionListener} via
     * {@link InteractionService#registerSingleUseComponentListener(ComponentInteractionListener, InteractionContext)}
     * and waits for the listener to be executed once. When an interaction is received on the target component, the
     * listener is executed and may return a value, which can be used downstream for further processing. It is
     * recommended to use one of the static factories of {@link ComponentInteractionListener} in order to get an
     * instance to pass to this method.
     * <p>
     * The returned Mono will error with {@link TimeoutException} if the user has not interacted with the target
     * component after a certain time, configurable via {@link InteractionConfig#awaitComponentTimeoutSeconds()}.
     * <p>
     * The component interaction will automatically be acknowledged according to the default acknowledgment mode defined
     * in {@link InteractionConfig#defaultACKMode()}. To specify an acknowledgment for this specific interaction, use
     * the overload {@link #awaitComponentInteraction(Acknowledge.Mode, ComponentInteractionListener)}.
     *
     * @param componentInteraction the instance of {@link ComponentInteractionListener}
     * @param <R>                  the return type of the listener
     * @return a Mono emitting the return value of the listener once it has been executed.
     */
    default <R> Mono<R> awaitComponentInteraction(ComponentInteractionListener<R> componentInteraction) {
        return awaitComponentInteraction(Acknowledge.Mode.DEFAULT, componentInteraction);
    }

    /**
     * Registers a {@link ComponentInteractionListener} via
     * {@link InteractionService#registerSingleUseComponentListener(ComponentInteractionListener, InteractionContext)}
     * and waits for the listener to be executed once. The interaction must happen in the same channel by the same user.
     * When an interaction is received on the target component, the listener is executed and may return a value, which
     * can be used downstream for further processing. It is recommended to use one of the static factories of
     * {@link ComponentInteractionListener} in order to get an instance to pass to this method.
     * <p>
     * The returned Mono will error with {@link TimeoutException} if the user has not interacted with the target
     * component after a certain time, configurable via {@link InteractionConfig#awaitComponentTimeoutSeconds()}.
     *
     * @param ack                  the acknowledgment mode to apply for the component interaction
     * @param componentInteraction the instance of {@link ComponentInteractionListener}
     * @param <R>                  the return type of the listener
     * @return a Mono emitting the return value of the listener once it has been executed.
     */
    <R> Mono<R> awaitComponentInteraction(Acknowledge.Mode ack, ComponentInteractionListener<R> componentInteraction);

    /**
     * Waits until the button which customId is given is clicked, by the same user in the same channel as this context's
     * event. Use this method if you only need to know when the button is clicked. Prefer
     * {@link #awaitButtonClick(String)} if you need the whole {@link ButtonInteractionContext} object.
     * <p>
     * This is a shorthand for:
     * <pre>
     *     awaitComponentInteraction(button(customId, btnCtx -> Mono.just(customId)));
     * </pre>
     * <p>
     * The returned Mono will error with {@link TimeoutException} if the user has not interacted with the target
     * component after a certain time, configurable via {@link InteractionConfig#awaitComponentTimeoutSeconds()}.
     *
     * @param customId the custom ID of the button
     * @return a Mono emitting the customId of the button when the button is clicked.
     */
    default Mono<String> awaitButtonClick(String customId) {
        return awaitButtonClick(Acknowledge.Mode.DEFAULT, customId);
    }

    /**
     * Waits until the button which customId is given is clicked, by the same user in the same channel as this context's
     * event. Use this method if you only need to know when the button is clicked. Prefer
     * {@link #awaitButtonInteraction(Acknowledge.Mode, String)} if you need the whole {@link ButtonInteractionContext}
     * object.
     * <p>
     * This is a shorthand for:
     * <pre>
     *     awaitComponentInteraction(button(customId, btnCtx -> Mono.just(customId)));
     * </pre>
     * <p>
     * The returned Mono will error with {@link TimeoutException} if the user has not interacted with the target
     * component after a certain time, configurable via {@link InteractionConfig#awaitComponentTimeoutSeconds()}.
     *
     * @param ack      the acknowledgment mode to apply for the component interaction
     * @param customId the custom ID of the button
     * @return a Mono emitting the customId of the button when the button is clicked.
     */
    default Mono<String> awaitButtonClick(Acknowledge.Mode ack, String customId) {
        return awaitComponentInteraction(ack, button(customId, btnCtx -> Mono.just(customId)));
    }

    /**
     * Waits until the button which customId is given is clicked, by the same user in the same channel as this context's
     * event. Use this method if you need the whole {@link ButtonInteractionContext} object. Prefer
     * {@link #awaitButtonClick(String)} if you only need to know when the button is clicked.
     * <p>
     * This is a shorthand for:
     * <pre>
     *     awaitComponentInteraction(button(customId, Mono::just));
     * </pre>
     * <p>
     * The returned Mono will error with {@link TimeoutException} if the user has not interacted with the target
     * component after a certain time, configurable via {@link InteractionConfig#awaitComponentTimeoutSeconds()}.
     *
     * @param customId the custom ID of the button
     * @return a Mono emitting the {@link ButtonInteractionContext} corresponding to the button clicked.
     */
    default Mono<ButtonInteractionContext> awaitButtonInteraction(String customId) {
        return awaitButtonInteraction(Acknowledge.Mode.DEFAULT, customId);
    }

    /**
     * Waits until the button which customId is given is clicked, by the same user in the same channel as this context's
     * event. Use this method if you need the whole {@link ButtonInteractionContext} object. Prefer
     * {@link #awaitButtonClick(Acknowledge.Mode, String)} if you only need to know when the button is clicked.
     * <p>
     * This is a shorthand for:
     * <pre>
     *     awaitComponentInteraction(ack, button(customId, Mono::just));
     * </pre>
     * <p>
     * The returned Mono will error with {@link TimeoutException} if the user has not interacted with the target
     * component after a certain time, configurable via {@link InteractionConfig#awaitComponentTimeoutSeconds()}.
     *
     * @param ack      the acknowledgment mode to apply for the component interaction
     * @param customId the custom ID of the button
     * @return a Mono emitting the {@link ButtonInteractionContext} corresponding to the button clicked.
     */
    default Mono<ButtonInteractionContext> awaitButtonInteraction(Acknowledge.Mode ack, String customId) {
        return awaitComponentInteraction(ack, button(customId, Mono::just));
    }

    /**
     * Waits until items are selected from the select menu which customId is given, by the same user in the same channel
     * as this context's event. Use this method if you only need to access the selected values. Prefer
     * {@link #awaitSelectMenuInteraction(String)} if you need the whole {@link SelectMenuInteractionContext} object.
     * <p>
     * This is a shorthand for:
     * <pre>
     *     awaitComponentInteraction(button(customId, selCtx -> Mono.just(selCtx.event().getValues())));
     * </pre>
     * <p>
     * The returned Mono will error with {@link TimeoutException} if the user has not interacted with the target
     * component after a certain time, configurable via {@link InteractionConfig#awaitComponentTimeoutSeconds()}.
     *
     * @param customId the custom ID of the select menu
     * @return a Mono emitting the customId of the select menu when items are selected.
     */
    default Mono<List<String>> awaitSelectMenuItems(String customId) {
        return awaitSelectMenuItems(Acknowledge.Mode.DEFAULT, customId);
    }

    /**
     * Waits until items are selected from the select menu which customId is given, by the same user in the same channel
     * as this context's event. Use this method if you only need to access the selected values. Prefer
     * {@link #awaitSelectMenuInteraction(Acknowledge.Mode, String)} if you need the whole
     * {@link SelectMenuInteractionContext} object.
     * <p>
     * This is a shorthand for:
     * <pre>
     *     awaitComponentInteraction(ack, button(customId, selCtx -> Mono.just(selCtx.event().getValues())));
     * </pre>
     * <p>
     * The returned Mono will error with {@link TimeoutException} if the user has not interacted with the target
     * component after a certain time, configurable via {@link InteractionConfig#awaitComponentTimeoutSeconds()}.
     *
     * @param ack      the acknowledgment mode to apply for the component interaction
     * @param customId the custom ID of the select menu
     * @return a Mono emitting the customId of the select menu when items are selected.
     */
    default Mono<List<String>> awaitSelectMenuItems(Acknowledge.Mode ack, String customId) {
        return awaitComponentInteraction(ack, selectMenu(customId, selCtx -> Mono.just(selCtx.event().getValues())));
    }

    /**
     * Waits until items are selected from the select menu which customId is given, by the same user in the same channel
     * as this context's event. Use this method if you need the whole {@link SelectMenuInteractionContext} object.
     * Prefer {@link #awaitSelectMenuItems(String)} if you only need to access the selected values.
     * <p>
     * This is a shorthand for:
     * <pre>
     *     awaitComponentInteraction(button(customId, Mono::just);
     * </pre>
     * <p>
     * The returned Mono will error with {@link TimeoutException} if the user has not interacted with the target
     * component after a certain time, configurable via {@link InteractionConfig#awaitComponentTimeoutSeconds()}.
     *
     * @param customId the custom ID of the select menu
     * @return a Mono emitting the customId of the select menu when items are selected.
     */
    default Mono<SelectMenuInteractionContext> awaitSelectMenuInteraction(String customId) {
        return awaitSelectMenuInteraction(Acknowledge.Mode.DEFAULT, customId);
    }

    /**
     * Waits until items are selected from the select menu which customId is given, by the same user in the same channel
     * as this context's event. Use this method if you need the whole {@link SelectMenuInteractionContext} object.
     * Prefer {@link #awaitSelectMenuItems(Acknowledge.Mode, String)} if you only need to access the selected values.
     * <p>
     * This is a shorthand for:
     * <pre>
     *     awaitComponentInteraction(ack, button(customId, Mono::just);
     * </pre>
     * <p>
     * The returned Mono will error with {@link TimeoutException} if the user has not interacted with the target
     * component after a certain time, configurable via {@link InteractionConfig#awaitComponentTimeoutSeconds()}.
     *
     * @param ack      the acknowledgment mode to apply for the component interaction
     * @param customId the custom ID of the select menu
     * @return a Mono emitting the customId of the select menu when items are selected.
     */
    default Mono<SelectMenuInteractionContext> awaitSelectMenuInteraction(Acknowledge.Mode ack, String customId) {
        return awaitComponentInteraction(ack, selectMenu(customId, Mono::just));
    }

    /**
     * Waits until the user submits a modal which customId is given, by the same user in the same channel as this
     * context's event. This is a shorthand for:
     * <pre>
     *     awaitComponentInteraction(button(customId, modalCtx -> Mono.just(modalCtx.event())));
     * </pre>
     * <p>
     * The returned Mono will error with {@link TimeoutException} if the user has not interacted with the target
     * component after a certain time, configurable via {@link InteractionConfig#awaitComponentTimeoutSeconds()}.
     *
     * @param customId the custom ID of the modal
     * @return a Mono emitting the modal submit event that contains the data submitted by the user
     */
    default Mono<ModalSubmitInteractionEvent> awaitModalSubmit(String customId) {
        return awaitModalSubmit(Acknowledge.Mode.DEFAULT, customId);
    }

    /**
     * Waits until the user submits a modal which customId is given, by the same user in the same channel as this
     * context's event. This is a shorthand for:
     * <pre>
     *     awaitComponentInteraction(ack, button(customId, modalCtx -> Mono.just(modalCtx.event())));
     * </pre>
     * <p>
     * The returned Mono will error with {@link TimeoutException} if the user has not interacted with the target
     * component after a certain time, configurable via {@link InteractionConfig#awaitComponentTimeoutSeconds()}.
     *
     * @param customId the custom ID of the modal
     * @return a Mono emitting the modal submit event that contains the data submitted by the user
     */
    default Mono<ModalSubmitInteractionEvent> awaitModalSubmit(Acknowledge.Mode ack, String customId) {
        return awaitComponentInteraction(ack, modalSubmit(customId, modalCtx -> Mono.just(modalCtx.event())));
    }

    /**
     * Gets the timeout value that is applied when calling
     * {@link #awaitComponentInteraction(ComponentInteractionListener)}.
     *
     * @return a {@link Duration}
     */
    Duration getAwaitComponentTimeout();
}
