---
title: Input tokenization
---

As for the majority of command-based user interfaces, a command may accept some user input, often called "arguments", to specify parameters for a specific action. The simplest commands can be run as-is, while more complex ones may require several arguments with some possibly being optional. Parsing the input, checking if required arguments are present, are of the correct format, etc can represent a lot of work when implementing such complex commands, and ends up being repetitive. The command extension of Botrino can do most of that boring work for you.

## Tokenization

When a message create event is received from Discord, the content of the message is interpreted in order to find the corresponding command. It will split the message content into "words", stripping out the prefix, will match the first "word" with a command name and will process the remaining "words" into **arguments** and **flags**. This process is called **input tokenization**.

### Arguments

An argument is a substring of the message content that is delimited with whitespaces and that comes after the command name. A simple example:

```
!hello world
```
Here the command name is `hello`, and `world` is an argument.

You can access the list of arguments via `CommandContext.input().getArguments()`:
```java
@Override
public Mono<Void> run(CommandContext ctx) {
    List<String> args = ctx.input().getArguments();
    return ctx.channel().createMessage(args.toString()).then();
}
```
If this was the implementation of the previous example, it would send `[world]` in the Discord channel (the `toString()` representation of the `List` of arguments). Another example:
```
!hello foo bar
```
would reply with `[foo, bar]`, etc.

It is possible to put spaces into a single argument if it is delimited with double quotes (`"`). As such, the example below:
```
!hello "foo bar"
```
would return a list of **one** element which is the string `foo bar`.

:::caution
This implies that the double quote character is reserved and should be escaped with `\` if you want to use it in an argument value, same for the `\` character itself. See the table below that illustrates that.
:::

input | output
------|--------
`!hello "foo bar"` | `[foo bar]`
`!hello \"foo bar\"` | `["foo, bar"]`
`!hello fo\\o ba\\r` | `[fo\o, ba\r]`

### Flags

Flags are similar to arguments, except they are **named** and are **position-independent**. A flag is distinguished from an argument by a dash (`-`) prefix.

```
!hello -a world
```

In the example above, there is **only one** argument, `world`. Indeed, `-a` isn't an argument but a **flag**, due to the presence of the `-` prefix. In the code of the command, you can access a flag like this:

```java
@Override
public Mono<Void> run(CommandContext ctx) {
    String message;
    if (ctx.input().getFlag("a").isPresent()) {
        message = "The flag is present";
    } else {
        message = "The flag isn't present";
    }
    return ctx.channel().createMessage(message).then();
}
```

You must not write the `-` prefix in the argument of `getFlag()`. It returns an `Optional<String>` that is empty if the flag is not present in the message. The position-independent nature of flags implies that `!hello -a world` and `!hello world -a` are strictly equivalent. Even something like `!-a hello world` will work. If the flag is present, it will return an empty `String`, unless a specific value is passed like this:

```
!hello -a=foo world
```

In this case, `getFlag("a")` would return an `Optional` containing the string `foo`. Here is a table to summarize:

input | result of `ctx.input().getFlag("a")`
------|-------------------------
`!hello` | `Optional.empty()`
`!hello -a` | `Optional.of("")`
`!hello -a=foo` | `Optional.of("foo")`

## Grammar

The fact that tokenization is done for you is a great thing, but that's only a part of the work. When you access the value of an argument, you still need to interpret it, and possibly do some type conversion and validation work on it.

Let's take a simple example. You want to create a command that allows to rename a channel in a guild. The most intuitive way to design the command is to do something like `!rename <channel> <new_name>`. The second argument is just a string so it won't be too much of an issue, however the first argument must be interpreted as an existing Discord channel in the current guild first.

With what you know already, you can implement that command as follows:

```java
package com.example.myproject;

import botrino.api.util.DiscordParser;
import botrino.api.util.MatcherFunction;
import botrino.command.Command;
import botrino.command.CommandContext;
import botrino.command.CommandFailedException;
import botrino.command.Scope;
import botrino.command.annotation.Alias;
import botrino.command.annotation.TopLevelCommand;
import discord4j.core.object.entity.channel.*;
import reactor.core.publisher.Mono;

@Alias("rename")
@TopLevelCommand
public final class RenameCommand implements Command {

    @Override
    public Mono<Void> run(CommandContext ctx) {
        var args = ctx.input().getArguments();
        if (args.size() < 2) {
            return Mono.error(new CommandFailedException("Expected 2 arguments"));
        }
        var gateway = ctx.event().getClient();
        var guildId = ctx.event().getGuildId().orElseThrow(); // Never throws because we set Scope.GUILD_ONLY
        var channelMono = DiscordParser.parseGuildChannel(gateway, guildId, args.get(0));
        var newName = args.get(1);
        return channelMono
                .flatMap(channel -> MatcherFunction.<Mono<? extends GuildChannel>>create()
                        .matchType(TextChannel.class, ch -> ch.edit(spec -> spec.setName(newName)))
                        .matchType(NewsChannel.class, ch -> ch.edit(spec -> spec.setName(newName)))
                        .matchType(StoreChannel.class, ch -> ch.edit(spec -> spec.setName(newName)))
                        .matchType(VoiceChannel.class, ch -> ch.edit(spec -> spec.setName(newName)))
                        .apply(channel)
                        .orElseGet(() -> Mono.error(new CommandFailedException("Unknown channel type"))))
                .then(ctx.channel().createMessage("Successfully renamed channel"))
                .then();
    }

    @Override
    public Scope scope() {
        return Scope.GUILD_ONLY;
    }
}
```

