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

import botrino.api.annotation.Primary;
import botrino.api.config.ConfigException;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.UndeclaredThrowableException;
import java.util.List;
import java.util.Optional;

/**
 * Utilities for things happening at configuration time.
 */
public final class ConfigUtils {

    /**
     * Selects one implementation among the given list. If the given list is empty, an empty Optional is returned . If
     * there is one element, this element is returned immediately. If there is more than one element, then exactly one
     * element is expected to have the {@link Primary} annotation on its runtime class, which is then returned. In all
     * other cases, a {@link ConfigException} will be thrown with an informative message.
     *
     * @param supertype the supertype that the elements are implementing
     * @param impls     the list of implementations
     * @param <T>       the actual type of the supertype
     * @return the selected implementation, or empty if the given list was empty
     * @throws ConfigException if the implementation could not be selected because the list contained more than one
     *                         element and either none or more than one of them were annotated with {@link Primary}.
     */
    public static <T> Optional<T> selectImplementation(Class<T> supertype, List<? extends T> impls) {
        if (impls.isEmpty()) {
            return Optional.empty();
        }
        if (impls.size() == 1) {
            return Optional.of(impls.get(0));
        }
        return impls.stream()
                .map(supertype::cast)
                .filter(impl -> impl.getClass().isAnnotationPresent(Primary.class))
                .reduce((impl1, impl2) -> {
                    throw new ConfigException("More than one implementation of " + supertype.getName() + " were " +
                            "marked @Primary. There must be exactly one, otherwise it is not possible to determine " +
                            "which one to use.");
                })
                .or(() -> {
                    throw new ConfigException("More than one implementation of " + supertype.getName() + " were " +
                            "found: " + impls + ". One of them must be marked @Primary in order to determine which " +
                            "one to use.");
                });
    }

    /**
     * Selects one implementation class among the given list. If the given list is empty, an empty Optional is returned
     * . If there is one element, this element is returned immediately. If there is more than one element, then exactly
     * one element is expected to have the {@link Primary} annotation, which is then returned. In all other cases, a
     * {@link ConfigException} will be thrown with an informative message.
     *
     * @param supertype the supertype that the elements are implementing
     * @param impls     the list of implementation classes
     * @param <T>       the actual type of the supertype
     * @return the selected implementation class, or empty if the given list was empty
     * @throws ConfigException if the implementation could not be selected because the list contained more than one
     *                         element and either none or more than one of them were annotated with {@link Primary}.
     */
    public static <T> Optional<Class<? extends T>> selectImplementationClass(Class<T> supertype,
                                                                             List<Class<? extends T>> impls) {
        if (impls.isEmpty()) {
            return Optional.empty();
        }
        if (impls.size() == 1) {
            return Optional.of(impls.get(0));
        }
        return impls.stream()
                .filter(impl -> impl.isAnnotationPresent(Primary.class))
                .reduce((impl1, impl2) -> {
                    throw new ConfigException("More than one implementation of " + supertype.getName() + " were " +
                            "marked @Primary. There must be exactly one, otherwise it is not possible to determine " +
                            "which one to use.");
                })
                .or(() -> {
                    throw new ConfigException("More than one implementation of " + supertype.getName() + " were " +
                            "found: " + impls + ". One of them must be marked @Primary in order to determine which " +
                            "one to use.");
                });
    }

    /**
     * Allows to create an instance of type, assuming there is a public no-arg constructor.
     *
     * @param type the type to instantiate
     * @param <T>  the actual type
     * @return the instance
     * @throws RuntimeException if somethings goes wrong during the instantiation (for example, if the given type does
     *                          not represent a concrete class with a no-arg constructor, or if the instantiation fails
     *                          for any reason). In all such cases, the {@link RuntimeException} contains the {@code
     *                          cause} which can be typically {@link InstantiationException}, {@link
     *                          NoSuchMethodException} or {@link IllegalAccessException}.
     */
    public static <T> T instantiate(Class<T> type) {
        try {
            return type.getConstructor().newInstance();
        } catch (InvocationTargetException e) {
            Throwable cause = e.getCause();
            if (cause instanceof RuntimeException) throw (RuntimeException) cause;
            if (cause instanceof Error) throw (Error) cause;
            throw new UndeclaredThrowableException(cause);
        } catch (InstantiationException
                | NoSuchMethodException
                | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }
}
