---
title: Cooldowns
---

If your commands perform costly operations or can potentially be abused, you may want to set a limit on the number of times the command can be used. The Cooldown API will allow you to deal with these situations without hassle.

## Set a cooldown to a command

Override the `cooldown()` method from `Command` (or use the corresponding method in `Command.Builder` if you build the command inline), and specify the cooldown using `Cooldown.of(int, Duration)`:

```java
@Override
public Cooldown cooldown() {
    return Cooldown.of(1, Duration.ofMinutes(1));
}
```

The integer represents the maximum number of times the command can be executed within a timeframe, and the duration represents the timeframe itself. Therefore, this example corresponds to a cooldown of **once per minute**.

:::info
Cooldowns are applied **per user**. Two different users using the same command will always have their own separate usage limits.
:::

## Sharing cooldown between commands

In some cases, you may want several different commands to share the same cooldown. In order to achieve this, all you need to do is to reuse the same `Cooldown` instance between the commands:

```java
var cooldown = Cooldown.of(3, Duration.ofMinutes(2));
commandService.addTopLevelCommand(Command.builder("test1",
            ctx -> ctx.channel().createMessage("test1").then())
        .setCooldown(cooldown)
        .build());
commandService.addTopLevelCommand(Command.builder("test2",
            ctx -> ctx.channel().createMessage("test2").then())
        .setCooldown(cooldown)
        .build());
commandService.addTopLevelCommand(Command.builder("test3",
            ctx -> ctx.channel().createMessage("test3").then())
        .setCooldown(cooldown)
        .build());
```

If you are implementing the command via a class, you can store the `Cooldown` instance in a static field, or wrap it in a service that you inject in each target command class.

:::warning
In order to properly share a cooldown from a parent command to a subcommand, it is important to store the instance in a field of the parent command, even if `inheritFrom(this)` is used. For instance, the following code will NOT share the instance to the subcommand:

```java
@Override
public Cooldown cooldown() {
    return Cooldown.of(1, Duration.ofMinutes(1));
}

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

Here is how to fix it:
```java
private final Cooldown cooldown = Cooldown.of(1, Duration.ofMinutes(1));

@Override
public Cooldown cooldown() {
    return cooldown;
}

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
Notice that the code of `subcommands()` didn't change, but now subsequent invocations of `cooldown()` (which is what `inheritFrom(this)` does behind the scenes) will properly return the same instance.
:::

## Handling cooldowns

When a user reaches the maximum number of permits and attempts to use the command again, the command will fail with a `CooldownException`. You can handle this exception via the command error handler, which will be covered in the [next section](handling-errors.md).
