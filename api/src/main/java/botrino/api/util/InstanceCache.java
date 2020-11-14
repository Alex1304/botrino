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
package botrino.api.util;

/**
 * Utility class that allows to get an instance of any class that has a public no-arg constructor, and cache the
 * instance so that subsequent requests for the same class return the same instance. It uses a {@link ClassValue}
 * internally.
 */
public final class InstanceCache {

    private final ClassValue<Object> cache = new ClassValue<>() {
        @Override
        protected Object computeValue(Class<?> type) {
            return ConfigUtils.instantiateNoArg(type);
        }
    };

    private InstanceCache() {
    }

    /**
     * Creates a new {@link InstanceCache} initialized with an empty cache of instances.
     *
     * @return a new {@link InstanceCache}
     */
    public static InstanceCache create() {
        return new InstanceCache();
    }

    /**
     * Gets an instance of the given type, either by creating it if it doesn't exist, or by returning the instance
     * already cached.
     *
     * @param type the class representing the type to get an instance for
     * @param <R>  the target type
     * @return the instance
     * @throws RuntimeException if somethings goes wrong during the instantiation (for example, if the given type does
     *                          not represent a concrete class with a no-arg constructor, or if the instantiation fails
     *                          for any reason). In all such cases, the {@link RuntimeException} contains the {@code
     *                          cause} which can be typically {@link InstantiationException}, {@link
     *                          NoSuchMethodException} or {@link IllegalAccessException}.
     */
    public <R> R getInstance(Class<R> type) {
        return type.cast(cache.get(type));
    }
}
