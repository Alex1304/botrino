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
package botrino.interaction;

import botrino.api.extension.BotrinoExtension;
import botrino.api.util.ConfigUtils;
import botrino.api.util.InstanceCache;
import botrino.api.util.MatcherConsumer;
import botrino.interaction.annotation.ChatInputCommand;
import botrino.interaction.annotation.ComponentCommand;
import botrino.interaction.annotation.MessageCommand;
import botrino.interaction.annotation.UserCommand;
import botrino.interaction.config.InteractionConfig;
import botrino.interaction.listener.ChatInputInteractionListener;
import botrino.interaction.listener.ComponentInteractionListener;
import botrino.interaction.listener.MessageInteractionListener;
import botrino.interaction.listener.UserInteractionListener;
import com.github.alex1304.rdi.config.ServiceDescriptor;
import com.github.alex1304.rdi.finder.annotation.RdiService;
import reactor.core.publisher.Mono;

import java.util.*;

public final class InteractionExtension implements BotrinoExtension {

    private final InstanceCache instanceCache = InstanceCache.create();
    private final List<InteractionErrorHandler> errorHandlers = new ArrayList<>();
    private final List<InteractionEventProcessor> eventProcessors = new ArrayList<>();
    private final Set<Object> chatInputCommands = new HashSet<>();
    private final Set<ChatInputInteractionListener> chatInputInteractionListeners = new HashSet<>();
    private final Set<UserInteractionListener> userInteractionListeners = new HashSet<>();
    private final Set<MessageInteractionListener> messageInteractionListeners = new HashSet<>();
    private final Set<ComponentInteractionListener<?>> componentInteractionListeners = new HashSet<>();
    private InteractionService interactionService;

    @Override
    public void onClassDiscovered(Class<?> clazz) {
        if (clazz.isAnnotationPresent(RdiService.class)) {
            return;
        }
        if (clazz.isAnnotationPresent(ChatInputCommand.class)) {
            chatInputCommands.add(instanceCache.getInstance(clazz));
        }
        if (ChatInputInteractionListener.class.isAssignableFrom(clazz)) {
            chatInputInteractionListeners.add(instanceCache.getInstance(
                    clazz.asSubclass(ChatInputInteractionListener.class)));
        }
        if (UserInteractionListener.class.isAssignableFrom(clazz) && clazz.isAnnotationPresent(UserCommand.class)) {
            userInteractionListeners.add(instanceCache.getInstance(clazz.asSubclass(UserInteractionListener.class)));
        }
        if (MessageInteractionListener.class.isAssignableFrom(clazz) &&
                clazz.isAnnotationPresent(MessageCommand.class)) {
            messageInteractionListeners.add(instanceCache.getInstance(
                    clazz.asSubclass(MessageInteractionListener.class)));
        }
        if (ComponentInteractionListener.class.isAssignableFrom(clazz) &&
                clazz.isAnnotationPresent(ComponentCommand.class)) {
            componentInteractionListeners.add(instanceCache.getInstance(
                    clazz.asSubclass(ComponentInteractionListener.class)));
        }
        if (InteractionErrorHandler.class.isAssignableFrom(clazz)) {
            errorHandlers.add(instanceCache.getInstance(clazz.asSubclass(InteractionErrorHandler.class)));
        }
        if (InteractionEventProcessor.class.isAssignableFrom(clazz)) {
            eventProcessors.add(instanceCache.getInstance(clazz.asSubclass(InteractionEventProcessor.class)));
        }
    }

    @Override
    public void onServiceCreated(Object serviceInstance) {
        MatcherConsumer.create()
                .matchType(InteractionService.class, o -> this.interactionService = o)
                .match(o -> o.getClass().isAnnotationPresent(ChatInputCommand.class), chatInputCommands::add)
                .matchType(ChatInputInteractionListener.class, chatInputInteractionListeners::add)
                .matchType(UserInteractionListener.class,
                        o -> o.getClass().isAnnotationPresent(UserCommand.class), userInteractionListeners::add)
                .matchType(MessageInteractionListener.class,
                        o -> o.getClass().isAnnotationPresent(MessageCommand.class), messageInteractionListeners::add)
                .matchType(ComponentInteractionListener.class,
                        o -> o.getClass().isAnnotationPresent(ComponentCommand.class),
                        componentInteractionListeners::add)
                .matchType(InteractionErrorHandler.class, errorHandlers::add)
                .matchType(InteractionEventProcessor.class, eventProcessors::add)
                .allowMultipleMatches(true)
                .accept(serviceInstance);
    }

    @Override
    public Set<ServiceDescriptor> provideExtraServices() {
        return Set.of();
    }

    @Override
    public Set<Class<?>> provideExtraDiscoverableClasses() {
        return Set.of(InteractionService.class, InteractionConfig.class);
    }

    @Override
    public Mono<Void> finishAndJoin() {
        return Mono.defer(() -> {
            Objects.requireNonNull(interactionService);
            chatInputCommands.forEach(c -> interactionService.registerChatInputCommand(c,
                    chatInputInteractionListeners));
            userInteractionListeners.forEach(interactionService::registerUserCommand);
            messageInteractionListeners.forEach(interactionService::registerMessageCommand);
            componentInteractionListeners.forEach(interactionService::registerComponentCommand);
            interactionService.setErrorHandler(ConfigUtils
                    .selectImplementation(InteractionErrorHandler.class, errorHandlers)
                    .orElse(InteractionErrorHandler.NO_OP));
            interactionService.setEventProcessor(ConfigUtils
                    .selectImplementation(InteractionEventProcessor.class, eventProcessors)
                    .orElse(InteractionEventProcessor.NO_OP));
            return interactionService.run();
        });
    }
}
