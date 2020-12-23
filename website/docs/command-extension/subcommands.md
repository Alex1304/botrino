---
title: Subcommands
---

A common use case when working with commands is to be able to split the logic of the command into multiple pieces. Typically, the first argument is used to route the execution flow of a command to a specific piece of code. It might also be needed to give this piece of code its own settings that might differ from the parent command, such as a custom scope or permission. Subcommands are made to enable that.

To create a subcommand, simply override the `Set<Command> subcommands()` method of the `Command` interface, and add your commands inline:

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
If you have a very complex command with many subcommands, it might be better to store your subcommand instances in private fields instead of nesting them, and make your `subcommands()` method return `Set.of(field1, field2, ...)`. You can also create classes implementing `Command`, but make sure NOT to annotate them with `@TopLevelCommand`!
:::
