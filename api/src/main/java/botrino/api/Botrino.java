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
import botrino.api.config.ConfigReader;
import botrino.api.config.LoginHandler;
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
import reactor.util.Logger;
import reactor.util.Loggers;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UncheckedIOException;
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

/**
 * Represents the entry point of an application based on Botrino.
 */
public final class Botrino {

    private static final Logger LOGGER = Loggers.getLogger(Botrino.class);
    private static final String API_VERSION_TXT = "apiVersion.txt";
    public static final String API_VERSION = readApiVersion();

    /**
     * Starts the Botrino application. It forwards the arguments of the main method, the first argument is interpreted
     * as the path to the botrino home directory in the file system, where it will find the configuration file.
     * <p>
     * This method will execute the following in this order:
     * <ol>
     *     <li>Finds all modules in the module path annotated with @{@link BotModule}</li>
     *     <li>Reads all classes contained in these modules, and processes them according to the features brought by
     *     these classes (configuration entries, services, commands, etc)</li>
     *     <li>Loads the configuration file using the configuration entries found in previous step</li>
     *     <li>Loads all services in a {@link RdiServiceContainer} and instantiates all of them, including the
     *     {@link GatewayDiscordClient} which will trigger the login process</li>
     *     <li>The method blocks until the bot disconnects</li>
     * </ol>
     *
     * @param args the args forwarded from the main method
     */
    public static void run(String[] args) {
        try {
            var botDir = Path.of(args.length == 0 ? "." : args[0]);
            var classes = scanBotModules();
            var configEntries = new HashSet<Class<?>>();
            var configReaders = new ArrayList<Class<? extends ConfigReader>>();
            var loginHandlers = new ArrayList<Class<? extends LoginHandler>>();
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
                if (ConfigReader.class.isAssignableFrom(clazz)) {
                    LOGGER.debug("Discovered config reader {}", clazz.getName());
                    configReaders.add(clazz.asSubclass(ConfigReader.class));
                }
                if (LoginHandler.class.isAssignableFrom(clazz)) {
                    LOGGER.debug("Discovered login handler {}", clazz.getName());
                    loginHandlers.add(clazz.asSubclass(LoginHandler.class));
                }
                extensions.forEach(ext -> ext.onClassDiscovered(clazz));
            }
            var configReader = ConfigUtils.selectImplementationClass(ConfigReader.class, configReaders)
                    .<ConfigReader>map(ConfigUtils::instantiate)
                    .orElseGet(() -> new ConfigReader() {});
            var loginHandler = ConfigUtils.selectImplementationClass(LoginHandler.class, loginHandlers)
                    .<LoginHandler>map(ConfigUtils::instantiate)
                    .orElseGet(() -> new LoginHandler() {});
            configEntries.add(BotConfig.class);
            configEntries.add(I18nConfig.class);

            var objectMapper = configReader.createConfigObjectMapper();
            var configJson = configReader.loadConfigJson(botDir);
            var configObjects = ConfigParser.create(objectMapper, configEntries).parse(configJson);
            var configContainerDescriptor = ServiceDescriptor.builder(ServiceReference.ofType(ConfigContainer.class))
                    .setFactoryMethod(staticFactory("of", ConfigContainer.class,
                            value(configObjects, Map.class)))
                    .build();
            var gatewayRef = ServiceReference.ofType(GatewayDiscordClient.class);
            var loginHandlerDescriptor = ServiceDescriptor.builder(gatewayRef)
                    .setFactoryMethod(externalStaticFactory(LoginHandler.class, "login", Mono.class,
                            value(loginHandler, LoginHandler.class),
                            ref(configContainerDescriptor.getServiceReference())))
                    .build();

            // Init RDI service container
            var serviceContainer = RdiServiceContainer.create(RdiConfig.builder()
                    .fromServiceFinder(AnnotationServiceFinder.create(serviceClasses))
                    .fromServiceFinder(() -> extensions.stream()
                            .flatMap(ext -> ext.provideExtraServices().stream())
                            .collect(Collectors.toSet()))
                    .registerService(configContainerDescriptor)
                    .registerService(loginHandlerDescriptor)
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

    private static String readApiVersion() {
        try (var in = Botrino.class.getResourceAsStream(API_VERSION_TXT)) {
            if (in == null) {
                throw new RuntimeException(API_VERSION_TXT + " not present in JAR");
            }
            try (var reader = new InputStreamReader(in);
                 var buffered = new BufferedReader(reader)) {
                return buffered.lines().collect(joining(System.lineSeparator()));
            }
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
