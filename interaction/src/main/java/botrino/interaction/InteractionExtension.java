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
import botrino.interaction.config.InteractionConfig;
import com.github.alex1304.rdi.config.ServiceDescriptor;
import com.github.alex1304.rdi.finder.annotation.RdiService;
import reactor.core.publisher.Mono;

import java.util.*;

public final class InteractionExtension implements BotrinoExtension {

    private final InstanceCache instanceCache = InstanceCache.create();
    private final Set<Interaction> interactions = new HashSet<>();
    private final List<InteractionErrorHandler> errorHandlers = new ArrayList<>();
    private final List<InteractionEventProcessor> eventProcessors = new ArrayList<>();
    private InteractionService interactionService;

    @Override
    public void onClassDiscovered(Class<?> clazz) {
        if (clazz.isAnnotationPresent(RdiService.class)) {
            return;
        }
        if (Interaction.class.isAssignableFrom(clazz) && clazz.isAnnotationPresent(AutoRegister.class)) {
            interactions.add(instanceCache.getInstance(clazz.asSubclass(Interaction.class)));
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
                .matchType(Interaction.class, o -> o.getClass().isAnnotationPresent(AutoRegister.class), interactions::add)
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
        Objects.requireNonNull(interactionService);
        interactions.forEach(interaction -> interaction.register(interactionService));
        interactionService.setErrorHandler(ConfigUtils.selectImplementation(InteractionErrorHandler.class, errorHandlers)
                .orElse(InteractionErrorHandler.NO_OP));
        interactionService.setEventProcessor(ConfigUtils.selectImplementation(InteractionEventProcessor.class, eventProcessors)
                .orElse(InteractionEventProcessor.NO_OP));
        return interactionService.handleCommands();
    }
}
