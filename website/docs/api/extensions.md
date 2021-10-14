---
title: Extensions
---

When you start your application, Botrino automatically loads all the classes present in bot modules. According to the type of classes that are discovered, an action will be performed on them such as registering a service or adding a configuration entry. Extensions allow you to hook into this module scanning process to add your own logic when classes are loaded.

## Declaring an extension

Unlike other components of the framework, extensions do not need to reside in a module annotated with `@BotModule`. Think of extensions like plugins for the framework itself and not for your bot application directly. Your module does not need to be `open` either, extensions are loaded via  [`java.util.ServiceLoader`](https://docs.oracle.com/en/java/javase/11/docs/api/java.base/java/util/ServiceLoader.html).

The first step is to create a class implementing the `BotrinoExtension` interface:

```java
package com.example.myproject;

import botrino.api.extension.BotrinoExtension;
import com.github.alex1304.rdi.config.ServiceDescriptor;
import reactor.core.publisher.Mono;

import java.util.Set;

public final class MyExtension implements BotrinoExtension {

    @Override
    public void onClassDiscovered(Class<?> clazz) {
        // ...
    }

    @Override
    public void onServiceCreated(Object o) {
        // ...
    }

    @Override
    public Set<ServiceDescriptor> provideExtraServices() {
        // ...
    }

    @Override
    public Set<Class<?>> provideExtraDiscoverableClasses() {
        // ...
    }

    @Override
    public Mono<Void> finishAndJoin() {
        // ...
    }
}
```

Before going into the details of the methods to implement, let's register this class as a provider for `BotrinoExtension`. This is done via the `module-info.java`:

```java
import botrino.api.extension.BotrinoExtension;
import com.example.extension.MyExtension;

module com.example.extension {

    requires botrino.api;
    provides BotrinoExtension with MyExtension;
}
```

:::info
You don't *have* to create a separate module just for your extension. It is totally fine to add the `provides` directive directly in your `@BotModule`, this example just shows that you are not required to.
:::

## Implementing an extension

Let's review each of the methods of `BotrinoExtension` to implement.

### `void onClassDiscovered(Class<?> clazz)`

This is a callback method invoked each time a class is discovered in a bot module. In most cases, you will check if this class implements a specific interface or is annotated with a specific annotation, and do some processing when it is relevant to do so.

:::caution
If you intend to create an instance of the class, it is highly recommended to skip classes annotated with `@RdiService` from this method, as they are supposed to be instantiated by the RDI container. That's why the `onServiceCreated(Object)` method exists.
:::

### `void onServiceCreated(Object o)`

This is a callback method invoked each time a service is created. It allows to execute some action on the service object right after it's created.

:::info
As this method returns `void`, it is not suited for performing reactive tasks. Instead, store the service object in a field and perform this task in `finishAndJoin()`.
:::

### `Set<ServiceDescriptor> provideExtraServices()`

Even though the extension may not be inside a bot module, it is still possible to register services that will be exposed to the bot application. You can do so via this method, allowing you to provide a set of [RDI service descriptors](https://alex1304.github.io/rdi/docs/service-descriptors). This method is only useful if you want to provide complex services that require writing raw descriptors (for example registering a class from a third-party library as a service with a custom name). For simple services maintained by yourself, you can use RDI annotations and make the annotated class discoverable via `provideExtraDiscoverableClasses()` instead of doing it via this method.

### `Set<Class<?>> provideExtraDiscoverableClasses()`

With this method you can explicitly specify a set of classes that Botrino will pick up just like if they were inside a bot module. It is guaranteed that each class contained in the set will eventually be passed to the `onClassDiscovered(Class)` method (unless they have the `@Exclude` annotation). As said earlier, it can be used as an alternative way to provide extra services, if the class contained in the set is annotated with RDI annotations. It can also be used to register new configuration entries, or new things you're defining yourself in your own extension!

### `Mono<Void> finishAndJoin()`

This is the last method that is invoked during the startup sequence. It allows you to perform a task, possibly reactive, based on the classes and objects you were able to collect via previous invocations of `onClassDiscovered(Class)` and `onServiceCreated(Object)`. The "join" part of this method's name indicates the fact that the returned reactive sequence does not need to be a finite source: you can use it to start processes living during the entire lifetime of the application, for example installing event listeners or starting a web server. The subscription to the returned `Mono` is automatically cancelled once the bot disconnects, allowing the application to shut down properly.

:::warning
If an exception is thrown or an error is emitted via the `Mono` from this method, the exception will propagate to the main thread, which will result in the bot to forcefully disconnect and the application to be terminated.
:::

## A concrete example: the interaction library

The [interaction library](../interaction-library/overview.md) of Botrino provides an implementation of `BotrinoExtension`, which is in charge of collecting the classes implementing `XxxInteractionListener`, `InteractionErrorHandler`, `InteractionEventProcessor` and so on, in order to register them in the `InteractionService`. It also exposes a new entry in `config.json` that allows to construct the [configuration](../interaction-library/configuration.md) object.

You can check the source code of the extension class of the interaction library on GitHub [here](https://github.com/Alex1304/botrino/blob/main/interaction/src/main/java/botrino/interaction/InteractionExtension.java). A few things to note to understand the code:
* Classes with the `@RdiService` annotation are ignored, since we want to use the instance created by RDI in case `XxxInteractionListener`, `InteractionErrorHandler` and `InteractionEventProcessor` are declared as services.
* An `InstanceCache` is used so that the same instance can be reused in case a class implements more than one interface.
* `InteractionService` utilizes RDI annotations, so we provide it via `provideExtraDiscoverableClasses()` and not `provideExtraServices()`.
* All implementations that were found are finally registered in the `finishAndJoin()` method, which runs the interaction service at the end.
