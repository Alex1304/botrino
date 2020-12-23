---
title: Creating commands
---

Commands represent the main form of interaction that Discord bots have with users. Creating commands is a simple and straightforward process, with the ability to customize different aspects of them.

## Declaring a command

To declare a command, two options are possible.

:::caution
This subsection only shows how to **declare** commands, they won't work until they are **registered**. Registration of commands is documented in the next subsection.
:::

### Implementing the `Command` interface

Create a class that implements `Command` and override the `Mono<Void> run(CommandContext)` method:

```java
package com.example.myproject;

import botrino.command.Command;
import botrino.command.CommandContext;
import botrino.command.annotation.Alias;
import reactor.core.publisher.Mono;

import java.util.Set;

@Alias("ping")
public final class PingCommand implements Command {

    @Override
    public Mono<Void> run(CommandContext ctx) {
        return ctx.channel()
                .createMessage(ctx.translate(Strings.APP, "ping"))
                .then();
    }
}
```

The `@Alias` annotation allows to specify one or more alias for the command. The `run` method contains the code of the command itself. Here we are just replying "Pong!" in the same channel the command was sent in.

:::info
Instead of using `@Alias`, it is also possible to override the `aliases()` default method from `Command`, which by default reads the value of the annotation. Can be useful if the aliases aren't known at compile time. The `@Alias` annotation will be ignored if `aliases()` is overriden, unless it calls the super implementation.
:::

There are many default methods in the `Command` interface that you can override to customize different aspects of the command, such as:

* `documentation(Translator)`: allows you to attach a documentation to the command. Useful if you want to make a help command or generate a user manual. The `Translator` parameter allows you to adapt the documentation to the language of the user. Details on how to implement this method is documented [here](documenting-commands.md).
* `privilege()`: defines a privilege, in other words a requirement that must be fulfilled in order to be allowed to execute the command. Details on how to implement this method is documented [here](privileges.md).
* `scope()`: defines where the command can be used. The possible values are: `Scope.ANYWHERE` (the command can be used anywhere), `Scope.DM_ONLY` (the command only works in private messages), `Scope.GUILD_ONLY` (the command only works inside a guild). A command ran outside of its scope will be ignored.
* `subcommands()`: defines subcommands for the command. More details about subcommands can be found [here](subcommands.md).
* `errorHandler()`: defines a command-specific error handler, overriding the global error handler if it exists. More details about error handlers can be found [here](handling-errors.md).
* `rateLimit()`: defines a rate limit for the command, that is the maximum number of times a command can be used per user within a specific timeframe. More details about rate limiting can be found [here](rate-limiting.md).

### Inline commands

Another way to create commands is to create them "inline", via the static factory methods of `Command`:

```java
var command = Command.of("ping",
        ctx -> ctx.channel()
                .createMessage("Pong!")
                .then());

var moreComplexCommand = Command.builder("hello",
        ctx -> ctx.channel()
                .createMessage("Hello!")
                .then())
        .scope(Scope.DM_ONLY)
        .rateLimit(RateLimit.of(2, Duration.ofSeconds(5)))
        .build();
```

The `builder` allows to access the same features as the default interface methods of `Command` (privilege, scope, rate limit...).

## Registering commands

In order to use the commands, you need to register them. Here again there are different ways to proceed.

### The `@TopLevelCommand` annotation

This approach is the most straightforward, but is only relevant for top-level commands (so not for subcommands), and only if you make the command by implementing the interface (so not for inline commands).

To use it, simply add the `@TopLevelCommand` annotation on the implementation class:

```java
package com.example.myproject;

import botrino.command.Command;
import botrino.command.CommandContext;
import botrino.command.annotation.Alias;
import botrino.command.annotation.TopLevelCommand;
import reactor.core.publisher.Mono;

import java.util.Set;

@TopLevelCommand
@Alias("ping")
public final class PingCommand implements Command {

    @Override
    public Mono<Void> run(CommandContext ctx) {
        return ctx.channel()
                .createMessage(ctx.translate(Strings.APP, "ping"))
                .then();
    }
}
```

That's it! You can run your bot, the `ping` command above is fully functional!

### Manual registration on the command service

In some cases you might want to register your commands manually, for example if you choose to make them inline. To achieve this, you need to inject the `CommandService` and call the `addTopLevelCommand` method on it:

```java
package com.example.myproject;

import botrino.command.Command;
import botrino.command.CommandService;
import com.github.alex1304.rdi.finder.annotation.RdiFactory;
import com.github.alex1304.rdi.finder.annotation.RdiService;

import java.util.Set;

@RdiService
public final class SampleService {

    @RdiFactory
    public SampleService(CommandService commandService) {
        var command = Command.of(Set.of("ping"), ctx ->
                ctx.channel()
                        .createMessage("Pong!")
                        .then());
        commandService.addTopLevelCommand(command);
    }
}
```

The code above is 100% functional just like the previous one, now you have an example for both approaches. Feel free to use either approach according to your preferences. The interface approach might be preferred for complex or stateful commands, while the inline approach might be preferred for simple and stateless commands.

## Commands as a service

Classes implementing commands can themselves be declared as services without any issues. For example if you need to access the `ConfigContainer` in your command, you can do this:

```java
package com.example.myproject;

import botrino.api.config.ConfigContainer;
import botrino.command.Command;
import botrino.command.CommandContext;
import botrino.command.annotation.Alias;
import botrino.command.annotation.TopLevelCommand;
import botrino.command.config.CommandConfig;
import com.github.alex1304.rdi.finder.annotation.RdiFactory;
import com.github.alex1304.rdi.finder.annotation.RdiService;
import reactor.core.publisher.Mono;

@RdiService
@TopLevelCommand
@Alias("prefix")
public final class PrefixCommand implements Command {

    private final String prefix;

    @RdiFactory
    public PrefixCommand(ConfigContainer configContainer) {
        this.prefix = configContainer.get(CommandConfig.class).prefix();
    }

    @Override
    public Mono<Void> run(CommandContext ctx) {
        return ctx.channel()
                .createMessage("The default prefix is " + prefix)
                .then();
    }
}
```

The command above accesses the configuration to get the default prefix of the bot. You can notice the use of `@RdiService` on top of the two other annotations, this works totally fine! Don't forget the `@RdiFactory` to inject the configuration container, and you're ready to run the bot and try out this command. Ideally, the string inside `createMessage` should be externalized, but it would needlessly complicate the documentation for this very simple example.

:::tip
If you declare a command as a service this way, you are allowed to do anything with it like any other service, for example inject it in other services, or set up `@RdiFactory` to be a [reactive static method](../api/working-with-services.md#injecting-a-service-in-a-reactive-static-factory) in case the command needs to perform a reactive task in order to be initialized.
:::
