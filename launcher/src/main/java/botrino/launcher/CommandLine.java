package botrino.launcher;

import java.util.*;
import java.util.Map.Entry;

/**
 * Represents the command line, split between arguments and options.
 */
public class CommandLine {

    private final ArrayList<String> argList;
    private final HashMap<String, String> options;

    private CommandLine(ArrayList<String> argList, HashMap<String, String> options) {
        this.argList = argList;
        this.options = options;
    }

    /**
     * Parses the program arguments into a {@link CommandLine} object. Identifies the arguments and the options used.
     *
     * @param args             the arguments of the program
     * @param availableOptions a Map containing the available options. Boolean for each option tells whether the option
     *                         expects a value or not
     * @return a {@link CommandLine} instance
     */
    public static CommandLine parse(String[] args, Map<String, Boolean> availableOptions) {
        var argList = new ArrayList<String>();
        var options = new HashMap<String, String>();
        Entry<String, Boolean> optionBeingRead = null;
        for (var arg : args) {
            Boolean isAnOptionThatExpectsValue = availableOptions.get(arg);
            // null = not an option
            // false = option that doesn't expect value
            // true = option that expects value
            if (isAnOptionThatExpectsValue != null) {
                if (optionBeingRead != null) {
                    if (!optionBeingRead.getValue()) {
                        options.put(optionBeingRead.getKey(), "");
                    } else {
                        throw new IllegalArgumentException(optionBeingRead.getKey() + " option expects a value");
                    }
                }
                optionBeingRead = Map.entry(arg, isAnOptionThatExpectsValue);
            } else if (arg.startsWith("-")) { // If it isn't an option, it shouldn't start with "-"
                throw new IllegalArgumentException("Unknown option " + arg);
            } else if (optionBeingRead != null && optionBeingRead.getValue()) { // In this case we are reading the
            	// value of an option
                options.put(optionBeingRead.getKey(), arg);
                optionBeingRead = null;
            } else { // In all other cases it's a regular argument
                argList.add(arg);
            }
        }
        if (optionBeingRead != null) {
            if (!optionBeingRead.getValue()) {
                options.put(optionBeingRead.getKey(), "");
            } else {
                throw new IllegalArgumentException(optionBeingRead.getKey() + " option expects a value");
            }
        }
        return new CommandLine(argList, options);
    }

    /**
     * Gets an unmodifiable list of the command line arguments.
     *
     * @return a {@link List}
     */
    public List<String> getArguments() {
        return Collections.unmodifiableList(argList);
    }

    /**
     * Gets the value of an option identified by its name, if present.
     *
     * @param optionName the name of the option to look up
     * @return an Optional containing the value of the option if present
     */
    public Optional<String> getOption(String optionName) {
        return Optional.ofNullable(options.get(optionName));
    }
}
