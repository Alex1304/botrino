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
package botrino.api.extension;

import botrino.api.annotation.BotModule;
import com.github.alex1304.rdi.config.ServiceDescriptor;
import com.github.alex1304.rdi.finder.annotation.RdiService;
import reactor.core.publisher.Mono;

import java.util.Set;

/**
 * Allows to add behavior during module path scanning on startup. It may collect classes implementing a specific
 * interface or annotated with a specific annotation within a module annotated with {@link BotModule} in order to do
 * special treatment on them. The command module will for example collect all classes implementing the Command interface
 * in order to add it in the command tree.
 * <p>
 * When dealing with classes that are services (with {@link RdiService} annotation), it is recommended not to
 * instantiate them manually and instead use the existing instance that can be collected via
 * {@link #onServiceCreated(Object)}.
 * <p>
 * Implementations of this interface must be added via a {@code provides} directive in {@code module-info.java} so that
 * Botrino can load it on startup.
 * <p>
 * Botrino will always execute methods of this interface on the {@code main} thread, so there is no need to make the
 * implementation thread-safe.
 */
public interface BotrinoExtension {

    /**
     * Callback method invoked when a class was discovered during module path scanning.
     *
     * @param clazz the class that was discovered
     */
    void onClassDiscovered(Class<?> clazz);

    /**
     * Callback method invoked when a service was instantiated. This is done after all classes have been discovered, so
     * a call to this method guarantees that {@link #onClassDiscovered(Class)} won't be called anymore.
     *
     * @param serviceInstance the instance of the service that was just created
     */
    void onServiceCreated(Object serviceInstance);

    /**
     * Allows the extension to provide extra services to be instantiated that is outside the scope of module path
     * scanning. Those extra services are expressed via RDI's {@link ServiceDescriptor}. Instances of the services
     * declared here can be later accessed during an invocation of {@link #onServiceCreated(Object)}.
     *
     * @return a set of service descriptors
     */
    Set<ServiceDescriptor> provideExtraServices();

    /**
     * Allows the extension to expose extra classes that normally cannot be discovered due to them being outside of a
     * module annotated with {@link BotModule}.
     *
     * @return a set of classes to be discoverable by Botrino
     */
    Set<Class<?>> provideExtraDiscoverableClasses();

    /**
     * This is the very last method invoked by Botrino before finishing startup. It allows to provide a reactive chain
     * that will be subscribed to by the {@code main} thread before blocking indefinitely and awaiting logout. It can
     * consist of installing event listeners, initializing some process, perform a post-startup cleanup task, etc. For
     * example this is used by the command extension to begin the subscription to message create events.
     * <p>
     * Since the returned chain is subscribed to directly on the {@code main} thread, it means that any error signal
     * emitted by the chain will terminate the JVM.
     *
     * @return a {@link Mono}
     */
    Mono<Void> finishAndJoin();
}
