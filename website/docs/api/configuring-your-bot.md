---
title: Configuring your bot
---

Standardizing the way to configure a bot is one of the primary purposes of Botrino. This section will cover the configuration part more in detail, how to access the values of the configuration file in your code, and how to add your own configuration entries for your application.

## The configuration JSON

The configuration is a JSON object, by default located in a `config.json` in the runtime directory, each field at the root of the object corresponds to one entry and maps to one class in the Java code.

### The `ConfigContainer`

In order to access the values of the configuration in the Java code, Botrino exposes the object `ConfigContainer` as a service that you can inject in your own code. An example below:

```java
package com.example.myproject;

import botrino.api.config.ConfigContainer;
import botrino.api.config.object.BotConfig;
import botrino.api.config.object.I18nConfig;
import com.github.alex1304.rdi.finder.annotation.RdiFactory;
import com.github.alex1304.rdi.finder.annotation.RdiService;

@RdiService
public final class SomeService {

    private final BotConfig botConfig;
    private final I18nConfig i18nConfig;

    @RdiFactory
    public SomeService(ConfigContainer configContainer) {
        this.botConfig = configContainer.get(BotConfig.class);
        this.i18nConfig = configContainer.get(I18nConfig.class);
    }
}
```

The `ConfigContainer#get(Class)` method is what allows you to access the entries of the JSON config inside your code.

### Built-in configuration entries

Botrino comes with a few configuration entries by default. Here is the list of them below for reference.

#### The `bot` entry

This entry is where you input the bot information (token, presence, intents, etc). The JSON for the `bot` entry has the following structure:

```js
{
    "bot": {
        "token": "...", // string: required
        "presence": { // object: optional, default {"status": "online"}
            "status": "...", // one of "online", "idle", "dnd", "invisible": required
            "activity_type": "...", // one of "playing", "watching", "listening", "streaming": optional
            "activity_text": "..." // string: optional
        },
        "enabled_intents": 0 // integer: optional, default 32509 (all non-privileged intents)
    }
}
```

The corresponding class in the Java code is `botrino.api.config.object.BotConfig`, accessed via `ConfigContainer.get(BotConfig.class)`.

#### The `i18n` entry

This entry is where you specify the localization settings (default locale and supported locales). The JSON for the `i18n` entry has the following structure:

```js
{
    "i18n": {
        "default_locale": "...", // string: required, must be a valid locale code ("en", "en-GB", "fr-FR"...)
        "supported_locales": [] // array of strings: optional, values must be valid locale codes
    }
}
```

The corresponding class in the Java code is `botrino.api.config.object.I18nConfig`, accessed via `ConfigContainer.get(I18nConfig.class)`.

### Adding your own configuration entries

The configuration JSON can of course be extended with more entries to include your own parameters.

#### Creating the configuration object

First step is to create a POJO like this:

```java
package com.example.myproject;

import botrino.api.annotation.ConfigEntry;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

@JsonDeserialize
@ConfigEntry("my_config")
public final class MyConfig {

    private String myProperty;
    private long myValue;

    public String getMyProperty() {
        return myProperty;
    }

    @JsonProperty("my_property")
    public void setMyProperty(String myProperty) {
        this.myProperty = myProperty;
    }

    public long getMyValue() {
        return myValue;
    }

    @JsonProperty("my_value")
    public void setMyValue(long myValue) {
        this.myValue = myValue;
    }
}
```

* The `@JsonDeserialize` annotation is to indicate that this class is intended for being constructed from a JSON input, processed by the Jackson library.
* The `@ConfigEntry` annotation allows Botrino to recognize it as a configuration object to be registered in the `ConfigContainer`, and to indicate the name of the field in the configuration file.

#### Adding the entry in the JSON file

Once you've created the object, you can add the following in your `config.json`:

```json {8-11}
{
    "bot": {
        ...
    },
    "i18n": {
        ...
    },
    "my_config": {
        "my_property": "hello!!!",
        "my_value": 42
    }
}
```

:::info
The name of the root field in the JSON must match with the name given in the `@ConfigEntry` annotation.
:::

#### Using the configuration object

To test this, we can create a sample service injecting the `ConfigContainer`:

```java
package com.example.myproject;

import botrino.api.config.ConfigContainer;
import com.github.alex1304.rdi.finder.annotation.RdiFactory;
import com.github.alex1304.rdi.finder.annotation.RdiService;
import reactor.util.Logger;
import reactor.util.Loggers;

@RdiService
public final class SampleService {

    private static final Logger LOGGER = Loggers.getLogger(SampleService.class);

    @RdiFactory
    public SampleService(ConfigContainer configContainer) {
        var myConfig = configContainer.get(MyConfig.class);
        LOGGER.info("My property = {}, my value = {}", myConfig.getMyProperty(), myConfig.getMyValue());
    }
}
```

When running, it should give the following output:
```
00:16:42.193 [main] DEBUG botrino.api.Botrino - Discovered config entry com.example.myproject.MyConfig
00:16:42.468 [main] INFO  com.example.myproject.SampleService - My property = hello!!!, my value = 42
```

## Customizing the JSON source

It is possible to override the behavior of Botrino when loading the configuration by implementing the `ConfigReader` interface. This interface has two methods, none of them are required to be implemented:
* `String loadConfigJson(Path botDirectory) throws IOException`: Allows to customize the way the configuration file is loaded. It is useful if you want to load the configuration from a file that is located at a different path or that has a different name than "config.json". You can even ignore the `botDirectory` parameter and load the JSON from a different source, or directly return a hard-coded JSON string for testing purposes for example. Note that this method throws `IOException` and that the return type is not reactive: indeed, this method is ran by Botrino on the main thread at the very start of the program, as such it does not need to be (and shouldn't be) asynchronous. This method is not required to be implemented: it has a default implementation that will simply read the JSON string from a file named `config.json` at the root of `botDirectory`.
* `ObjectMapper createConfigObjectMapper()`: Allows to customize the Jackson `ObjectMapper` instance used to parse the JSON string. You can for example register extra modules and deserializers. This method is not required to be implemented: by default it will create an `ObjectMapper` with only the `Jdk8Module` registered (allows to recognize types such as `java.util.Optional`).

If no `ConfigReader` implementation is found in your module, it will use a default one which can be recreated like this:

```java
package com.example.myproject;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public final class DefaultConfigReader implements ConfigReader {

    @Override
    public String loadConfigJson(Path botDirectory) throws IOException {
        return Files.readString(botDirectory.resolve("config.json"));
    }

    @Override
    public ObjectMapper createConfigObjectMapper() {
        return new ObjectMapper().registerModule(new Jdk8Module());
    }
}
```

:::caution
* The implementation class must have a no-arg constructor.
* If more than one implementation of `ConfigReader` are found, it will result in an error as it is impossible to determine which one to use. If you don't want to remove the extra implementation(s), you can mark one of them with the `@Primary` annotation to lift the ambiguity. You may alternatively use the `@Exclude` annotation if you don't want one implementation to be picked up by Botrino.
:::
