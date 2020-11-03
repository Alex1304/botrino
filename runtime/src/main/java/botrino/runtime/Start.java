package botrino.runtime;

import botrino.framework.config.ConfigContainer;
import botrino.framework.config.ConfigEntry;
import botrino.framework.config.ConfigException;
import botrino.framework.config.DiscordLoginHandler;
import botrino.framework.config.bot.BotConfigEntry;
import botrino.framework.util.InstanceCache;
import botrino.runtime.config.ConfigParser;
import com.github.alex1304.rdi.RdiServiceContainer;
import com.github.alex1304.rdi.ServiceReference;
import com.github.alex1304.rdi.config.RdiConfig;
import com.github.alex1304.rdi.config.ServiceDescriptor;
import com.github.alex1304.rdi.finder.annotation.AnnotationServiceFinder;
import com.github.alex1304.rdi.finder.annotation.RdiService;
import com.google.gson.JsonParser;
import discord4j.core.GatewayDiscordClient;
import reactor.core.Exceptions;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.lang.reflect.Modifier;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Collectors;

import static com.github.alex1304.rdi.config.FactoryMethod.externalStaticFactory;
import static com.github.alex1304.rdi.config.FactoryMethod.staticFactory;
import static com.github.alex1304.rdi.config.Injectable.ref;
import static com.github.alex1304.rdi.config.Injectable.value;

public class Start {

    private static final Path CONFIG_JSON = Path.of(".", "config.json");
    private static final Path MODULES_JSON = Path.of(".", "modules.json");

    public static void main(String[] args) throws IOException {
        var botDir = Path.of(args[0]);
        var configJson = Files.readString(botDir.resolve(CONFIG_JSON));
        var modulesJson = Files.readString(botDir.resolve(MODULES_JSON));

        // Load modules
        Set<Class<?>> classes = new HashSet<>();
        for (var element : JsonParser.parseString(modulesJson).getAsJsonArray()) {
            classes.addAll(scanModule(element.getAsString()));
        }

        // Extract classes that are services, config entries, and commands
        var instanceCache = InstanceCache.create();
//        Set<Class<?>> commands = new HashSet<>();
        Set<ConfigEntry<?>> configEntries = new HashSet<>();
        List<DiscordLoginHandler> discordLoginHandlers = new ArrayList<>();
        Set<Class<?>> serviceClasses = new HashSet<>();
        for (var clazz : classes) {
            if (clazz.isInterface() || Modifier.isAbstract(clazz.getModifiers())) {
                continue;
            }
//            if (clazz.isAnnotationPresent(CommandDescriptor.class)) {
//                commands.add(clazz);
//            }
            if (clazz.isAnnotationPresent(RdiService.class)) {
                serviceClasses.add(clazz);
            }
            if (ConfigEntry.class.isAssignableFrom(clazz)) {
                configEntries.add(instanceCache.getInstance(clazz.asSubclass(ConfigEntry.class)));
            }
            if (DiscordLoginHandler.class.isAssignableFrom(clazz)) {
                discordLoginHandlers.add(instanceCache.getInstance(clazz.asSubclass(DiscordLoginHandler.class)));
            }
        }
        if (discordLoginHandlers.size() > 1) {
            throw new ConfigException("More than one subclass of DiscordLoginHandler were found: "
                    + discordLoginHandlers.stream()
                            .map(Object::getClass)
                            .map(Class::getName)
                            .collect(Collectors.joining(", ")));
        }
        DiscordLoginHandler loginHandler = discordLoginHandlers.isEmpty()
                ? new DefaultDiscordLoginHandler() : discordLoginHandlers.get(0);
        configEntries.add(new BotConfigEntry());

        var configObjects = ConfigParser.create(configEntries).parse(configJson);
        var configContainerDescriptor = ServiceDescriptor.builder(ServiceReference.ofType(ConfigContainer.class))
                .setFactoryMethod(staticFactory("of", ConfigContainer.class, value(configObjects, Collection.class)))
                .build();
        var loginHandlerDescriptor = ServiceDescriptor.builder(ServiceReference.ofType(DiscordLoginHandler.class))
                .setFactoryMethod(externalStaticFactory(Start.class, "login", Mono.class,
                        value(loginHandler, DiscordLoginHandler.class),
                        ref(configContainerDescriptor.getServiceReference())))
                .build();

        // Init RDI service container
        var serviceContainer = RdiServiceContainer.create(RdiConfig.builder()
                .fromServiceFinder(AnnotationServiceFinder.create(serviceClasses))
                .registerService(configContainerDescriptor)
                .registerService(loginHandlerDescriptor)
                .build());

        // Start everything
        var serviceMonos = serviceClasses.stream()
                .map(ServiceReference::ofType)
                .map(serviceContainer::getService)
                .collect(Collectors.toList());
        Flux.merge(serviceMonos)
                .blockLast();
    }

    private static Set<Class<?>> scanModule(String moduleName) throws IOException {
        try (var moduleReader = Start.class.getModule().getLayer().configuration()
                .findModule(moduleName)
                .orElseThrow(() -> new IllegalArgumentException("The given module was "
                        + "not found in the module path"))
                .reference()
                .open(); var list = moduleReader.list()) {
            return list.filter(resource -> resource.endsWith(".class") && !resource.contains("-"))
                    .map(resource -> resource.substring(0, resource.length() - ".class".length())
                            .replace(FileSystems.getDefault().getSeparator(), "."))
                    .map(className -> {
                        try {
                            return Class.forName(className);
                        } catch (ClassNotFoundException e) {
                            throw Exceptions.propagate(e);
                        }
                    })
                    .collect(Collectors.toUnmodifiableSet());
        }
    }

    private static Mono<GatewayDiscordClient> login(DiscordLoginHandler loginHandler, ConfigContainer configContainer) {
        return loginHandler.login(configContainer);
    }
}
