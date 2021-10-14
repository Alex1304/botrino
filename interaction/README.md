# Botrino Interaction

Botrino comes with a library that allows to easily create application commands and listeners for Discord interactions. Discord recently added [Application Commands](https://discord.com/developers/docs/interactions/application-commands) as a native way to implement commands within Discord, as well as [Message Components](https://discord.com/developers/docs/interactions/message-components) to allow for more specific and intuitive interactions with the bot.

[![Maven Central](https://img.shields.io/maven-central/v/com.alex1304.botrino/botrino-interaction)](https://search.maven.org/artifact/com.alex1304.botrino/botrino-interaction)
[![javadoc](https://javadoc.io/badge2/com.alex1304.botrino/botrino-interaction/javadoc.svg)](https://javadoc.io/doc/com.alex1304.botrino/botrino-interaction)

## Preamble

Although it is designed to be an extension of the Botrino framework, this library is completely decoupled from the framework itself. As such, it is possible to add this library to your project even if you aren't using the framework. The only difference is that you won't benefit from the automatic registration of commands, but you will be able to use all features.

## Features

* Straightforward annotation-based command declaration, with full support for slash commands with subcommands/subcommand groups and context menu commands
* Automatic registration of commands into the Discord application, with ability to choose whether to deploy them globally or in a specific guild during development
* Inject command options into fields using annotations to conveniently access the values
* Handle component interactions either by treating them as regular commands or by awaiting them in order to continue the execution of a parent command
* Automatic ACK of interaction events, configurable on a per-command basis
* Define privileges for each command with your own rules
* Cooldown per user
* Centralized error handling
* Pre-process interaction events by filtering them or adapting the locale to the target user
* Utilities such as pagination system using buttons

## Quick start

**JDK 11 or newer is required.** You can download the OpenJDK [here](https://adoptopenjdk.net/?variant=openjdk11&jvmVariant=hotspot).

You have two ways to start a project. **Choose only one of the two options.**

### Option 1: Using Botrino framework

1. Initialize your project by following the steps in the [Getting Started page](https://botrino.alex1304.com/docs/getting-started). **If you decide to use the Maven archetype, the library will be already added, so you won't need to follow the next steps!** Otherwise, continue reading.

2. Add the following dependency in your project. Replace [VERSION] with the latest version as shown here: [![Maven Central](https://img.shields.io/maven-central/v/com.alex1304.botrino/botrino-interaction)](https://search.maven.org/artifact/com.alex1304.botrino/botrino-interaction)

   **Maven:**

    ```xml
    <dependency>
        <groupId>com.alex1304.botrino</groupId>
        <artifactId>botrino-interaction</artifactId>
        <version>[VERSION]</version>
    </dependency>
    ```

    **Gradle**

    ```groovy
    repositories {
        mavenCentral()
    }
    
    dependencies {
        implementation 'com.alex1304.botrino:botrino-interaction:[VERSION]'
    }
    ```

3. Add `requires botrino.interaction` in your `module-info.java`. You can actually remove `requires botrino.api` since `botrino.interaction` is already requiring it transitively.
    ```java
    import botrino.api.annotation.BotModule;

    @BotModule
    open module com.example.myproject {
    
        requires botrino.interaction;
    }
    ```
4. Add the following line in the end of your `config.json`:
    ```json
        {
            // ...
            "interaction": {}
        }
    ```

You are now ready to add commands to your application, they will be registered automatically! You can jump directly to the [Creating your first commands](#creating-your-first-commands) section.

### Option 2: Using the library alone

1. Add the following dependency in your project. Replace [VERSION] with the latest version as shown here: [![Maven Central](https://img.shields.io/maven-central/v/com.alex1304.botrino/botrino-interaction)](https://search.maven.org/artifact/com.alex1304.botrino/botrino-interaction)

   **Maven:**

    ```xml
    <dependency>
        <groupId>com.alex1304.botrino</groupId>
        <artifactId>botrino-interaction</artifactId>
        <version>[VERSION]</version>
    </dependency>
    ```

   **Gradle**

    ```groovy
    repositories {
        mavenCentral()
    }
    
    dependencies {
        implementation 'com.alex1304.botrino:botrino-interaction:[VERSION]'
    }
    ```

2. Create a main method with these few lines of code:

    ```java
    package testbot1;
    
    import botrino.interaction.InteractionService;
    import botrino.interaction.config.InteractionConfig;
    import discord4j.core.DiscordClient;
    
    public final class Main {
    
        public static void main(String[] args) {
            final var config = InteractionConfig.builder()
                    // Slash commands will be deployed to this guild (recommended during dev).
                    // Not specifying anything will deploy globally.
                    .applicationCommandsGuildId(361255823357509645L)
                    .build();
            // Login to Discord using the token passed as program argument
            final var gateway = DiscordClient.create(args[0]).login().block();
            // Initialize the interaction service
            final var interactionService = InteractionService.create(config, gateway);
            // Register your commands
            interactionService.registerChatInputCommand(new PingCommand());
            // Listen for interaction events until the bot disconnects
            interactionService.run().takeUntilOther(gateway.onDisconnect()).block();
        }
    }
    ```

## Creating your first commands

### A basic ping command

```java
package testbot1;

import botrino.interaction.annotation.ChatInputCommand;
import botrino.interaction.listener.ChatInputInteractionListener;
import botrino.interaction.context.ChatInputInteractionContext;
import org.reactivestreams.Publisher;

@ChatInputCommand(name = "ping", description = "Pings the bot to check if it is alive.")
public final class PingCommand implements ChatInputInteractionListener {

    @Override
    public Publisher<?> run(ChatInputInteractionContext ctx) {
        return ctx.event().createFollowup("Pong !");
    }
}
```

### A command with options

```java
package testbot1;

import botrino.interaction.annotation.ChatInputCommand;
import botrino.interaction.context.ChatInputInteractionContext;
import botrino.interaction.grammar.ChatInputCommandGrammar;
import botrino.interaction.listener.ChatInputInteractionListener;
import discord4j.core.object.command.ApplicationCommandOption;
import discord4j.discordjson.json.ApplicationCommandOptionData;
import org.reactivestreams.Publisher;

import java.util.List;

@ChatInputCommand(name = "options", description = "Option testing")
public class OptionsCommand implements ChatInputInteractionListener {

    private final ChatInputCommandGrammar<Options> grammar = ChatInputCommandGrammar.of(Options.class);

    @Override
    public Publisher<?> run(ChatInputInteractionContext ctx) {
        return grammar.resolve(ctx.event()).flatMap(options -> ctx.event()
                .createFollowup("Value of `my-string`: " + options.myString));
    }

    @Override
    public List<ApplicationCommandOptionData> options() {
        return grammar.toOptions();
    }

    private static class Options {
        @ChatInputCommandGrammar.Option(
                type = ApplicationCommandOption.Type.STRING,
                name = "my-string",
                description = "The string argument",
                required = true,
                choices = {
                        @ChatInputCommandGrammar.Choice(name = "Choice 1", stringValue = "1"),
                        @ChatInputCommandGrammar.Choice(name = "Choice 2", stringValue = "2"),
                        @ChatInputCommandGrammar.Choice(name = "Choice 3", stringValue = "3")
                }
        )
        String myString;
    }
}
```

### A command with subcommands and subcommand groups

```java
@ChatInputCommand(
        name = "nest",
        description = "Subcommand testing",
        subcommands = {
                @Subcommand(name = "sub1", description = "Subcommand 1", listener = NestCommand.Sub1.class),
                @Subcommand(name = "sub2", description = "Subcommand 2", listener = NestCommand.Sub2.class)
        },
        subcommandGroups = {
                @SubcommandGroup(name = "group1", description = "Group 1", subcommands = {
                        @Subcommand(name = "sub", description = "Subcommand", listener = NestCommand.G1Sub.class)
                })
        }
)
public final class NestCommand {

    public static final class Sub1 implements ChatInputInteractionListener {

        @Override
        public Publisher<?> run(ChatInputInteractionContext ctx) {
            return ctx.event().createFollowup("sub1: pong!");
        }
    }

    public static final class Sub2 implements ChatInputInteractionListener {

        @Override
        public Publisher<?> run(ChatInputInteractionContext ctx) {
            return ctx.event().createFollowup("sub2: pong!");
        }
    }

    public static final class G1Sub implements ChatInputInteractionListener {

        @Override
        public Publisher<?> run(ChatInputInteractionContext ctx) {
            return ctx.event().createFollowup("group1 sub: pong!");
        }
    }
}
```

### A command using component interactions

```java
package testbot1;

import botrino.interaction.annotation.ChatInputCommand;
import botrino.interaction.context.ChatInputInteractionContext;
import botrino.interaction.listener.ChatInputInteractionListener;
import discord4j.core.object.component.ActionRow;
import discord4j.core.object.component.SelectMenu;
import discord4j.core.object.entity.Message;
import discord4j.core.spec.InteractionFollowupCreateSpec;
import org.reactivestreams.Publisher;
import reactor.core.publisher.Mono;

import java.util.UUID;

import static botrino.interaction.listener.ComponentInteractionListener.selectMenu;

@ChatInputCommand(name = "select", description = "Command for testing select menus")
public class SelectCommand implements ChatInputInteractionListener {

    @Override
    public Publisher<?> run(ChatInputInteractionContext ctx) {
        final var customId = UUID.randomUUID().toString();
        return ctx.event().createFollowup("Select an option:")
                .withComponents(ActionRow.of(SelectMenu.of(customId,
                        SelectMenu.Option.of("option 1", "foo"),
                        SelectMenu.Option.of("option 2", "bar"),
                        SelectMenu.Option.of("option 3", "baz"))))
                .map(Message::getId)
                // Wait until the select menu is interacted with and return the value clicked
                .flatMap(messageId -> ctx.awaitSelectMenuItems(customId)
                        .flatMap(items -> ctx.event().createFollowup("You clicked: " + items.get(0))
                                .then(ctx.event().deleteFollowup(messageId))));
    }
}
```

### A command using a pagination system based on components

```java
package testbot1;

import botrino.interaction.annotation.ChatInputCommand;
import botrino.interaction.context.ChatInputInteractionContext;
import botrino.interaction.listener.ChatInputInteractionListener;
import botrino.interaction.util.MessagePaginator;
import discord4j.core.object.component.ActionRow;
import discord4j.core.object.component.Button;
import discord4j.core.spec.MessageCreateSpec;
import org.reactivestreams.Publisher;
import reactor.core.publisher.Mono;

@ChatInputCommand(name = "paginate", description = "Pagination testing")
public final class PaginateCommand implements ChatInputInteractionListener {

    @Override
    public Publisher<?> run(ChatInputInteractionContext ctx) {
        return MessagePaginator.paginate(ctx, 5, state -> Mono.just(MessageCreateSpec.create()
                .withContent("Page " + (state.getPage() + 1) + "/" + state.getPageCount())
                .withComponents(ActionRow.of(
                        state.previousButton(customId -> Button.secondary(customId, "<< Previous")),
                        state.nextButton(customId -> Button.secondary(customId, "Next >>")),
                        state.closeButton(customId -> Button.danger(customId, "Close"))
                ))));
    }
}
```

<img src="https://botrino.alex1304.com/img/paginate.png" alt="" />

**All of these features (and more !) are fully documented here:** https://botrino.alex1304.com/docs/interaction-library/overview