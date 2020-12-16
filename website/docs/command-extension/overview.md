---
title: Overview
---

Botrino comes with an extension that allows to easily create commands based on message create events. Although Discord recently added [Slash Commands](https://discord.com/developers/docs/interactions/slash-commands) as a native way to implement commands within Discord, message create-based commands will certainly remain the most flexible solution.

## Preamble

By definition, as this is an extension, it does not belong to the core framework API and you are not required to use it. It aims at giving you enough flexibility to cover the majority of use cases, but for very specific ones you might need to implement your own solution. That's why feedback on this extension is more than welcome, if you feel something is missing feel free to open an issue on the [GitHub repository](https://github.com/Alex1304/botrino).

## Features

* Register unlimited commands
* Message tokenization into arguments and flags
* Apply a grammar to command arguments to conveniently convert them into actual Java types, supporting required, optional, and varying arguments
* Unlimited subcommands
* Attach a documentation to all your commands and subcommands
* Define privileges for each command with your own rules
* Global and per-command error handling
* Per-guild prefix and language
* Rate-limiting / cooldowns
* Blacklist individual users, guilds or channels from using commands to handle cases of abuse
* Create interactive menus

## Code examples

:::info
The examples below make use of translated strings, they are assumed to exist as described in [this page](../api/i18n.md).
:::

A basic `!ping` command:
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
        return ctx.channel().createMessage(ctx.translate(Strings.APP, "ping")).then();
    }
}
```

A `!sendword <word> <count> [channels...]` command that sends a word `count` times in zero, one or more channels. Requires `ADMINISTRATOR` permission and may be used at most once every 1 minute:

```java
package com.example.myproject;

import botrino.command.Command;
import botrino.command.CommandContext;
import botrino.command.grammar.ArgumentMapper;
import botrino.command.grammar.CommandGrammar;
import botrino.command.privilege.Privilege;
import botrino.command.privilege.Privileges;
import botrino.command.ratelimit.RateLimit;
import discord4j.core.object.entity.channel.GuildChannel;
import discord4j.core.object.entity.channel.GuildMessageChannel;
import discord4j.rest.util.Permission;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.List;
import java.util.Set;

public final class SendWordCommand implements Command {

    private final CommandGrammar<Args> grammar = CommandGrammar.builder()
            .nextArgument("word")
            .nextArgument("count", ArgumentMapper.asInteger())
            .nextArgument("channels", ArgumentMapper.asGuildChannel())
            .setVarargs(true)
            .build(Args.class);

    @Override
    public Set<String> aliases() {
        return Set.of("sendword");
    }

    @Override
    public Mono<Void> run(CommandContext ctx) {
        return grammar.resolve(ctx)
                .flatMap(args -> Flux.fromIterable(args.channels)
                        .ofType(GuildMessageChannel.class)
                        .flatMap(channel -> Flux.range(0, args.count)
                                .flatMap(__ -> channel.createMessage(args.word))
                                .then())
                        .then());
    }

    @Override
    public RateLimit rateLimit() {
        return RateLimit.of(1, Duration.ofMinutes(1));
    }

    @Override
    public Privilege privilege() {
        return Privileges.checkPermissions(
                tr -> tr.translate(Strings.APP, "error_perm_denied"),
                perms -> perms.contains(Permission.ADMINISTRATOR));
    }

    private static final class Args {
        private String word;
        private int count;
        private List<GuildChannel> channels;
    }
}
```

## Getting started

Using the Maven archetype as outlined in Botrino's [Getting Started guide](../getting-started.md) will automatically configure the command extension for you. The section ["From a blank project"](../getting-started.md#from-a-blank-project) explains how to go for a more manual approach, and the guide already shows how to configure the command extension. Simply make sure to include the `botrino-command` artifact in your project dependencies and to add `requires botrino.command;` in your `module-info.java`, and you're ready to go.
