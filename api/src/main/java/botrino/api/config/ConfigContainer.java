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

import java.util.Collection;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;

import static java.util.stream.Collectors.toMap;

/**
 * Contains the configuration for a specific bot.
 */
public final class ConfigContainer {

    private final Map<Class<?>, ConfigObject> configMap;

    private ConfigContainer(Map<Class<?>, ConfigObject> configMap) {
        this.configMap = configMap;
    }

    /**
     * Creates a new {@link ConfigContainer} with the given collection of config objects.
     *
     * @param configObjects the config objects held by this {@link ConfigContainer}
     * @return a new {@link ConfigContainer}
     */
    public static ConfigContainer of(Collection<? extends ConfigObject> configObjects) {
        Objects.requireNonNull(configObjects);
        return new ConfigContainer(configObjects.stream().collect(toMap(Object::getClass, Function.identity())));
    }

    /**
     * Gets the configuration object of the given type.
     *
     * @param type the type of configuration object to get
     * @param <C>  the actual type of the configuration object
     * @return the configuration object
     * @throws ConfigException if no configuration exists for the given type
     */
    public <C extends ConfigObject> C get(Class<C> type) {
        ConfigObject c = configMap.get(type);
        if (c == null) {
            throw new ConfigException("No configuration object found for type " + type.getName());
        }
        return type.cast(c);
    }
}
