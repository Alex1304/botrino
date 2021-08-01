# Botrino

A simple yet powerful framework to develop, configure and run Discord bots based on Discord4J.

![GitHub release (latest SemVer)](https://img.shields.io/github/v/release/Alex1304/botrino?sort=semver)
[![Maven Central](https://img.shields.io/maven-central/v/com.alex1304.botrino/botrino-api)](https://search.maven.org/artifact/com.alex1304.botrino/botrino-api)
![License](https://img.shields.io/github/license/Alex1304/botrino)
[![javadoc](https://javadoc.io/badge2/com.alex1304.botrino/botrino-api/javadoc.svg)](https://javadoc.io/doc/com.alex1304.botrino/botrino-api)

<img align="right" style="padding:40px;" src="https://botrino.alex1304.com/img/logo.svg" width="20%" />

## What is Botrino?

Botrino is a Java framework that provides guidelines and a set of tools to build Discord bots in a more convenient way. Pretty much in the same spirit as [Spring Boot](https://spring.io/projects/spring-boot), it allows to easily setup standalone bot applications that are ready to run, embedding a few third-party libraries such as [Jackson](https://github.com/FasterXML/jackson-core) for JSON-based configuration, [RDI](https://alex1304.github.io/rdi/docs/intro) for a reactive IoC container, and [Discord4J](https://discord4j.com) for the interface with the [Discord Bot API](https://discord.com/developers/docs/intro).

## Motivations

Starting the development of a Discord bot follows most of the time the same pattern: you create a project, import your favorite Discord client library, export a configuration file or an environment variable with the bot token, and design a whole structure for your commands and your logic, before you can actually start to implement them. When working with Java, this structure is even more important otherwise you may adopt bad practices and end up with a bot that is difficult to maintain.

This is how came the idea of this project: have something that can handle for you all the initial workflow of setting up a project with a solid structure, at the only cost of letting the framework choose some libraries for you, so that you can focus on what matters. Botrino is born.

It also aims at providing a [command extension](https://botrino.alex1304.com/docs/command-extension/overview) that integrates well with the structure of Botrino, while still letting you the choice of using your own.

## Overview

Botrino utilizes Java modules, introduced in the JDK 9 and released in the JDK 11 as a LTS version. The classes of your application will reside in one or more modules with the following `module-info.java` structure:

```java
import botrino.api.annotation.BotModule;

@BotModule
open module com.example.myproject {

    requires botrino.api;
}
```

The annotation as well as the `open` modifier will allow Botrino to automatically scan through all the classes present in the module, in order to automatically register configuration entries, commands, services, etc.

Inside your module, you can create services using [RDI annotations](https://alex1304.github.io/rdi/docs/annotation-based-configuration) that are automatically loaded on startup:

```java
package com.example.myproject;

import com.github.alex1304.rdi.finder.annotation.RdiFactory;
import com.github.alex1304.rdi.finder.annotation.RdiService;
import discord4j.core.GatewayDiscordClient;
import discord4j.core.event.domain.lifecycle.ReadyEvent;
import reactor.core.publisher.Mono;
import reactor.util.Logger;
import reactor.util.Loggers;

@RdiService
public final class SampleService {

    private static final Logger LOGGER = Loggers.getLogger(SampleService.class);

    // We can inject other services, here we are injecting
    // the GatewayDiscordClient provided by Botrino
    @RdiFactory
    public SampleService(GatewayDiscordClient gateway) {
        gateway.on(ReadyEvent.class, ready -> Mono.fromRunnable(
                        () -> LOGGER.info("Logged in as "
                                + ready.getSelf().getTag())))
                .subscribe();
    }
}

```

The bot is configured via a JSON configuration file with contents similar to this:

```json
{
    "bot": {
        "token": "yourTokenHere",
        "presence": {
            "status": "online",
            "activity_type": "playing",
            "activity_text": "Hello world!"
        },
        "enabled_intents": 32509
    },
    "i18n": {
        "default_locale": "en",
        "supported_locales": ["en"]
    }
}
```

## Command extension

Botrino comes with an extension that allows to easily create commands based on message create events. Although Discord recently added [Slash Commands](https://discord.com/developers/docs/interactions/slash-commands) as a native way to implement commands within Discord, message create-based commands will certainly remain the most flexible solution.

### Preamble

By definition, as this is an extension, it does not belong to the core framework API and you are not required to use it. It aims at giving you enough flexibility to cover the majority of use cases, but for very specific ones you might need to implement your own solution. That's why feedback on this extension is more than welcome, if you feel something is missing feel free to open an issue on the [GitHub repository](https://github.com/Alex1304/botrino).

### Features

* Register unlimited commands
* Message tokenization into arguments and flags
* Apply a grammar to command arguments to conveniently convert them into actual Java types, supporting required, optional, and varying arguments
* Unlimited subcommands
* Attach a documentation to all your commands and subcommands
* Define privileges for each command with your own rules
* Cooldowns
* Global and per-command error handling
* Process message create events to filter them or to adapt prefix and language
* Interactive menus

### Code examples

A basic `!ping` command:

```java
package com.example.myproject;

import botrino.interaction.Command;
import botrino.interaction.CommandContext;
import botrino.interaction.annotation.Alias;
import botrino.interaction.annotation.TopLevelCommand;
import reactor.core.publisher.Mono;

@TopLevelCommand
@Alias("ping")
public final class PingCommand implements Command {

    @Override
    public Mono<Void> run(CommandContext ctx) {
        return ctx.channel()
                .createMessage(ctx.translate(Strings.APP, "ping"))
                .then();
    }
}
```

A `!sendword <word> <count> [channels...]` command that sends a word `count` times in each of the specified channels. Requires `ADMINISTRATOR` permission and may be used at most once every 1 minute:

```java
package com.example.myproject;

import botrino.interaction.Command;
import botrino.interaction.CommandContext;
import botrino.interaction.annotation.Alias;
import botrino.interaction.annotation.TopLevelCommand;
import botrino.interaction.grammar.ArgumentMapper;
import botrino.interaction.grammar.CommandGrammar;
import botrino.interaction.privilege.Privilege;
import botrino.interaction.privilege.Privileges;
import botrino.interaction.cooldown.Cooldown;
import discord4j.core.object.entity.channel.GuildChannel;
import discord4j.core.object.entity.channel.GuildMessageChannel;
import discord4j.rest.util.Permission;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.List;

@TopLevelCommand
@Alias("sendword")
public final class SendWordCommand implements Command {

    private final CommandGrammar<Args> grammar = CommandGrammar.builder()
            .nextArgument("word")
            .nextArgument("count", ArgumentMapper.asInteger())
            .nextArgument("channels", ArgumentMapper.asGuildChannel())
            .setVarargs(true)
            .build(Args.class);

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
    public Cooldown cooldown() {
        return Cooldown.of(1, Duration.ofMinutes(1));
    }

    @Override
    public Privilege privilege() {
        return Privileges.checkPermissions(
                ctx -> new PrivilegeException(ctx.translate(Strings.APP,
                        "error_requires_admin")),
                perms -> perms.contains(Permission.ADMINISTRATOR));
    }

    private static final class Args {
        private String word;
        private int count;
        private List<GuildChannel> channels;
    }
}
```

## Getting Started

### Prerequisites

* JDK 11 or above. You can download the OpenJDK [here](https://adoptopenjdk.net/?variant=openjdk11&jvmVariant=hotspot)
* Apache Maven 3, preferably the latest version available [here](https://maven.apache.org/download.cgi).

### From the Maven Archetype

The recommended way to start a project with Botrino is to use the Maven archetype (replace `[VERSION]` with the latest version available): [![Maven Central](https://img.shields.io/maven-central/v/com.alex1304.botrino/botrino-api)](https://search.maven.org/artifact/com.alex1304.botrino/botrino-api)

```
mvn archetype:generate -DarchetypeGroupId=com.alex1304.botrino -DarchetypeArtifactId=botrino-archetype -DarchetypeVersion=[VERSION]
```

You will be asked to enter the `groupId`, the `artifactId`, the `version` and the `package` of your project.

### From a blank project

You may as well start from a blank project and import Botrino yourself. Be aware that it will require a bit more effort to set up than using the archetype.

Import the following dependencies (if you don't want the command extension you can omit `botrino-command`):

Maven:
```xml
<dependency>
    <groupId>com.alex1304.botrino</groupId>
    <artifactId>botrino-api</artifactId>
    <version>[VERSION]</version>
</dependency>
<dependency>
    <groupId>com.alex1304.botrino</groupId>
    <artifactId>botrino-command</artifactId>
    <version>[VERSION]</version>
</dependency>
```

Gradle:
```groovy
repositories {
    mavenCentral()
}

dependencies {
    implementation 'com.alex1304.botrino:botrino-api:[VERSION]'
    implementation 'com.alex1304.botrino:botrino-command:[VERSION]'
}
```

Create a `module-info.java` annotated with `@BotModule`, with the `open` modifier and that requires the Botrino API module:

```java
import botrino.api.annotation.BotModule;

@BotModule
open module com.example.myproject {

    requires botrino.api;
    requires botrino.command; // if using command extension
}
```

The module `botrino.api` transitively requires all libraries necessary to work, including Discord4J, Reactor, Netty, RDI and Jackson, so you don't need to put `requires` for those libraries. **If you get compilation errors, remember to configure your project to use JDK 11 or above.**

Finally, add a class with a `main` method:

```java
package com.example.myproject;

import botrino.api.Botrino;

public final class Main {

    public static void main(String[] args) {
        Botrino.run(args);
    }
}
```

**A more complete guide to get started and to run the bot can be found on the [documentation website](https://botrino.alex1304.com/docs/getting-started).**

## Discord4J version interoperability

Major and minor version numbers of Botrino will match with a minor and major version of Discord4J. Botrino **v1.0.x** supports Discord4J **v3.2.x**; **v1.1.x** will support **v3.3.x**, etc. The patch version number however will be independent from the patch version of Discord4J.
Find the table below for reference regarding version dependencies:

Botrino version | Discord4J version | Reactor version | RDI version
----------------|-------------------|-----------------|-----------------
v1.0.0-RC1      | v3.2.0-M3         | v3.4.4          | v1.1.2
v1.0.0-M3       | v3.2.0-M3         | v3.4.4          | v1.1.1
v1.0.0-M2       | v3.2.0-M1         | v3.4.1          | v1.1.1
v1.0.0-M1       | v3.2.0-M1         | v3.4.1          | v1.1.0

## Useful links

* [Botrino website with full documentation](https://botrino.alex1304.com)
* [Javadoc (API)](https://www.javadoc.io/doc/com.alex1304.botrino/botrino-api/latest/index.html)
* [Javadoc (Command extension)](https://www.javadoc.io/doc/com.alex1304.botrino/botrino-command/latest/index.html)
* [Discord4J website](https://discord4j.com)

## License

This project is licensed under the MIT license.

## Contributions

Have a feature to suggest or a bug to report ? Issues and pull requests are more than welcome! Make sure to follow the template and share your ideas.

## Contact

If you wish to contact me directly, you can DM me on Discord (Alex1304#9704) or send an email to mirandaa1304@gmail.com. Depending on how this project turns out, a community Discord server can be considered for the future.
