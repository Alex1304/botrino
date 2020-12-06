# Botrino

A simple yet powerful framework to develop, configure and run Discord bots based on Discord4J.

![GitHub release (latest SemVer)](https://img.shields.io/github/v/release/Alex1304/botrino?sort=semver)
[![Maven Central](https://img.shields.io/maven-central/v/com.alex1304.botrino/botrino)](https://search.maven.org/artifact/com.alex1304.botrino/botrino)
![License](https://img.shields.io/github/license/Alex1304/botrino)
[![javadoc](https://javadoc.io/badge2/com.alex1304.botrino/botrino/javadoc.svg)](https://javadoc.io/doc/com.alex1304.botrino/botrino) 

🚧 **The framework does not yet have a public release, and the docs are still in construction. It is published only for showcase and feedback purposes, a first release will happen soon&trade;**

<img align="right" style="padding:40px;" src="https://botrino.alex1304.com/img/logo.svg" width="20%" />

## What is Botrino?

Botrino is a Java framework that provides guidelines and a set of tools to build Discord bots in a more convenient way. Pretty much in the same spirit as [Spring Boot](https://spring.io/projects/spring-boot), it allows to easily setup standalone bot applications that are ready to run, embedding a few third-party libraries such as [Jackson](https://github.com/FasterXML/jackson-core) for JSON-based configuration, [RDI](https://alex1304.github.io/rdi/docs/intro) for a reactive IoC container, and [Discord4J](https://discord4j.com) for the interface with the [Discord Bot API](https://discord.com/developers/docs/intro).

## Motivations

Starting the development of a Discord bot follows most of the time the same pattern: you create a project, import your favorite Discord client library, export a configuration file or an environment variable with the bot token, and design a whole structure for your commands and your logic, before you can actually start to implement them. When working with Java, this structure is even more important otherwise you may adopt bad practices and end up with a bot that is difficult to maintain.

This is how came the idea of this project: have something that can handle for you all the initial workflow of setting up a project with a solid structure, at the only cost of letting the framework choose some libraries for you, so that you can focus on what matters. Botrino is born.

It also aims at providing a command module that integrates well with the structure of Botrino, while still letting you the choice of using your own.

## Overview

Botrino utilizes Java modules, introduced in the JDK 9 and released in the JDK 11 as a LTS version. The classes of your application will reside in one or more modules with the following `module-info.java` structure:

```java
import botrino.api.annotation.BotModule;

@BotModule
open module your.app {

    requires botrino.api;
}
```

The annotation as well as the `open` modifier will allow Botrino to automatically scan through all the classes present in the module, in order to automatically register configuration entries, commands, services, etc.

Inside your module, you can create services using [RDI annotations](https://alex1304.github.io/rdi/docs/annotation-based-configuration) that are automatically loaded on startup:

```java
package your.app;

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

The bot is configured via a `config.json` file in the working directory of the app with contents similar to this:

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

To go further and familiarize yourself with the framework, check out the [Getting Started guide](https://botrino.alex1304.com/docs/getting-started).

## Useful links

* [Botrino website with full documentation](https://botrino.alex1304.com)
* [Javadoc](https://www.javadoc.io/doc/com.alex1304.botrino/botrino/latest/index.html)
* [Discord4J website](https://discord4j.com)

## License

This project is licensed under the MIT license.

## Contributions

Have a feature to suggest or a bug to report ? Issues and pull requests are more than welcome! Make sure to follow the template and share your ideas.

## Contact

If you wish to contact me directly, you can DM me on Discord (Alex1304#9704) or send an email to mirandaa1304@gmail.com. Depending on how this project turns out, a community Discord server can be considered for the future.
