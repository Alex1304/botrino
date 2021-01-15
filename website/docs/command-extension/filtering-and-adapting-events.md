---
title: Filtering and adapting events
---

import useBaseUrl from '@docusaurus/useBaseUrl';

Commands are triggered when a message create event is received via the Discord gateway. It is possible for you to intervene between the moment the event is received and the moment it parses the message content to trigger a command. This allows you to drop some events to prevent any command from being executed in a certain context, or to determine which prefix the command must start with and which locale to use according to the event received.

## The `CommandEventProcessor` interface

Create a class in your bot module that implements `CommandEventProcessor`. Botrino will pick it up and will automatically inject it in the internal listener to message create events.

```java
package com.example.myproject;

import botrino.command.CommandEventProcessor;
import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.object.entity.channel.GuildMessageChannel;
import reactor.core.publisher.Mono;

import java.util.Locale;

public final class ExampleEventProcessor implements CommandEventProcessor {

    @Override
    public Mono<Boolean> filter(MessageCreateEvent event) {
        return Mono.just(!event.getMessage().getAuthor()
                            .map(User::isBot).orElse(true)
                && !event.getMessage().getContent().contains("ignore"));
    }

    @Override
    public Mono<String> prefixForEvent(MessageCreateEvent event) {
        return event.getGuild()
                .map(guild -> guild.getName().charAt(0))
                .filter(c -> ("" + c).matches("[A-Za-z0-9]"))
                .map(c -> c + "!");
    }

    @Override
    public Mono<Locale> localeForEvent(MessageCreateEvent event) {
        return event.getMessage().getChannel()
                .ofType(GuildMessageChannel.class)
                .flatMap(channel -> {
                    if (channel.getName().endsWith("fr")) {
                        return Mono.just(Locale.FRENCH);
                    } else if (channel.getName().endsWith("de")) {
                        return Mono.just(Locale.GERMAN);
                    } else {
                        return Mono.empty();
                    }
                });
    }
}
```

* `filter(MessageCreateEvent)` allows to decide whether to keep or to drop the given `MessageCreateEvent`. The default implementation will drop events from bot accounts and webhooks (unless you know what you're doing, **it is highly recommended to keep this behavior**). This method can also be useful if you want to implement a blacklist system to prevent some people or some guilds from using the commands of your bot. In this example, we are filtering out events coming from bots, but we are also filtering out events which message content contains the string "ignore".
* `prefixForEvent(MessageCreateEvent)` allows to dynamically change the command prefix according to the event received. In order to keep the default prefix, you are expected to return an empty `Mono`. If you want to provide a way for your guilds to configure their own custom prefix, this is where you would implement it. In this example, we are taking the first letter of the guild name, and define the prefix as the letter followed by `!`. If the message was sent in DMs or if the guild name doesn't start with an alphanumeric character, it will use the default prefix of the bot defined in the configuration file.
* `localeForEvent(MessageCreateEvent)` allows to adapt the locale according to the event received. In order to keep the default locale, you are expected to return an empty `Mono`. The returned locale will be accessible via the `CommandContext` (which implements `Translator`) once the event is recognized as a command. You will typically store the locale in a database (either per guild or per user) and retrieve it here using the data given by the message create event. In this example, we are interpreting the suffix of the channel name as the locale, with only French and German being supported. It will use the default locale defined in the configuration file in all other cases.

:::caution
* The implementation class must have a no-arg constructor.
* If more than one implementation of `CommandEventProcessor` are found, it will result in an error as it is impossible to determine which one to use. If you don't want to remove the extra implementation(s), you can mark one of them with the `@Primary` annotation to lift the ambiguity. You may alternatively use the `@Exclude` annotation if you don't want one implementation to be picked up by Botrino.
:::

Here is the example implementation above in action, in a guild named "test". You can notice the bot now responds with `t!` instead of the default prefix as the guild name starts with "t", and the `t!ping ignore` produces no response because the event was dropped due to the presence of "ignore" in the message content.

<img src={useBaseUrl('img/eventProcessorExample.png')} alt="" />
