---
title: Interactive menus
---

import useBaseUrl from '@docusaurus/useBaseUrl';

In some situations, running commands isn't the most intuitive solution to interact with a bot. Common examples are: choosing an item in a list, navigating in a paginated content, or triggering an extra action after successfully running a command (e.g "do it again" or "delete the original message"). In these situations you may prefer a solution where users can interact in a more straightforward way, either by replying with a one-word message or by using reactions. This can be done in an intuitive way with **interactive menus**.

## Get a factory

In order to create interactive menus, you first need to get an `InteractiveMenuFactory`. A factory is able to create interactive menus with predefined parameters, such as the default timeout or the emojis to use for reactions if you want to make a pagination system. These parameters are configurable via the configuration file of the bot as explained [here](configuration.md).

In order to get a factory that is parameterized with the contents of the JSON configuration, inject the `CommandService` in your command or your own service and call `interactiveMenuFactory()`:

```java
@RdiService
public final class SomeService {

    @RdiFactory
    public SomeService(CommandService commandService) {
        InteractiveMenuFactory menuFactory = commandService.interactiveMenuFactory();
    }
}
```

:::info
You can always use `InteractiveMenuFactory.of(...)` if you don't want to use the values from the configuration file. In this case you don't need to import the command service.
:::

## Create a simple menu

Let's start with an example command that utilizes interactive menus:

```java
package com.example.myproject;

import botrino.command.*;
import botrino.command.annotation.Alias;
import botrino.command.annotation.TopLevelCommand;
import botrino.command.menu.UnexpectedReplyException;
import com.github.alex1304.rdi.finder.annotation.RdiFactory;
import com.github.alex1304.rdi.finder.annotation.RdiService;
import reactor.core.publisher.Mono;

import java.util.Set;

@Alias({"question", "quiz"})
@TopLevelCommand
@RdiService
public final class QuestionCommand implements ParentCommand {

    private final CommandService commandService;

    @RdiFactory
    public QuestionCommand(CommandService commandService) {
        this.commandService = commandService;
    }

    @Override
    public Set<Command> subcommands() {
        return Set.of(
                Command.builder(Set.of("easy"), ctx -> commandService.interactiveMenuFactory()
                        .create("2 + 2 = ?")
                        .addMessageItem("4", interaction -> interaction.getOriginalCommandContext()
                                .channel().createMessage("Correct!").then())
                        .addMessageItem("", interaction -> interaction.getOriginalCommandContext()
                                .channel().createMessage("Nope! Try again")
                                .then(Mono.error(new UnexpectedReplyException()))) // Will trigger a retry
                        .open(ctx)
                        .then())
                        .inheritFrom(this)
                        .build(),
                Command.builder(Set.of("hard"), ctx -> commandService.interactiveMenuFactory()
                        .create("12455 * 2442 - 85 = ?")
                        .addMessageItem("30415025", interaction -> interaction.getOriginalCommandContext()
                                .channel().createMessage("Correct!").then())
                        .addMessageItem("", interaction -> interaction.getOriginalCommandContext()
                                .channel().createMessage("Nope! Try again")
                                .then(Mono.error(new UnexpectedReplyException()))) // Will trigger a retry
                        .open(ctx)
                        .then())
                        .inheritFrom(this)
                        .build());
    }
}
```

The way this command works is simple: if you send `!quiz easy` in chat, the bot will send a message "2 + 2 = ?" and will wait for your answer. If your answer is correct, it will reply with "Correct!" and will close the menu. If the answer is wrong, it will send "Nope! Try again" and will leave the menu open so you can retry. Here is how to achieve this:
* Call `create(...)` with the message you want the bot to send.
* Add items to the menu. They can be message-based or reaction-based.
* When the user replies with a specific message, or reacts with a specific reaction, the corresponding item will be triggered (a message item with empty string is triggered as a fallback when nothing is matched). For message-based interactions, the message content will be [tokenized exactly like commands](input-tokenization.md), so you can accept arguments and flags.
* You can error with `UnexpectedReplyException` to keep the menu open and let the user attempt to interact again
* Call `open(CommandContext)` to activate the interactive menu. This will make the bot send the message and wait for interactions. A menu closes either when the user has interacted or when the timeout period has passed. It is possible to prevent the menu from closing on interaction via `closeAfterMessage(false)`/`closeAfterReaction(false)`, and it is possible to automatically delete the original message on close via `deleteMenuOnClose(true)` and `deleteMenuOnTimeout(true)`. The `open` method emits a `MenuTermination` enum value that indicates whether the menu was closed via user interaction or via timeout.

:::warning
It is very important to set a reasonable timeout value. If you set a too high value, or a value of zero which means no timeout, it will create more and more subscriptions on the Discord4J event scheduler, which may cause the bot to slow down over time and eventually crash. If no value is provided in the configuration file, it will use a default timeout value of 600 seconds (10 minutes), as explained in the [configuration page](configuration.md).
:::

## Stateful menus