Since this example is simple enough, the `run` method is only 18 lines long, so it's not that bad. But you can notice that 8 of those lines only consist of parsing the arguments, the core logic of the method resides in the last 10 lines. You can imagine for more complex commands with many arguments that the first part can be heavy and repetitive. This is the problematic **command grammars** bring a solution to.

With a grammar, you can completely get rid of the code that handles arguments, so that your `run` method is effectively focused on its core logic:

```java
package com.example.myproject;

import botrino.api.util.MatcherFunction;
import botrino.command.Command;
import botrino.command.CommandContext;
import botrino.command.CommandFailedException;
import botrino.command.Scope;
import botrino.command.annotation.Alias;
import botrino.command.annotation.TopLevelCommand;
import botrino.command.grammar.ArgumentMapper;
import botrino.command.grammar.CommandGrammar;
import discord4j.core.object.entity.channel.*;
import reactor.core.publisher.Mono;

@Alias("rename")
@TopLevelCommand
public final class RenameCommand implements Command {

    private final CommandGrammar<Args> grammar = CommandGrammar.builder() // Step 2
            .nextArgument("channel", ArgumentMapper.asGuildChannel())
            .nextArgument("newName")
            .build(Args.class);

    @Override
    public Mono<Void> run(CommandContext ctx) {
        return grammar.resolve(ctx) // Step 3
                .flatMap(args -> MatcherFunction.<Mono<? extends GuildChannel>>create()
                        .matchType(TextChannel.class, ch -> ch.edit(spec -> spec.setName(args.newName)))
                        .matchType(NewsChannel.class, ch -> ch.edit(spec -> spec.setName(args.newName)))
                        .matchType(StoreChannel.class, ch -> ch.edit(spec -> spec.setName(args.newName)))
                        .matchType(VoiceChannel.class, ch -> ch.edit(spec -> spec.setName(args.newName)))
                        .apply(args.channel)
                        .orElseGet(() -> Mono.error(new CommandFailedException("Unknown channel type"))))
                .then(ctx.channel().createMessage("Successfully renamed channel"))
                .then();
    }

    @Override
    public Scope scope() {
        return Scope.GUILD_ONLY;
    }

    private static final class Args { // Step 1
        private GuildChannel channel;
        private String newName;
    }
}
```

Creating a grammar is done in 3 steps:

1. Declare an **internal class** with fields corresponding to arguments with their correct type.
2. Build a `CommandGrammar` instance describing the sequence of arguments accepted by your command. The `nextArgument` method allows you to specify the name of the field of the internal class to bind, with an `ArgumentMapper` if the target type is something else than `String`. Think of `ArgumentMapper` like a `BiFunction<CommandContext, String, T>` where `T` is the target type. The most common types are present as a static factory in `ArgumentMapper`, but you can implement your own with a lambda expression. The order in which `nextArgument` calls are made is important as it will determine if the command should be `!rename <channel> <newName>` or `!rename <newName> <channel>`.
3. In your `run` method, all you need to do is to call `grammar.resolve(CommandContext)` to get an instance of your internal class with all arguments filled. In this case it will automatically emit `InvalidSyntaxException` if the arguments are missing, because all arguments are required by default.

The `CommandGrammar.Builder` class has additional features allowing you to configure **optional** and **varying** arguments:

```java title="Example 1: foo is required, bar is optional"
CommandGrammar.builder()
        .nextArgument("foo")
        .beginOptionalArguments()
        .nextArgument("bar")
        .build(Args.class);

private static final class Args {
    private String foo; // Cannot be null
    private String bar; // Can be null
}
```
```java title="Example 2: both foo and bar are optional"
CommandGrammar.builder()
        .beginOptionalArguments()
        .nextArgument("foo")
        .nextArgument("bar", ArgumentMapper.asInteger())
        .build(Args.class);

private static final class Args {
    private String foo; // Can be null
    private Integer bar; // Can be null, so must be of type Integer (and not int)
}
```
```java title="Example 3: bar can be repeated zero, one or multiple times"
CommandGrammar.builder()
        .nextArgument("foo")
        .nextArgument("bar", ArgumentMapper.asInteger())
        .setVarargs(true)
        .build(Args.class);

private static final class Args {
    private String foo; // Cannot be null
    private List<Integer> bar; // Must be a List of the target type, can be empty
}
```

A few things to note:

* If an optional argument is not provided, the corresponding field in the internal class will be filled with `null`. It means that for primitive types you need to use the wrapper type for the field type (`java.lang.Integer` for `int`, etc) otherwise you will experience `NullPointerException`.
* Optional arguments can only be at the end, hence `beginOptionalArguments()` which separates required from optional arguments.
* `setVarargs(true)` is the only method that is insensitive to its position in the chain: it only applies to the very last argument.
* A varying argument is materialized with a field of type `java.util.List` in the internal class, and is always optional even if `beginOptionalArguments()` was never called (in which case the list will be empty).

:::info
The Command Grammar API does not support flags at this moment, only regular arguments. Feel free to suggest it or open a PR in the [GitHub repository](https://github.com/Alex1304/botrino) if you have improvement ideas.
:::
