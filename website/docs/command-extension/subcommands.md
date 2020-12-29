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
        Command.builder("sub",
                ctx -> ctx.channel()
                        .createMessage("This is a subcommand!")
                        .then())
                .inheritFrom(this)
                .build()
    );
}
```

If let's say the prefix is `!` and the top level command is named `top`, sending `!top sub` in chat will make the bot reply with "This is a subcommand!".

:::info
The `Mono<Void> run(CommandContext)` method of the top level command will **not** be run if the subcommand is triggered.
:::

:::tip
`inheritFrom(this)` allows to inherit some settings from the parent command, such as privilege, scope, error handler and cooldown. It won't inherit aliases, action, documentation and subcommands. You are not required to call it if you don't want those properties to be inherited.
:::

## Command classes as subcommands

If the code of your subcommand is quite complex, you may prefer to declare your subcommand by creating a class implementing `Command` instead. The advantage of this approach is that you can easily reuse the subcommand class for more than one top-level command, and write code with the same flexibility as regular commands.

```java
package com.example.myproject;

import botrino.command.Command;
import botrino.command.CommandContext;
import botrino.command.annotation.Alias;
import reactor.core.publisher.Mono;

import java.util.Set;

@Alias("sub")
public final class MySubcommand implements Command {

    @Override
    public Mono<Void> run(CommandContext ctx) {
        return ctx.channel()
                .createMessage("This is a subcommand!")
                .then();
    }
}
```

:::warning
Do not use the `@TopLevelCommand` annotation, as we want this command to be a subcommand and not a top-level command.
:::

To register this subcommand, you have two options:

1. Either instantiate the class yourself:
    ```java title="MyTopLevelCommand.java"
    @Override
    public Set<Command> subcommands() {
        return Set.of(new MySubcommand());
    }
    ```
2. Or declare the class as a service and inject it in your top-level command (recommended):
    ```java title="MySubcommand.java" {1}
    @RdiService
    @Alias("sub")
    public final class MySubcommand implements Command {
        // ...
    ```
    ```java title="MyTopLevelCommand.java" {1,7}
    @RdiService
    @Alias("top")
    public final class MyTopLevelCommand implements Command {

        private final MySubcommand sub;

        @RdiFactory
        public MyTopLevelCommand(MySubcommand sub) {
            this.sub = sub;
        }

        @Override
        public Set<Command> subcommands() {
            return Set.of(sub);
        }

        // ...
    ```

## Subcommand-only commands

If you have a top-level command which can only be used via its subcommands, you can make the top-level command implement `ParentCommand` instead of `Command`. That interface extends `Command` so it's the same in terms of features, except that the `run()` method has a default implementation which simply throws an invalid syntax error, and the `subcommands()` method is made abstract.

```java
@Alias("top")
public final class MyTopLevelCommand implements ParentCommand {

    // No need to override run()

    @Override
    public Set<Command> subcommands() {
        return Set.of(
            Command.builder("sub",
                    ctx -> ctx.channel()
                            .createMessage("This is a subcommand!")
                            .then())
                    .inheritFrom(this)
                    .build()
        );
    }
}
```
