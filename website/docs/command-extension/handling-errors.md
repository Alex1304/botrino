---
title: Handling errors
---

The execution of a command may fail for many reasons. You can handle these errors in a way that is adapted to each type of errors, this is done via the `CommandErrorHandler` interface.

## Global error handler

If you create a class implementing `CommandErrorHandler` in your bot module, it will automatically be registered as a **global error handler**. It means that this handler will be applied to all commands.

:::tip
The example below uses hardcoded strings, but you are encouraged to externalize them according to [this guide](../api/i18n.md).
:::

```java
package com.example.myproject;

import botrino.api.util.DurationUtils;
import botrino.command.CommandContext;
import botrino.command.CommandErrorHandler;
import botrino.command.CommandFailedException;
import botrino.command.InvalidSyntaxException;
import botrino.command.cooldown.CooldownException;
import botrino.command.privilege.PrivilegeException;
import reactor.core.publisher.Mono;

public final class GlobalErrorHandler implements CommandErrorHandler {

    @Override
    public Mono<Void> handleCommandFailed(CommandFailedException e,
                                          CommandContext ctx) {
        return ctx.channel().createMessage("\uD83D\uDEAB " + e.getMessage()).then();
    }

    @Override
    public Mono<Void> handleInvalidSyntax(InvalidSyntaxException e,
                                          CommandContext ctx) {
        var badArgName = e.getBadArgumentName().orElse(null);
        var badArgValue = e.getBadArgumentValue().orElse(null);
        String message;
        if (badArgName == null && badArgValue == null) {
            message = "Expected a subcommand.";
        } else if (badArgName == null) {
            message = "Subcommand \"" + badArgValue + "\" not found.";
        } else if (badArgValue == null) {
            message = "Missing argument `<" + badArgName + ">`";
        } else {
            message = "Value \"" + badArgValue + "\" is invalid for " +
                    "argument `<" + badArgName + ">`";
        }
        return ctx.channel().createMessage(message).then();
    }

    @Override
    public Mono<Void> handlePrivilege(PrivilegeException e, CommandContext ctx) {
        return ctx.channel().createMessage("You have insufficient privileges " +
                "to run this command.").then();
    }

    @Override
    public Mono<Void> handleCooldown(CooldownException e, CommandContext ctx) {
        return ctx.channel().createMessage("You are on cooldown. " +
                "Retry in " + DurationUtils.format(e.getRetryAfter())).then();
    }

    @Override
    public Mono<Void> handleDefault(Throwable t, CommandContext ctx) {
        return ctx.channel()
                .createMessage("Something went wrong! Sorry for the inconvenience.")
                .onErrorResume(e -> {
                    t.addSuppressed(e);
                    return Mono.empty();
                }).then(Mono.error(t)); // Forward downstream for logging
    }
}
```

The methods of `CommandErrorHandler` correspond to the most common error types. Each method exposes the `CommandContext` in which the error happened. None of them are required to be implemented, by default they just forward errors downstream which will only log them at ERROR level. Currently there are five of them:

* `handleCommandFailed(CommandFailedException, CommandContext)`: allows to recover on a `CommandFailedException`. This exception represents a "normal" failure of the command, for example if the command allows the user to search for a resource but the resource requested by the user was not found. This exception generally carries a user-friendly message, so the way to handle it will mostly consist of replying to the user with that message.
* `handleInvalidSyntax(InvalidSyntaxException, CommandContext)`: allows to recover on an `InvalidSyntaxException`. This can be used to send a help message to the user on the syntax of the command.
* `handlePrivilege(PrivilegeException, CommandContext)`: allows to recover on a `PrivilegeException`. It is thrown when a user attempts to use a command with insufficient privileges. Typically, handling this exception will consist of telling the user they cannot use the command, or simply failing silently. More details on privileges can be found in [this section](privileges.md).
* `handleCooldown(CooldownException, CommandContext)`: allows to recover on a `CooldownException`. It is thrown when a user attempts to use a command past the maximum usage limit within a time interval. Generally, it will be handled by notifying the user that they need to wait some time before trying the command again (the exception carries the exact time left). More details on cooldowns can be found in [this section](cooldowns.md).
* `handleDefault(Throwable, CommandContext)`: allows to recover on an exception type that corresponds to none of the above.

:::caution
* The implementation class must have a no-arg constructor.
* If more than one implementation of `CommandErrorHandler` are found, it will result in an error as it is impossible to determine which one to use. If you don't want to remove the extra implementation(s), you can mark one of them with the `@Primary` annotation to lift the ambiguity. You may alternatively use the `@Exclude` annotation if you don't want one implementation to be picked up by Botrino.
:::

## Command-specific error handler

It is possible to override the global error handler for a specific command. Simply override the `errorHandler()` method from `Command` (or call `setErrorHandler` on `Command.Builder`) and implement the error handler directly as an anonymous class:

```java
@Override
public CommandErrorHandler errorHandler() {
    return new CommandErrorHandler() {
        // Override methods here
    };
}
```

:::warning
If you aren't using an anonymous class, make sure to add the `@Exclude` annotation on it otherwise it will be recognized as a global error handler. Alternatively, you may use `@Primary` on your global error handler so you don't need to worry about it.
:::
