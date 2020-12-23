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
package botrino.command;

import botrino.api.extension.BotrinoExtension;
import botrino.api.util.ConfigUtils;
import botrino.api.util.InstanceCache;
import botrino.command.annotation.TopLevelCommand;
import botrino.command.config.CommandConfig;
import com.github.alex1304.rdi.config.ServiceDescriptor;
import com.github.alex1304.rdi.finder.annotation.RdiService;
import reactor.core.publisher.Mono;

import java.util.*;

public final class CommandExtension implements BotrinoExtension {

    private final InstanceCache instanceCache = InstanceCache.create();
    private final Set<Command> commands = new HashSet<>();
    private final List<CommandErrorHandler> errorHandlers = new ArrayList<>();
    private final List<CommandEventProcessor> eventProcessors = new ArrayList<>();
    private CommandService commandService;

    @Override
    public void onClassDiscovered(Class<?> clazz) {
        if (clazz.isAnnotationPresent(RdiService.class)) {
            return;
        }
        if (Command.class.isAssignableFrom(clazz) && clazz.isAnnotationPresent(TopLevelCommand.class)) {
            commands.add(instanceCache.getInstance(clazz.asSubclass(Command.class)));
        }
        if (CommandErrorHandler.class.isAssignableFrom(clazz)) {
            errorHandlers.add(instanceCache.getInstance(clazz.asSubclass(CommandErrorHandler.class)));
        }
        if (CommandEventProcessor.class.isAssignableFrom(clazz)) {
            eventProcessors.add(instanceCache.getInstance(clazz.asSubclass(CommandEventProcessor.class)));
        }
    }

    @Override
    public void onServiceCreated(Object serviceInstance) {
        if (serviceInstance instanceof CommandService) {
            this.commandService = (CommandService) serviceInstance;
        }
        if (serviceInstance instanceof Command
                && serviceInstance.getClass().isAnnotationPresent(TopLevelCommand.class)) {
            commands.add((Command) serviceInstance);
        }
        if (serviceInstance instanceof CommandErrorHandler) {
            errorHandlers.add((CommandErrorHandler) serviceInstance);
        }
        if (serviceInstance instanceof CommandEventProcessor) {
            eventProcessors.add((CommandEventProcessor) serviceInstance);
        }
    }

    @Override
    public Set<ServiceDescriptor> provideExtraServices() {
        return Set.of();
    }

    @Override
    public Set<Class<?>> provideExtraDiscoverableClasses() {
        return Set.of(CommandService.class, CommandConfig.class);
    }

    @Override
    public Mono<Void> finishAndJoin() {
        Objects.requireNonNull(commandService);
        commands.forEach(commandService::addTopLevelCommand);
        commandService.setErrorHandler(ConfigUtils.selectImplementation(CommandErrorHandler.class, errorHandlers)
                .orElse(CommandErrorHandler.NO_OP));
        commandService.setEventProcessor(ConfigUtils.selectImplementation(CommandEventProcessor.class, eventProcessors)
                .orElse(CommandEventProcessor.NO_OP));
        return commandService.listenToCommands();
    }
}
