---
title: Documenting commands
---

import useBaseUrl from '@docusaurus/useBaseUrl';

For users to know how to use your bot, it is very important to provide some documentation on what your commands do. The majority of bots achieve that via a help command, which usually displays a list of commands and detailed description for each of them. After reading this page you will have all resources needed to implement your own help command.

## Defining a documentation for a command

To attach a documentation to a command, override the `documentation()` method from `Command` (or use the corresponding method in `Command.Builder` if you build the command inline):

```java
@Override
public CommandDocumentation documentation(Translator translator) {
    return CommandDocumentation.builder()
            .setSyntax("[command...]")
            .setDescription("Displays helpful info on commands.")
            .setBody("Without arguments, gives a list of available commands. Pass a command or a sequence " +
                    "of subcommands in arguments to get detailed information on that specific command/subcommand.")
            .build();
}
```

You can specify several fields, which include:
* The **syntax**: indication on the structure of arguments. If you use `CommandGrammar`, it is recommended to do `.setSyntax(grammar.toString())`.
* The **description**: a one-liner description for the command. Will typically be printed next to the command name when listing commands.
* The **body**: a more lengthy description which explains more in-depth how the command works. May take several lines.
* The **flags**: the details about the flags that are accepted.

:::tip
Use the `Translator` parameter of the `documentation()` method to adapt the language and externalize the strings of the documentation content.
:::

## Accessing documentation from other commands

If you want to implement a help command, you need to gather all `Command` instances and call `documentation()` on them. You can achieve this by injecting the `CommandService` and using the `listCommands()` method.

Here is an example of a fully working `!help` command, which accepts an argument to view the documentation for a specific command or subcommand. If the argument is not provided, it will list all commands with only their name and description. It throws `CommandFailedException` if the command does not exist.

```java
package com.example.myproject;

import botrino.api.i18n.Translator;
import botrino.api.util.Markdown;
import botrino.command.Command;
import botrino.command.CommandContext;
import botrino.command.CommandFailedException;
import botrino.command.CommandService;
import botrino.command.annotation.Alias;
import botrino.command.annotation.TopLevelCommand;
import botrino.command.doc.CommandDocumentation;
import botrino.command.grammar.CommandGrammar;
import com.github.alex1304.rdi.finder.annotation.RdiFactory;
import com.github.alex1304.rdi.finder.annotation.RdiService;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static java.util.function.Predicate.not;

@Alias("help")
@TopLevelCommand
@RdiService
public class HelpCommand implements Command {

    private final CommandGrammar<Args> grammar = CommandGrammar.builder()
            .nextArgument("command")
            .setVarargs(true)
            .build(Args.class);

    private final CommandService commandService;

    @RdiFactory
    public HelpCommand(CommandService commandService) {
        this.commandService = commandService;
    }

    private static String formatDocForCommand(Command cmd, CommandContext ctx, Args args) {
        var doc = cmd.documentation(ctx);
        var sb = new StringBuilder();
        sb.append("```\n");
        sb.append(ctx.getPrefixUsed());
        sb.append(String.join(" ", args.command));
        sb.append(' ');
        sb.append(doc.getSyntax());
        sb.append("\n```\n");
        sb.append(doc.getDescription());
        sb.append("\n\n");
        sb.append(doc.getBody());
        sb.append('\n');
        if (!doc.getFlags().isEmpty()) {
            sb.append("__Flags:__\n");
            for (var flagInfo : doc.getFlags()) {
                sb.append('`');
                sb.append(flagInfo.getValueFormat());
                sb.append("`: ");
                sb.append(flagInfo.getDescription());
                sb.append('\n');
            }
        }
        return sb.toString();
    }

    @Override
    public Mono<Void> run(CommandContext ctx) {
        return grammar.resolve(ctx).flatMap(args -> {
            if (args.command.isEmpty()) {
                // List all top-level commands
                return ctx.channel().createMessage(commandService.listCommands().stream()
                        .map(cmd -> {
                            var aliases = String.join("|", cmd.aliases());
                            var desc = Optional.of(cmd.documentation(ctx).getDescription())
                                    .filter(not(String::isEmpty))
                                    .orElseGet(() -> Markdown.italic("No description"));
                            return Markdown.code(ctx.getPrefixUsed() + aliases) + ": " + desc;
                        })
                        .collect(Collectors.joining("\n")));
            }
            // Send documentation for specific command
            var cmdFound = commandService.getCommandAt(args.command.get(0),
                    args.command.subList(1, args.command.size()).toArray(new String[0]));
            return cmdFound.map(cmd -> ctx.channel().createMessage(formatDocForCommand(cmd, ctx, args)))
                    .orElseGet(() -> Mono.error(new CommandFailedException("Command not found")));
        }).then();
    }

    @Override
    public CommandDocumentation documentation(Translator translator) {
        return CommandDocumentation.builder()
                .setSyntax(grammar.toString())
                .setDescription("Displays helpful info on commands.")
                .setBody("Without arguments, gives a list of available commands. Pass a command or a sequence " +
                        "of subcommands in arguments to get detailed information on that specific command/subcommand.")
                .build();
    }

    private static final class Args {
        private List<String> command;
    }
}
```

The command above in action:

<img src={useBaseUrl('img/doc_example.png')} alt="" />
