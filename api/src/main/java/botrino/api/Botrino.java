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
package botrino.api;

import botrino.api.annotation.BotModule;
import botrino.api.annotation.ConfigEntry;
import botrino.api.annotation.Exclude;
import botrino.api.config.ConfigContainer;
import botrino.api.config.ConfigParser;
import botrino.api.config.DefaultStartupHandler;
import botrino.api.config.StartupHandler;
import botrino.api.config.object.BotConfig;
import botrino.api.config.object.I18nConfig;
import botrino.api.extension.BotrinoExtension;
import botrino.api.util.ConfigUtils;
import com.github.alex1304.rdi.RdiServiceContainer;
import com.github.alex1304.rdi.ServiceReference;
import com.github.alex1304.rdi.config.RdiConfig;
import com.github.alex1304.rdi.config.ServiceDescriptor;
import com.github.alex1304.rdi.finder.annotation.AnnotationServiceFinder;
import com.github.alex1304.rdi.finder.annotation.RdiService;
import discord4j.core.GatewayDiscordClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;
import reactor.util.Logger;
import reactor.util.Loggers;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Collectors;

import static com.github.alex1304.rdi.config.FactoryMethod.externalStaticFactory;
import static com.github.alex1304.rdi.config.FactoryMethod.staticFactory;
import static com.github.alex1304.rdi.config.Injectable.ref;
import static com.github.alex1304.rdi.config.Injectable.value;
import static java.util.stream.Collectors.joining;
import static java.util.stream.Collectors.toUnmodifiableSet;

public final class Botrino {

    private static final Logger LOGGER = Loggers.getLogger(Botrino.class);
    private static final String API_VERSION_TXT = "META-INF/botrino/apiVersion.txt";

    public static void run() {
        run(new String[0]);
    }

    public static void run(String[] args) {
        try {
            var botDir = Path.of(args.length == 0 ? "." : args[0]);
            var classes = scanBotModules();
            var configEntries = new HashSet<Class<?>>();
            var startupHandlers = new ArrayList<Class<? extends StartupHandler>>();
            var serviceClasses = new HashSet<Class<?>>();
            var extensions = ServiceLoader.load(BotrinoExtension.class)
                    .stream()
                    .map(ServiceLoader.Provider::get)
                    .collect(Collectors.toSet());
            classes.addAll(extensions.stream()
                    .flatMap(ext -> ext.provideExtraDiscoverableClasses().stream())
                    .collect(Collectors.toSet()));
            for (var clazz : classes) {
                if (clazz.isAnonymousClass() || clazz.isAnnotationPresent(Exclude.class)) {
                    continue;
                }
                if (clazz.isAnnotationPresent(RdiService.class)) {
                    LOGGER.debug("Discovered service class {}", clazz.getName());
                    serviceClasses.add(clazz);
                }
                if (clazz.isAnnotationPresent(ConfigEntry.class)) {
                    LOGGER.debug("Discovered config entry {}", clazz.getName());
                    configEntries.add(clazz);
                }
                if (StartupHandler.class.isAssignableFrom(clazz)) {
                    LOGGER.debug("Discovered discord login handler {}", clazz.getName());
                    startupHandlers.add(clazz.asSubclass(StartupHandler.class));
                }
                extensions.forEach(ext -> ext.onClassDiscovered(clazz));
            }
            var startupHandler = ConfigUtils.selectImplementationClass(StartupHandler.class, startupHandlers)
                    .<StartupHandler>map(ConfigUtils::instantiate)
                    .orElseGet(DefaultStartupHandler::new);
            configEntries.add(BotConfig.class);
            configEntries.add(I18nConfig.class);

            var objectMapper = startupHandler.createConfigObjectMapper();
            var configJson = startupHandler.loadConfigJson(botDir);
            var configObjects = ConfigParser.create(objectMapper, configEntries).parse(configJson);
            var configContainerDescriptor = ServiceDescriptor.builder(ServiceReference.ofType(ConfigContainer.class))
                    .setFactoryMethod(staticFactory("of", ConfigContainer.class,
                            value(configObjects, Map.class)))
                    .build();
            var gatewayRef = ServiceReference.ofType(GatewayDiscordClient.class);
            var loginHandlerDescriptor = ServiceDescriptor.builder(gatewayRef)
                    .setFactoryMethod(externalStaticFactory(Botrino.class, "login", Mono.class,
                            value(startupHandler, StartupHandler.class),
                            ref(configContainerDescriptor.getServiceReference())))
                    .build();
            var apiVersionDescriptor = ServiceDescriptor.builder(ServiceReference.of("apiVersion", String.class))
                    .setFactoryMethod(externalStaticFactory(Botrino.class, "apiVersion", Mono.class))
                    .build();

            // Init RDI service container
            var serviceContainer = RdiServiceContainer.create(RdiConfig.builder()
                    .fromServiceFinder(AnnotationServiceFinder.create(serviceClasses))
                    .fromServiceFinder(() -> extensions.stream()
                            .flatMap(ext -> ext.provideExtraServices().stream())
                            .collect(Collectors.toSet()))
                    .registerService(configContainerDescriptor)
                    .registerService(loginHandlerDescriptor)
                    .registerService(apiVersionDescriptor)
                    .build());

            // Initialize all services and await logout
            Flux.fromIterable(serviceClasses)
                    .flatMap(clazz -> serviceContainer.getService(ServiceReference.ofType(clazz)))
                    .doOnNext(service -> extensions.forEach(ext -> ext.onServiceCreated(service)))
                    .then(serviceContainer.getService(gatewayRef))
                    .flatMap(gateway -> gateway.onDisconnect()
                            .and(Mono.when(extensions.stream()
                                    .map(BotrinoExtension::finishAndJoin)
                                    .collect(Collectors.toList()))
                                    .takeUntilOther(gateway.onDisconnect())))
                    .block();
        } catch (Exception e) {
            if (e instanceof RuntimeException) {
                throw (RuntimeException) e;
            }
            throw new RuntimeException(e);
        }
    }

