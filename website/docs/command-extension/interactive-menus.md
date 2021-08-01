---
title: Interactive menus
---

import useBaseUrl from '@docusaurus/useBaseUrl';

In some situations, running commands isn't the most intuitive solution to interact with a bot. Common examples are: choosing an item in a list, navigating in a paginated content, or triggering an extra action after successfully running a command (e.g "do it again" or "delete the original message"). In these situations you may prefer a solution where users can interact in a more straightforward way, either by replying with a one-word message, by using reactions, or by using the brand new [Message Components](https://discord.com/developers/docs/interactions/message-components) feature of Discord. This can be done intuitively with **interactive menus**.

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
public final class QuestionCommand implements ParentCommand {

    @Override
    public Set<Command> subcommands() {
        return Set.of(
                Command.builder(Set.of("easy"), ctx -> InteractiveMenu.create("2 + 2 = ?")
                        .addMessageItem("4", interaction -> interaction.getOriginalCommandContext()
                                .channel().createMessage("Correct!").then())
                        .addMessageItem("", interaction -> interaction.getOriginalCommandContext()
                                .channel().createMessage("Nope! Try again")
                                .then(Mono.error(new UnexpectedReplyException()))) // Will trigger a retry
                        .open(ctx)
                        .then())
                        .inheritFrom(this)
                        .build(),
                Command.builder(Set.of("hard"), ctx -> InteractiveMenu.create("12455 * 2442 - 85 = ?")
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
* Add items to the menu. They can be message-based, reaction-based or based on message components.
* When the user replies with a specific message, reacts with a specific reaction, or interacts with a button or a select menu, the corresponding item will be triggered (a message item with empty string is triggered as a fallback when nothing is matched). For message-based interactions, the message content will be [tokenized exactly like commands](input-tokenization.md), so you can accept arguments and flags.
* If you receive an interaction that was not expected or doesn't meet certain conditions, you can error with `UnexpectedReplyException` to keep the menu open and let the user attempt to interact again
* Call `open(CommandContext)` to activate the interactive menu. This will make the bot send the menu message and wait for interactions. A menu closes either when the user has interacted or when the timeout period has passed. It is possible to prevent the menu from closing on interaction via `closeAfterXxx(false)`, and it is possible to automatically delete the original message on close via `deleteMenuOnClose(true)` and `deleteMenuOnTimeout(true)`. The `open` method emits a `MenuTermination` enum value that indicates whether the menu was closed via user interaction or via timeout.

:::warning
If you want to customize the duration of the timeout, it is very important to set a reasonable value. If you set a too high duration, or a value of zero which means no timeout, it may not release the resources used for listening to the Discord Gateway events, which may cause the bot to slow down over time and eventually crash. The default timeout value is 10 minutes.
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
public class StatefulMenuCommand implements Command {

    @Override
    public Mono<Void> run(CommandContext ctx) {
        return InteractiveMenu.create("First name:")
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

With stateful menus you can create paginated content by saving the current page number in the interaction context. However, that would be repetitive and a lot of work, especially if you want to handle the navigation through the pages (for example via buttons to go to next/previous page). You can create a paginated menu very easily using `createPaginated`:

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
public class PaginatedCommand implements Command {

    @Override
    public Mono<Void> run(CommandContext ctx) {
        return commandService.interactiveMenuFactory()
                .createPaginated((ctx0, page) -> Mono.just(MessageTemplate.builder()
                            .setMessageContent("Page " + (page + 1) + "/5")
                            .build()), PaginationButtonGroup.create(), 5)
                .open(ctx)
                .then();
    }
}
```

This command will create something like this in chat:

<img src={useBaseUrl('img/paginated.png')} alt="" />

You can see that it automatically configures a menu with 3 button-based items, one to go to next page, one to go to previous page, and one to close the menu. It also creates one message-based item called `page`, allowing you to jump to a specific page. For example, typing `page 2` in chat will make the menu jump to the second page. Previous button will appear disabled if you are on first page, next button will appear disabled if you are on last page.

:::caution
In the code, page numbers are **zero-indexed**. However, on the user's end, they will be **one-indexed**. As such, the `page` parameter in the `createPaginated` lambda starts at 0, while the page number argument expected by the `page` message-based item starts at 1.
:::
