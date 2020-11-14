package botrino.command;

import botrino.command.doc.CommandDocumentation;
import botrino.command.privilege.Privilege;
import botrino.command.privilege.Privileges;
import reactor.core.publisher.Mono;

import java.util.Locale;
import java.util.Set;

/**
 * Represents a bot command.
 */
public interface Command {

    /**
     * Gets the aliases for this command.
     *
     * @return the set of aliases, must not be empty
     */
    Set<String> getAliases();

    /**
     * Defines the action of the command.
     *
     * @param ctx the context
     * @return a Mono that completes empty when the command is successful, and emits an error when something goes wrong.
     */
    Mono<Void> run(CommandContext ctx);

    /**
     * Gets the documentation of the command.
     *
     * @param locale the locale indicating the language of the documentation
     * @return the documentation
     */
    default CommandDocumentation getDocumentation(Locale locale) {
        return CommandDocumentation.empty();
    }

    /**
     * Gets the privilege that must be granted for a user to execute this command.
     *
     * @return the privilege
     */
    default Privilege getPrivilege() {
        return Privileges.allowed();
    }

    /**
     * Gets the scope of this command.
     *
     * @return the scope
     */
    default Scope getScope() {
        return Scope.ANYWHERE;
    }

    /**
     * Gets the subcommands for this command.
     *
     * @return the subcommands, may be empty
     */
    default Set<Command> getSubcommands() {
        return Set.of();
    }

    /**
     * Gets whether the command should be allowed to use by other bots. In most cases this should be false, which is
     * by default.
     *
     * @return whether the command should be allowed to use by other bots
     */
    default boolean allowUseByBots() {
        return false;
    }
}