    private static Set<Class<?>> scanBotModules() throws IOException {
        var classes = new HashSet<Class<?>>();
        var moduleNames = ModuleLayer.boot().modules()
                .stream()
                .filter(module -> module.isAnnotationPresent(BotModule.class))
                .map(Module::getName)
                .collect(toUnmodifiableSet());
        var moduleConfig = ModuleLayer.boot().configuration();
        for (var moduleName : moduleNames) {
            try (var moduleReader = moduleConfig.findModule(moduleName)
                    .orElseThrow()
                    .reference()
                    .open();
                 var resources = moduleReader.list()) {
                classes.addAll(resources.filter(resource -> resource.endsWith(".class") && !resource.contains("-"))
                        .map(resource -> resource.substring(0, resource.length() - ".class".length())
                                .replace(FileSystems.getDefault().getSeparator(), "."))
                        .map(className -> {
                            try {
                                return Class.forName(className);
                            } catch (ClassNotFoundException e) {
                                throw new RuntimeException(e);
                            }
                        })
                        .collect(toUnmodifiableSet()));
            }
        }
        return classes;
    }

    public static Mono<GatewayDiscordClient> login(StartupHandler startupHandler, ConfigContainer configContainer) {
        return startupHandler.login(configContainer);
    }

    public static Mono<String> apiVersion() {
        return Mono.fromCallable(Botrino::readApiVersion)
                .subscribeOn(Schedulers.boundedElastic());
    }

    private static String readApiVersion() throws IOException {
        try (var in = Botrino.class.getResourceAsStream(API_VERSION_TXT)) {
            if (in == null) {
                throw new RuntimeException(API_VERSION_TXT + " not present in JAR");
            }
            try (var reader = new InputStreamReader(in);
                 var buffered = new BufferedReader(reader)) {
                return buffered.lines().collect(joining(System.lineSeparator()));
            }
        }
    }
}
