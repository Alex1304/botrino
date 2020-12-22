---
title: Creating commands
---

Commands represent the main form of interaction that Discord bots have with users. Given you properly configured the command extension, creating commands is a simple and straightforward process.

## Implementing the `Command` interface

All your classes present in your module that implement `Command` will automatically be registered. It requires to implement two methods, `Set<String> aliases()` and `Mono<Void> run(CommandContext)`:

```java
package com.example.myproject;

import botrino.command.Command;
import botrino.command.CommandContext;
import reactor.core.publisher.Mono;

import java.util.Set;

public final class PingCommand implements Command {

    @Override
    public Set<String> aliases() {
        return Set.of("ping");
    }

    @Override
    public Mono<Void> run(CommandContext ctx) {
        return ctx.channel()
                .createMessage("Pong!")
                .then();
    }
}
```

`aliases()` indicates the name(s) of the command to write in chat right after the prefix, there may be more than one. If the set is empty, the command won't be registered.

`run(CommandContext)` contains the code of the command itself. Here we are just replying "Pong!" in the same channel the command was sent in.

There are many default methods in the `Command` interface that you can override to customize different aspects of the command, such as:

* `documentation(Translator)`: allows you to attach a documentation to the command. Useful if you want to make a help command or generate a user manual. The `Translator` parameter allows you to adapt the documentation to the language of the user. Details on how to implement this method is documented [here](documenting-commands.md).
* `privilege()`: defines a privilege, in other words a requirement that must be fulfilled in order to be allowed to execute the command. Details on how to implement this method is documented [here](privileges.md).
* `scope()`: defines where the command can be used. The possible values are: `Scope.ANYWHERE` (the command can be used anywhere), `Scope.DM_ONLY` (the command only works in private messages), `Scope.GUILD_ONLY` (the command only works inside a guild). A command ran outside of its scope will be ignored.
* `subcommands()`: defines subcommands for the command. More details about subcommands can be found [here](subcommands.md).
* `errorHandler()`: defines a command-specific error handler, overriding the global error handler if it exists. More details about error handlers can be found [here](handling-errors.md).
* `rateLimit()`: defines a rate limit for the command, that is the maximum number of times a command can be used per user within a specific timeframe. More details about rate limiting can be found [here](rate-limiting.md).

## Creating inline commands

Implementing the interface isn't the only way to create commands. Another way is to create them inline, via the static factory methods of `Command`:

```java
var command = Command.of(Set.of("ping"),
        ctx -> ctx.channel()
                .createMessage("Pong!")
                .then());

var moreComplexCommand = Command.builder(Set.of("hello"),
        ctx -> ctx.channel()
                .createMessage("Hello!")
                .then())
        .scope(Scope.DM_ONLY)
        .rateLimit(RateLimit.of(2, Duration.ofSeconds(5)))
        .build();
```

In order to register them, you need to import the `CommandService` and call the `addCommand` method on it:

```java
package com.example.myproject;

import botrino.command.CommandService;
import com.github.alex1304.rdi.finder.annotation.RdiFactory;
import com.github.alex1304.rdi.finder.annotation.RdiService;
import reactor.util.Logger;
import reactor.util.Loggers;

@RdiService
public final class SampleService {

    @RdiFactory
    public SampleService(CommandService commandService) {
        var command = Command.of(Set.of("ping"), ctx ->
                ctx.channel()
                        .createMessage("Pong!")
                        .then());
        commandService.addCommand(command);
    }
}
```

Generally, the interface approach will be preferred for top-level commands, and the inline approach will be used more for subcommands.
