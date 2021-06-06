#set( $symbol_pound = '#' )
#set( $symbol_dollar = '$' )
#set( $symbol_escape = '\' )
package ${package};

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;
import java.util.stream.Collectors;

public class Launcher {

    public static void main(String[] args) throws IOException, InterruptedException {
        var argsList = Arrays.asList(args);
        if (argsList.contains("--help")) {
            System.out.println("Usage: ${rootArtifactId} [--help] [--detached] [--batch-mode]");
            System.out.println("All flags are optional.");
            System.out.println("${symbol_escape}t--help${symbol_escape}t${symbol_escape}t${symbol_escape}tDisplays this help message and exits immediately.");
            System.out.println("${symbol_escape}t--detached${symbol_escape}t${symbol_escape}tRuns the bot in the background and detaches the process from the " +
                    "current console window.");
            System.out.println("${symbol_escape}t--batch-mode${symbol_escape}t${symbol_escape}tDo not prompt to press Enter to exit the program.");
            return;
        }
        var javaHome = Path.of(System.getProperty("java.home"));
        var processBuilder = new ProcessBuilder();
        processBuilder.directory(javaHome.toFile());
        var command = processBuilder.command();
        command.add(javaHome.resolve(Path.of("bin", "java")).toString());
        var jvmArgsTxt = javaHome.resolve("jvmArgs.txt");
        List<String> jvmArgs;
        if (Files.exists(jvmArgsTxt) && !(jvmArgs = Files.lines(jvmArgsTxt)
                .filter(line -> !line.startsWith("${symbol_pound}"))
                .collect(Collectors.toList())).isEmpty()) {
            command.addAll(jvmArgs);
        }
        command.add("-p");
        command.add("modules");
        command.add("-cp");
        command.add(".");
        command.add("--add-modules=ALL-MODULE-PATH");
        command.add("-m");
        command.add("${package}/${package}.Main");
        System.out.println(String.join(" ", command));
        if (argsList.contains("--detached")) {
            var process = processBuilder.start();
            System.out.println("The bot has been started and is running in the background. PID: " + process.pid());
        } else {
            processBuilder.inheritIO().start().waitFor();
        }
        if (!argsList.contains("--batch-mode")) {
            System.out.println("Press Enter to exit...");
            new Scanner(System.in).nextLine();
        }
    }
}
