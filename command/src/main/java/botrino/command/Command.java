package botrino.command;

import botrino.api.i18n.Translator;
import botrino.command.annotation.Alias;
import botrino.command.cooldown.Cooldown;
import botrino.command.doc.CommandDocumentation;
import botrino.command.privilege.Privilege;
import botrino.command.privilege.Privileges;
import reactor.core.publisher.Mono;
import reactor.util.annotation.Nullable;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
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
     * Initializes a command builder with the given alias and action function.
     *
     * @param alias  the alias of the command
     * @param action the action to execute when the command is ran
     * @return a new builder
     */
    static Builder builder(String alias, Function<? super CommandContext, ? extends Mono<Void>> action) {
        return new Builder(Set.of(alias), action);
    }

    /**
     * Creates a command with the given aliases and action function, with default settings (empty documentation,
     * unrestricted privilege and scope, no subcommand, no cooldown, no error handler). Use {@link #builder(Set,
     * Function)} to customize those settings.
     *
     * @param aliases the aliases of the command
     * @param action  the action to execute when the command is ran
     * @return a new {@link Command}
     */
    static Command of(Set<String> aliases, Function<? super CommandContext, ? extends Mono<Void>> action) {
        return builder(aliases, action).build();
    }

    /**
     * Creates a command with the given alias and action function, with default settings (empty documentation,
     * unrestricted privilege and scope, no subcommand, no cooldown, no error handler). Use {@link #builder(Set,
     * Function)} to customize those settings.
     *
     * @param alias  the alias of the command
     * @param action the action to execute when the command is ran
     * @return a new {@link Command}
     */
    static Command of(String alias, Function<? super CommandContext, ? extends Mono<Void>> action) {
        return builder(alias, action).build();
    }

    /**
     * Defines the action of the command.
     *
     * @param ctx the context
     * @return a Mono that completes empty when the command is successful, and emits an error when something goes wrong.
     */
    Mono<Void> run(CommandContext ctx);

    /**
     * Defines the aliases for this command.
     *
     * @return the set of aliases. If empty, the command will not be registered.
     */
    default Set<String> aliases() {
        var topLevelAnnot = getClass().getAnnotation(Alias.class);
        if (topLevelAnnot != null) {
            return Set.of(topLevelAnnot.value());
        }
        return Set.of();
    }

    /**
     * Defines the documentation of the command.
     *
     * @param translator a translator that can be used to translate the documentation
     * @return the documentation
     */
    default CommandDocumentation documentation(Translator translator) {
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
     * Defines the error handler for this command. It overrides the global command error handler if it exists.
     *
     * @return the error handler for this command
     */
    default CommandErrorHandler errorHandler() {
        return CommandErrorHandler.NO_OP;
    }

    /**
     * Defines the cooldown of the command on a per user basis. In other words, the number of times a user can execute
     * this command within a certain timeframe.
     *
     * @return the cooldown
     */
    default Cooldown cooldown() {
        return Cooldown.none();
    }

    final class Builder {

        private final Set<String> aliases;
        private final Function<? super CommandContext, ? extends Mono<Void>> action;
        private final Set<Command> subcommands = new HashSet<>();
        private Function<? super Translator, CommandDocumentation> documentation;
        private Privilege privilege;
        private Scope scope;
        private CommandErrorHandler errorHandler;
        private Cooldown cooldown;

        private Builder(Set<String> aliases, Function<? super CommandContext, ? extends Mono<Void>> action) {
            this.aliases = aliases;
            this.action = action;
        }

        /**
         * Inherit properties of the other command, such as privilege, scope, error handler and cooldown. It won't
         * inherit aliases, action, documentation and subcommands.
         *
         * @param other the other command to inherit from
         * @return this builder
         */
        public Builder inheritFrom(Command other) {
            Objects.requireNonNull(other);
            return setPrivilege(other.privilege())
                    .setScope(other.scope())
                    .setErrorHandler(other.errorHandler())
                    .setCooldown(other.cooldown());
        }

        /**
         * Defines the documentation of the command.
         *
         * @param documentation a function that provides the {@link CommandDocumentation} object possibly translated, or
         *                      null to use default value (empty documentation)
         * @return this builder
         */
        public Builder setDocumentation(@Nullable Function<? super Translator, CommandDocumentation> documentation) {
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
         * Defines the cooldown for this command.
         *
         * @param cooldown the cooldown for this command, or null to use default value ({@link Cooldown#none()})
         * @return this builder
         */
        public Builder setCooldown(@Nullable Cooldown cooldown) {
            this.cooldown = cooldown;
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
                public CommandDocumentation documentation(Translator translator) {
                    return documentation == null ? Command.super.documentation(translator) :
                            documentation.apply(translator);
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
                public CommandErrorHandler errorHandler() {
                    return errorHandler == null ? Command.super.errorHandler() : errorHandler;
                }

                @Override
                public Cooldown cooldown() {
                    return cooldown == null ? Command.super.cooldown() : cooldown;
                }

                @Override
                public String toString() {
                    return "Command{aliases=" + aliases + "}";
                }
            };
        }
    }
}
