package botrino.command;

import botrino.command.doc.CommandDocumentation;
import botrino.command.privilege.Privilege;
import botrino.command.privilege.Privileges;
import reactor.core.publisher.Mono;
import reactor.util.annotation.Nullable;

import java.util.*;
import java.util.function.Function;

/**
 * Represents a bot command.
 */
public interface Command {

    /**
     * Initializes a command builder with the given aliases and action function.
     *
     * @param aliases the aliases of the command
     * @param action  the action to execute when the command is ran
     * @return a new builder
     */
    static Builder builder(Set<String> aliases, Function<? super CommandContext, ? extends Mono<Void>> action) {
        return new Builder(aliases, action);
    }

    /**
     * Creates a command with the given aliases and action function, with default settings (empty documentation,
     * unrestricted privilege and scope, no subcommand, ignored by other bots). Use {@link #builder(Set, Function)} to
     * customize those settings.
     *
     * @param aliases the aliases of the command
     * @param action  the action to execute when the command is ran
     * @return a new {@link Command}
     */
    static Command of(Set<String> aliases, Function<? super CommandContext, ? extends Mono<Void>> action) {
        return builder(aliases, action).build();
    }

    /**
     * Defines the aliases for this command.
     *
     * @return the set of aliases, must not be empty
     */
    Set<String> aliases();

    /**
     * Defines the action of the command.
     *
     * @param ctx the context
     * @return a Mono that completes empty when the command is successful, and emits an error when something goes wrong.
     */
    Mono<Void> run(CommandContext ctx);

    /**
     * Defines the documentation of the command.
     *
     * @param locale the locale indicating the language of the documentation
     * @return the documentation
     */
    default CommandDocumentation documentation(Locale locale) {
        return CommandDocumentation.empty();
    }

    /**
     * Defines the privilege that must be granted for a user to execute this command.
     *
     * @return the privilege
     */
    default Privilege privilege() {
        return Privileges.allowed();
    }

    /**
     * Defines the scope of this command.
     *
     * @return the scope
     */
    default Scope scope() {
        return Scope.ANYWHERE;
    }

    /**
     * Defines the subcommands for this command.
     *
     * @return the subcommands, may be empty
     */
    default Set<Command> subcommands() {
        return Set.of();
    }

    /**
     * Whether the command should be ignored if used by other bots. In most cases this should be true, which is by
     * default.
     *
     * @return whether the command should be ignored by other bots
     */
    default boolean ignoreBots() {
        return true;
    }

    /**
     * Defines the error handler for this command. It overrides the global command error handler if it exists.
     *
     * @return the error handler for this command
     */
    default CommandErrorHandler errorHandler() {
        return CommandErrorHandler.NO_OP;
    }

    final class Builder {

        private final Set<String> aliases;
        private final Function<? super CommandContext, ? extends Mono<Void>> action;
        private final Set<Command> subcommands = new HashSet<>();
        private Function<? super Locale, CommandDocumentation> documentation;
        private Privilege privilege;
        private Scope scope;
        private boolean ignoreBots;
        private boolean ignoreBots_set;
        private CommandErrorHandler errorHandler;

        private Builder(Set<String> aliases, Function<? super CommandContext, ? extends Mono<Void>> action) {
            this.aliases = aliases;
            this.action = action;
        }

        /**
         * Inherit properties of the other command such as privilege, scope, ignore bots state, and error handler. It
         * won't inherit aliases, action, documentation and subcommands.
         *
         * @param other the other command to inherit from
         * @return this builder
         */
        public Builder inheritFrom(Command other) {
            Objects.requireNonNull(other);
            return setPrivilege(other.privilege())
                    .setScope(other.scope())
                    .setIgnoreBots(other.ignoreBots())
                    .setErrorHandler(other.errorHandler());
        }

        /**
         * Defines the documentation of the command.
         *
         * @param documentation a function that provides the {@link CommandDocumentation} object adapted to the locale,
         *                      or null to use default value (empty documentation)
         * @return this builder
         */
        public Builder setDocumentation(@Nullable Function<? super Locale, CommandDocumentation> documentation) {
            this.documentation = documentation;
            return this;
        }

        /**
         * Defines the privilege that must be granted for a user to execute this command.
         *
         * @param privilege the privilege to set, or null to use default value ({@link Privileges#allowed()})
         * @return this builder
         */
        public Builder setPrivilege(@Nullable Privilege privilege) {
            this.privilege = privilege;
            return this;
        }

        /**
         * Defines the scope of this command.
         *
         * @param scope the scope in which the command can be used, or null to use default value ({@link
         *              Scope#ANYWHERE})
         * @return this builder
         */
        public Builder setScope(@Nullable Scope scope) {
            this.scope = scope;
            return this;
        }

        /**
         * Adds a subcommand to this command.
         *
         * @param command the subcommand to add
         * @return this builder
         */
        public Builder addSubcommand(Command command) {
            Objects.requireNonNull(command);
            subcommands.add(command);
            return this;
        }

        /**
         * Whether the command should be ignored if used by other bots. In most cases this should be true, which is by
         * default.
         *
         * @param ignoreBots whether the command should ignore bots
         * @return this builder
         */
        public Builder setIgnoreBots(boolean ignoreBots) {
            this.ignoreBots = ignoreBots;
            this.ignoreBots_set = true;
            return this;
        }

        /**
         * Defines the error handler for this command. It overrides the global command error handler.
         *
         * @param errorHandler the error handler for this command, or null to use default value ({@link
         *                     CommandErrorHandler#NO_OP})
         * @return this builder
         */
        public Builder setErrorHandler(@Nullable CommandErrorHandler errorHandler) {
            this.errorHandler = errorHandler;
            return this;
        }

        /**
         * Creates a command based on the current state of this builder.
         *
         * @return a new {@link Command}
         */
        public Command build() {
            return new Command() {
                @Override
                public Set<String> aliases() {
                    return aliases;
                }

                @Override
                public Mono<Void> run(CommandContext ctx) {
                    return action.apply(ctx);
                }

                @Override
                public CommandDocumentation documentation(Locale locale) {
                    return documentation == null ? Command.super.documentation(locale) : documentation.apply(locale);
                }

                @Override
                public Privilege privilege() {
                    return privilege == null ? Command.super.privilege() : privilege;
                }

                @Override
                public Scope scope() {
                    return scope == null ? Command.super.scope() : scope;
                }

                @Override
                public Set<Command> subcommands() {
                    return subcommands;
                }

                @Override
                public boolean ignoreBots() {
                    return ignoreBots_set ? ignoreBots : Command.super.ignoreBots();
                }

                @Override
                public CommandErrorHandler errorHandler() {
                    return errorHandler == null ? Command.super.errorHandler() : errorHandler;
                }

                @Override
                public String toString() {
                    return "Command{aliases=" + aliases + "}";
                }
            };
        }
    }
}
