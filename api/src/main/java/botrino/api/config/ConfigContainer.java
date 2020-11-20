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

import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Objects;

/**
 * Contains the configuration for the bot.
 */
public final class ConfigContainer {

    private final Map<String, ?> configMap;

    private ConfigContainer(Map<String, ?> configMap) {
        this.configMap = configMap;
    }

    /**
     * Creates a new {@link ConfigContainer} with a {@link Map} containing the configuration objects associated by their
     * name.
     *
     * @param configObjects the config objects held by this {@link ConfigContainer}
     * @return a new {@link ConfigContainer}
     */
    public static ConfigContainer of(Map<String, ?> configObjects) {
        Objects.requireNonNull(configObjects);
        return new ConfigContainer(configObjects);
    }

    /**
     * Gets the configuration object of the given type. The class given in argument is expected to have a {@link
     * ConfigEntry} annotation, otherwise {@link IllegalArgumentException} will be thrown.
     *
     * @param type the type of configuration object to get
     * @param <C>  the actual type of the configuration object
     * @return the configuration object associated to the given class
     * @throws IllegalArgumentException if the given class does not have the {@link ConfigEntry} annotation
     * @throws NoSuchElementException   if no configuration exists for the given type
     */
    public <C> C get(Class<C> type) {
        var annotation = type.getAnnotation(ConfigEntry.class);
        if (annotation == null) {
            throw new IllegalArgumentException("The class " + type.getName() + " does not have the @ConfigEntry " +
                    "annotation");
        }
        var c = configMap.get(annotation.value());
        if (c == null) {
            throw new NoSuchElementException("No configuration object found for '" + annotation.value() + "' " +
                    "(of type " + type.getName() + ")");
        }
        return type.cast(c);
    }
}
