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
package botrino.api.config;

import botrino.api.annotation.ConfigEntry;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import reactor.util.function.Tuple2;
import reactor.util.function.Tuples;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * A parser that is in charge of instantiating the configuration objects according the the raw JSON input. This class
 * should generally not be used directly as it is already managed by Botrino.
 */
public final class ConfigParser {

    private final ObjectMapper objectMapper;
    private final Map<String, Class<?>> expectedEntries;

    private ConfigParser(ObjectMapper objectMapper, Map<String, Class<?>> expectedEntries) {
        this.objectMapper = objectMapper;
        this.expectedEntries = expectedEntries;
    }

    public static ConfigParser create(ObjectMapper objectMapper, Set<Class<?>> expectedEntries) {
        return new ConfigParser(objectMapper, expectedEntries.stream()
                .filter(clazz -> clazz.isAnnotationPresent(ConfigEntry.class))
                .map(clazz -> Tuples.of(clazz.getAnnotation(ConfigEntry.class).value(), clazz))
                .collect(Collectors.toMap(Tuple2::getT1, Tuple2::getT2)));
    }

    public Map<String, Object> parse(String rawJson) {
        try {
            var configObjects = new HashMap<String, Object>();
            var notFound = new HashSet<>(expectedEntries.keySet());
            var object = objectMapper.readTree(rawJson);
            var fields = object.fields();
            while (fields.hasNext()) {
                var field = fields.next();
                var name = field.getKey();
                var element = field.getValue();
                var targetType = expectedEntries.get(name);
                if (targetType != null) {
                    notFound.remove(name);
                    var configObject = objectMapper.treeToValue(element, targetType);
                    configObjects.put(name, configObject);
                }
            }
            if (!notFound.isEmpty()) {
                throw new ConfigException("The following entries are missing in the configuration: " + notFound);
            }
            return configObjects;
        } catch (JsonProcessingException e) {
            throw new ConfigException("Something went wrong when parsing configuration file", e);
        }
    }
}
