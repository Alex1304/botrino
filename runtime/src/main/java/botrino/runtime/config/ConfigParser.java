package botrino.runtime.config;

import botrino.framework.config.ConfigEntry;
import botrino.framework.config.ConfigException;
import botrino.framework.config.ConfigObject;
import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonParser;
import org.apache.commons.lang3.ClassUtils;
import reactor.util.function.Tuple2;
import reactor.util.function.Tuples;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public final class ConfigParser {

    private final Map<String, Tuple2<Gson, Type>> expectedEntries;

    private ConfigParser(Map<String, Tuple2<Gson, Type>> expectedEntries) {
        this.expectedEntries = expectedEntries;
    }

    public static ConfigParser create(Set<ConfigEntry<?>> expectedEntries) {

        return new ConfigParser(expectedEntries.stream()
                .map(entry -> {
                    var gsonBuilder = new GsonBuilder()
                            .setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES);
                    return Tuples.of(entry.define(gsonBuilder),
                            Tuples.of(gsonBuilder.create(), extractGenericType(entry)));
                })
                .collect(Collectors.toMap(Tuple2::getT1, Tuple2::getT2)));
    }

    private static Type extractGenericType(ConfigEntry<?> entry) {
        var allSuperclassesAndInterfacesStream = Stream.concat(
                ClassUtils.getAllInterfaces(entry.getClass()).stream(),
                ClassUtils.getAllSuperclasses(entry.getClass()).stream());
        return allSuperclassesAndInterfacesStream
                .flatMap(clazz -> Arrays.stream(clazz.getGenericInterfaces()))
                .filter(ParameterizedType.class::isInstance)
                .map(ParameterizedType.class::cast)
                .filter(type -> type.getRawType() == ConfigEntry.class)
                .flatMap(parameterizedType -> Arrays.stream(parameterizedType.getActualTypeArguments()))
                .findAny()
                .orElse(Object.class);
    }

    public Set<ConfigObject> parse(String rawJson) {
        var configObjects = new HashSet<ConfigObject>();
        var notFound = new HashSet<>(expectedEntries.keySet());
        var object = JsonParser.parseString(rawJson).getAsJsonObject();
        for (var property : object.entrySet()) {
            var name = property.getKey();
            var element = property.getValue();
            var gsonWithTargetType = expectedEntries.get(name);
            if (gsonWithTargetType != null) {
                var gson = gsonWithTargetType.getT1();
                var targetType = gsonWithTargetType.getT2();
                notFound.remove(name);
                ConfigObject configObject = gson.fromJson(element, targetType);
                var failures = configObject.validate();
                if (!failures.isEmpty()) {
                    throw new ConfigException("The configuration entry '" + name
                            + "' contains the following error(s): " + failures);
                }
                configObjects.add(configObject);
            }
        }
        if (!notFound.isEmpty()) {
            throw new ConfigException("The following entries are missing in the configuration: " + notFound);
        }
        return configObjects;
    }
}
