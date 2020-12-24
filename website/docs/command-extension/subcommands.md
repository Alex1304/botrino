---
title: Subcommands
---

A common use case when working with commands is to be able to split the logic of the command into multiple pieces. Typically, the first argument is used to route the execution flow of a command to a specific piece of code. It might also be needed to give this piece of code its own settings that might differ from the parent command, such as a custom scope or permission. Subcommands are made to enable that.

## Inline subcommands

To create a subcommand, simply override the `Set<Command> subcommands()` method of the `Command` interface. You can then add your commands inline:

```java
@Override
public Set<Command> subcommands() {
    return Set.of(
        Command.builder(Set.of("ping"),
                ctx -> ctx.channel()
                        .createMessage("Pong!")
                        .then())
                .inheritFrom(this)
                .build()
    );
}
```

:::info
`inheritFrom(this)` allows to inherit some settings from the parent command, such as privilege, scope, error handler and rate limit. It won't inherit aliases, action, documentation and subcommands. You are not required to call it if you don't want those properties to be inherited.
:::

:::tip
If you have a very complex command with many subcommands, it might be better to store your subcommand instances in private fields instead of nesting them, and make your `subcommands()` method return `Set.of(field1, field2, ...)`.
:::

## Command classes as subcommands

If the code of your subcommand is quite complex, you may prefer to declare your subcommand by creating a class implementing `Command` instead:

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
                .createMessage("Pong!")
                .then();
    }
}
```

:::warning
Do not use the `@TopLevelCommand` annotation, as we want this command to be a subcommand and not a top-level command.
:::
