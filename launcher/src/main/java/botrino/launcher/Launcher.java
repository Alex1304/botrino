package botrino.launcher;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Map;
import java.util.Optional;

import static java.util.stream.Collectors.toUnmodifiableList;

public class Launcher {

    private static final String JAR_DIR = "jar";
    private static final String BOTS_DIR = "bots";
    private static final String RUNTIME_DIR = "runtime";

    public static void main(String[] args) throws IOException {
        var commandLine = CommandLine.parse(args, Map.of("home", true));
        if (commandLine.getArguments().size() < 1) {
            throwUsage();
        }
        var botrinoHome = Path.of(commandLine.getOption("home")
                .or(() -> Optional.ofNullable(System.getenv("BOTRINO_HOME")))
                .orElse("."));
        switch (commandLine.getArguments().get(0)) {
            case "start":
                handleStart(botrinoHome, commandLine);
                break;
            default:
                throwUsage();
        }

    }

    private static void handleStart(Path botrinoHome, CommandLine commandLine) throws IOException {
        if (commandLine.getArguments().size() < 2) {
            throwStartUsage();
        }
        var botDir = botrinoHome.resolve(BOTS_DIR).resolve(commandLine.getArguments().get(1));
        var jarDir = botDir.resolve(JAR_DIR);
        var runtimeDir = botrinoHome.resolve(RUNTIME_DIR);
        var resolvedJavaHome = Path.of(System.getProperty("java.home"))
                .resolve(Path.of("bin", "java"))
                .toString();
        var modulePath = buildModulePath(jarDir, runtimeDir);
        var processBuilder = new ProcessBuilder();
        var command = processBuilder.command();
        command.add(resolvedJavaHome);
        if (!modulePath.isEmpty()) {
            command.add("--module-path");
            command.add(modulePath);
        }
        command.add("-cp");
        command.add(botDir.toString());
        command.add("-m");
        command.add("botrino.runtime/botrino.runtime.Start");
        command.add(botDir.toString());
        System.out.println(String.join(" ", command));
        var process = processBuilder.start();
        System.out.println("Bot started (PID: " + process.pid() + ")");
    }

    private static String buildModulePath(Path jarDir, Path runtimeDir) throws IOException {
        var paths = new ArrayList<String>();
        try (var jarStream = Files.list(jarDir); var runtimeStream = Files.list(runtimeDir)) {
            paths.addAll(jarStream.map(Path::toString)
                    .collect(toUnmodifiableList()));
            paths.addAll(runtimeStream.map(Path::toString)
                    .collect(toUnmodifiableList()));
        }
        return String.join(":", paths);
    }

    private static void throwUsage() {
        throw new IllegalArgumentException("Usage: botrino [-home <botrino_home>] <action>");
    }

    private static void throwStartUsage() {
        throw new IllegalArgumentException("Usage: botrino start <bot_dir>");
    }
}
