---
title: Getting Started
---

import useBaseUrl from '@docusaurus/useBaseUrl';
import Tabs from '@theme/Tabs';
import TabItem from '@theme/TabItem';

:::caution
🚧 **The framework does not yet have a public release, and the docs are still in construction. It is published only for showcase and feedback purposes, a first release will happen soon&trade;**.
:::

## Prerequisites

* JDK 11 or above. You can download the OpenJDK [here](https://adoptopenjdk.net/?variant=openjdk11&jvmVariant=hotspot)
* Apache Maven 3, preferably the latest version available [here](https://maven.apache.org/download.cgi).

This documentation assumes you have decent knowledge of the Java programming language. Being familiar with Discord4J and reactive programming is recommended, but not required. The [Discord4J documentation](https://wiki.discord4j.com) provides great guides to get started with [reactive programming](https://wiki.discord4j.com/en/latest/Reactive-(Reactor)-Tutorial/) and [advanced Java features](https://wiki.discord4j.com/en/latest/Lambda-Tutorial/).

## From the Maven archetype

The recommended way to start a project with Botrino is to use the Maven archetype (replace `[VERSION]` with the latest version available): [![Maven Central](https://img.shields.io/maven-central/v/com.alex1304.botrino/botrino)](https://search.maven.org/artifact/com.alex1304.botrino/botrino)

```
mvn archetype:generate -DarchetypeGroupId=com.alex1304.botrino -DarchetypeArtifactId=botrino-archetype -DarchetypeVersion=[VERSION]
```

You will be asked to enter the `groupId`, the `artifactId`, the `version` and the `package` of your project. If successful, it should generate a project with the following contents:

```
myproject
├── app
│   ├── pom.xml
│   └── src
│       └── main
│           ├── external-resources
│           │   ├── config.json
│           │   ├── launcher.cmd
│           │   └── logback.xml
│           ├── java
│           │   ├── com
│           │   │   └── example
│           │   │       └── myproject
│           │   │           ├── Main.java
│           │   │           ├── PingCommand.java
│           │   │           ├── SampleService.java
│           │   │           └── Strings.java
│           │   └── module-info.java
│           └── resources
│               └── AppStrings.properties
├── delivery
│   └── pom.xml
├── launcher
│   ├── pom.xml
│   └── src
│       └── main
│           └── java
│               ├── com
│               │   └── example
│               │       └── myproject
│               │           └── Launcher.java
│               └── module-info.java
└── pom.xml
```

* The `app/` directory corresponds to the main module of your bot application. It already contains pre-generated classes with a main method, an example command and an example service. It also shows how to externalize strings via a `.properties` files in the root of `src/main/resources`, and a class `Strings` containing constants to reference them. The `src/main/external-resources` directory contains the configuration files necessary to run the bot.
* The `delivery/` directory only contains a `pom.xml` that is capable of generating a runtime image of the bot application using the `jlink` utility, bundled with the JDK 11.
* The `launcher` directory contains the module used by `delivery` to create a basic launcher for the runtime image.
* The `pom.xml` which configures the project by importing the libraries and configuring the multi-module build.

Note that the archetype will automatically include the [command module](command-module/configuration.md) in your project dependencies. If you do not want to use the command module and use your own instead, you can remove the Maven dependency to `botrino-command` in both the root `pom.xml` and `app/pom.xml`, remove the `"command"` object from `app/src/main/external-resources/config.json`, and remove the `requires botrino.command;` line from `app/module-info.java`.

This project is ready to be opened in your favorite IDE (Eclipse, IntelliJ...), and you can directly jump to the [Running your bot](#running-your-bot) section.

## From a blank project

If you don't want the JLink runtime image, or if you want to use a build tool other than Maven, you may as well start from a blank project and import Botrino yourself. Be aware that it will require a bit more effort to set up than using the archetype.

Import the following dependencies (if you don't want the command module you can omit `botrino-command`):

<Tabs
    groupId="build-tools"
    defaultValue="maven"
    values={[
        {label: 'Maven', value: 'maven'},
        {label: 'Gradle', value: 'gradle'},
    ]}>
<TabItem value="maven">

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

</TabItem>
<TabItem value="gradle">

```groovy
repositories {
    mavenCentral()
}

dependencies {
    implementation 'com.alex1304.botrino-api:[VERSION]'
    implementation 'com.alex1304.botrino-command:[VERSION]'
}
```

</TabItem>
</Tabs>

As usual, replace `[VERSION]` with the latest version available: [![Maven Central](https://img.shields.io/maven-central/v/com.alex1304.botrino/botrino)](https://search.maven.org/artifact/com.alex1304.botrino/botrino)

Create a `module-info.java` annotated with `@BotModule`, with the `open` modifier and that requires the Botrino API module:

```java
import botrino.api.annotation.BotModule;

@BotModule
open module your.app {

    requires botrino.api;
    requires botrino.command;
}
```

The module `botrino.api` transitively exposes all libraries necessary to work, including Discord4J, Reactor, Netty, RDI and Jackson, so you don't need to put `requires` for those libraries.

:::caution
If you get compilation errors, remember to configure your project to use JDK 11 or above.
:::

Finally, add a class with a `main` method:

```java
package your.app;

import botrino.api.Botrino;

public final class Main {

    public static void main(String[] args) {
        Botrino.run(args);
    }
}
```

## Running your bot

### During development

When you are developing your bot, you may prefer to run the bot directly in your IDE rather than package your application every time.

If you used the archetype, copy the contents of `app/src/main/external-resources` in a new directory on your hard drive, **outside of the project workspace**. If you aren't using the archetype, create a directory outside of your project and add a `config.json` file with the following contents (insert your bot token in the `"token"` field, and remove the `"command"` entry if you aren't using the command module):

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
    },
    "command": {
        "prefix": "!"
    }
}
```

Use the tabs below depending on whether you use Eclipse or IntelliJ. If you use another IDE, it should be similar enough so you can figure out by yourself.

<Tabs
    groupId="ide"
    defaultValue="intellij"
    values={[
        {label: 'IntelliJ', value: 'intellij'},
        {label: 'Eclipse', value: 'eclipse'},
    ]}>
<TabItem value="intellij">

1. Open `Run` > `Edit Configurations...`

2. If you are using the archetype, it should detect a run configuration called "Main" automatically. If so, jump to step 7, otherwise continue

3. Click `+` then `Application`

4. Select Java 11 (or whatever JDK 11+ you have installed)

5. In the "Main class" field, enter the fully qualified name of the class containing the main method

6. In the "VM options" field, copy and paste the following: `--add-modules=ALL-MODULE-PATH -cp .`

7. In the "Working directory" field, enter the absolute path (or click the folder icon to browse) to the directory where you copied/created the configuration files earlier

<img src={useBaseUrl('img/intellij.png')} alt="" />

8. Click "OK" and run

</TabItem>
<TabItem value="eclipse">

1. Open `Run` > `Run Configurations...`

2. Right click `Java Application` then click `New Configuration`

3. In the "Project" field, select your project containing the main class

4. In the "Main class" field, enter the fully qualified name of the class containing the main method

<img src={useBaseUrl('img/eclipse1.png')} alt="" />

5. Go to the "Dependencies" tab, highlight "Classpath Entries", then click "Advanced...", select "Add External Folder", "OK", and browse to the directory where you copied/created the configuration files earlier

6. Still in the "Dependencies" tab, find the "Add modules" dropdown and select `ALL-MODULE-PATH`

<img src={useBaseUrl('img/eclipse2.png')} alt="" />

7. Click "Apply" then "Run"

</TabItem>
</Tabs>

### In a production environment

If you aren't using the archetype, you would need to configure yourself the packaging for the production environment, including scripts to launch the bot with the correct VM arguments, etc, just like any other Java application. If you are using the archetype, you can build the JLink runtime image with the following command:

```
mvn package -Dtoken=<BOT_TOKEN>
```

The bot token property is not required, but saves you from manually editing the json file to insert the token later on. This command will produce a `.zip` file found in `delivery/target` directory. You can unzip it in your production environment, and just run `./bin/<launcher name>`. The launcher will find a file named `launcher.cmd` in the working directory from which the command is launched. If you run the launcher from a different directory, you can specify the location to `launcher.cmd` in the arguments:

```
./bin/<launcher name> /path/to/launcher/cmd
```
`<launcher name>` by default corresponds to the `artifactId` of your project.

:::info
Running the bot via the launcher will start the process in the background, so it is not attached to the current command line window. The background process' working directory will be set to the location of the `launcher.cmd` file. If you want to run the bot itself directly within the command line window, you can just execute the command line found in the `launcher.cmd` file directly.
:::
