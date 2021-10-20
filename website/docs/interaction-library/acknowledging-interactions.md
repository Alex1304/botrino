---
title: Acknowledging interactions
---

When an interaction event is received from the gateway, the library is able to acknowledge them automatically. This allows to simplify your code a lot, as you can directly use `createFollowup()` or `editReply()` without worrying about choosing between `reply()`,  `edit()`, `deferReply()` or `deferEdit()` first. If you have a specific use case that requires you to take full control over the acknowledgment process, the library gets you covered by offering a way to disable automatic acknowledgment on a per-command basis.

## Modifying the default acknowledgment behavior

This was partially covered in the [Configuration](configuration.md) page, the default behavior can be set via the `default_ack_mode` field of `config.json` if you are using the Botrino framework, or via `InteractionConfig.Builder#defaultACKMode(String)` when building the configuration manually. Here's a table describing the possible values and their behavior:

| value | behavior |
|---|---|
| `default` | Equivalent to `defer`. |
| `defer` | Automatically calls `deferReply()` (for commands) or `deferEdit()` (for components). |
| `defer_ephemeral` | Automatically calls `deferReply().withEphemeral(true)` (for commands) or `deferEdit().withEphemeral(true)` (for components). |
| `none` | Does not call any acknowledgment method. |

## Overriding the acknowledgment mode on a per-command basis

Let's say you have `defer` as default behavior in your config, and you want to make a command that replies exclusively with ephemeral messages. There would be no way to achieve this without overriding the acknowledgment behavior for this specific command so that it can be ephemeral. This is as simple as adding an `@Acknowledge` annotation with the desired mode as value:

```java {9}
package testbot1;

import botrino.interaction.annotation.Acknowledge;
import botrino.interaction.annotation.ChatInputCommand;
import botrino.interaction.listener.ChatInputInteractionListener;
import botrino.interaction.context.ChatInputInteractionContext;
import org.reactivestreams.Publisher;

@Acknowledge(Acknowledge.Mode.DEFER_EPHEMERAL)
@ChatInputCommand(name = "ping", description = "Pings the bot to check if it is alive.")
public final class PingCommand implements ChatInputInteractionListener {

    @Override
    public Publisher<?> run(ChatInputInteractionContext ctx) {
        return ctx.event().createFollowup("Pong!").withEphemeral(true);
    }
}
```

Since this is a very simple command, you could even completely disable automatic acknowledgment and use `reply()` instead of `createFollowup()`:

```java
package testbot1;

import botrino.interaction.annotation.Acknowledge;
import botrino.interaction.annotation.ChatInputCommand;
import botrino.interaction.listener.ChatInputInteractionListener;
import botrino.interaction.context.ChatInputInteractionContext;
import org.reactivestreams.Publisher;

@Acknowledge(Acknowledge.Mode.NONE)
@ChatInputCommand(name = "ping", description = "Pings the bot to check if it is alive.")
public final class PingCommand implements ChatInputInteractionListener {

    @Override
    public Publisher<?> run(ChatInputInteractionContext ctx) {
        return ctx.event().reply("Pong!").withEphemeral(true);
    }
}
```

:::warning
If your command is made of subcommands or subcommand groups, the `@Acknowledge` annotation must be used on the listener implementation class of individual subcommands; putting it on the parent class alongside `@ChatInputCommand` will have no effect.
:::