If you create a menu that accepts more than one reply, you may want to store the result of the previous replies somewhere. You can achieve that by using the interaction context, which is a simple `Map` that is created when the menu is opened and lasts until the menu is closed. In other words, if the same menu accepts multiple replies, the same `Map` will be used across all replies and you can use it to store state. An example below:

```java
package com.example.myproject;

import botrino.command.Command;
import botrino.command.CommandContext;
import botrino.command.CommandService;
import botrino.command.annotation.Alias;
import botrino.command.annotation.TopLevelCommand;
import com.github.alex1304.rdi.finder.annotation.RdiFactory;
import com.github.alex1304.rdi.finder.annotation.RdiService;
import reactor.core.publisher.Mono;

@Alias("stmenu")
@TopLevelCommand
@RdiService
public class StatefulMenuCommand implements Command {

    private final CommandService commandService;

    @RdiFactory
    public StatefulMenuCommand(CommandService commandService) {
        this.commandService = commandService;
    }

    @Override
    public Mono<Void> run(CommandContext ctx) {
        return commandService.interactiveMenuFactory()
                .create("First name:")
                .writeInteractionContext(map -> map.put("step", 1))
                .addMessageItem("", interaction -> {
                    int step = interaction.get("step");
                    switch (step) {
                        case 1:
                            interaction.set("firstName", interaction.getInput().getRaw());
                            interaction.set("step", 2);
                            return interaction.getOriginalCommandContext().channel().createMessage("Last name:").then();
                        case 2:
                            interaction.set("lastName", interaction.getInput().getRaw());
                            interaction.set("step", 3);
                            return interaction.getOriginalCommandContext().channel().createMessage("Age:").then();
                        default:
                            return interaction.getOriginalCommandContext().channel()
                                    .createMessage("Done! Your info:\nFirst name: " + interaction.get("firstName") +
                                            "\nLast name: " + interaction.get("lastName") + "\nAge: " +
                                            interaction.getInput().getRaw())
                                    .doOnNext(message -> interaction.closeMenu())
                                    .then();
                    }
                })
                .closeAfterMessage(false)
                .open(ctx)
                .then();
    }
}
```

We initialize the map with `writeInteractionContext()`. Then we use `closeAfterMessage(false)` to accept multiple replies, and we manually close the menu via `interaction.closeMenu()` at the end of the last step. The command works as follows:

<img src={useBaseUrl('img/stmenu.png')} alt="" />

## Paginated menus

With stateful menus you can create paginated content by saving the current page number in the interaction context. But that would be repetitive and a lot of work, especially if you want to handle the navigation through the pages (for example via reactions to go to next/previous page). You can create a paginated menu very easily using `createPaginated`:

```java
package com.example.myproject;

import botrino.api.util.MessageTemplate;
import botrino.command.Command;
import botrino.command.CommandContext;
import botrino.command.CommandService;
import botrino.command.annotation.Alias;
import botrino.command.annotation.TopLevelCommand;
import botrino.command.menu.PageNumberOutOfRangeException;
import com.github.alex1304.rdi.finder.annotation.RdiFactory;
import com.github.alex1304.rdi.finder.annotation.RdiService;
import reactor.core.publisher.Mono;

@Alias("paginated")
@TopLevelCommand
@RdiService
public class PaginatedCommand implements Command {

    private final CommandService commandService;

    @RdiFactory
    public PaginatedCommand(CommandService commandService) {
        this.commandService = commandService;
    }

    @Override
    public Mono<Void> run(CommandContext ctx) {
        return commandService.interactiveMenuFactory()
                .createPaginated((ctx0, page) -> {
                    PageNumberOutOfRangeException.check(page, 4);
                    return Mono.just(MessageTemplate.builder()
                            .setMessageContent("Page " + (page + 1) + "/5")
                            .build());
                })
                .open(ctx)
                .then();
    }
}
```

This command will create something like this in chat:

<img src={useBaseUrl('img/paginated.png')} alt="" />

You can see that it automatically configures a menu with 3 reaction-based items, one to go to next page, one to go to previous page, and one to close the menu. It also creates one message-based item called `page`, allowing you to jump to a specific page. For example, typing `page 2` in chat will make the menu jump to the second page. If you attempt to go to next page when already at last page, it will loop and go back to first page, and vice-versa. To specify the maximum page number, you are supposed to throw `PageNumberOutOfRangeException` if the `page` parameter is out of range, which you can do by calling `PageNumberOutOfRangeException.check(current, max)`.

The lambda returns a `Mono<MessageTemplate>`, a message template being an object that contains all data to display the message for a specific page, abstracting whether it's *creating* a new message or *editing* an existing message.

:::caution
In the code, page numbers are **zero-indexed**. However, on the user's end, they will be **one-indexed**. As such, the `page` parameter in the `createPaginated` lambda as well as the arguments of `PageNumberOutOfRangeException.check(current, max)` start at 0, while the page number argument expected by the `page` message-based item starts at 1.
:::

:::tip
You can customize the emojis used for pagination as explained in the [configuration page](configuration.md).
:::
