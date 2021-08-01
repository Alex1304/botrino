package botrino.command.menu;

import botrino.command.CommandContext;
import discord4j.core.object.entity.Message;
import reactor.core.publisher.Sinks;
import reactor.util.annotation.Nullable;

import java.util.NoSuchElementException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.UnaryOperator;

import static java.util.Objects.requireNonNull;

abstract class MenuInteraction {

    private final CommandContext originalCommandContext;
    private final ConcurrentHashMap<String, Object> contextVariables;
    private final Message menuMessage;
    private final Sinks.One<InteractiveMenu.MenuTermination> closeNotifier;

    MenuInteraction(CommandContext originalCommandContext, Message menuMessage,
                    ConcurrentHashMap<String, Object> contextVariables,
                    Sinks.One<InteractiveMenu.MenuTermination> closeNotifier) {
        this.originalCommandContext = originalCommandContext;
        this.menuMessage = menuMessage;
        this.closeNotifier = closeNotifier;
        this.contextVariables = contextVariables;
    }

    /**
     * Updates a variable in the context of this interaction.
     *
     * @param <T>          the type of value
     * @param varName      the variable name
     * @param valueUpdater the function that updates the value. The function may return <code>null</code> to signal that
     *                     the variable should be unset
     * @param defaultValue the value to take if the variable is not set
     * @return the value after update
     * @throws ClassCastException if the value isn't of type T
     */
    @SuppressWarnings("unchecked")
    public <T> T update(String varName, UnaryOperator<T> valueUpdater, T defaultValue) {
        requireNonNull(varName, "varName");
        requireNonNull(valueUpdater, "valueUpdater");
        requireNonNull(defaultValue, "defaultValue");
        return (T) contextVariables.compute(varName, (k, v) -> {
            if (v == null) {
                return defaultValue;
            }
            return valueUpdater.apply((T) v);
        });
    }

    /**
     * Sets a variable in the context of this interaction to the given value.
     *
     * @param <T>     the type of value
     * @param varName the variable name
     * @param value   the new value, or <code>null</code> to unset
     */
    public <T> void set(String varName, @Nullable T value) {
        requireNonNull(varName, "varName");
        if (value == null) {
            contextVariables.remove(varName);
        } else {
            contextVariables.put(varName, value);
        }
    }

    /**
     * Gets a variable in the context of this interaction.
     *
     * @param <T>     the expected type of the value
     * @param varName teh variable name
     * @return the value associated to the variable
     * @throws NoSuchElementException if no variable for the given name is set
     * @throws ClassCastException     if the value is not of type <code>T</code>
     */
    public <T> T get(String varName) {
        requireNonNull(varName, "varName");
        @SuppressWarnings("unchecked")
        T value = (T) contextVariables.get(varName);
        if (value == null) {
            throw new NoSuchElementException("Undeclared interaction variable " + varName);
        }
        return value;
    }

    /**
     * Gets the context of the command that originally created the interactive menu associated to this interaction.
     *
     * @return a {@link CommandContext}
     */
    public CommandContext getOriginalCommandContext() {
        return originalCommandContext;
    }

    /**
     * Gets the menu message prompting the user for interaction.
     *
     * @return the Discord message of the menu
     */
    public Message getMenuMessage() {
        return menuMessage;
    }

    /**
     * Closes the menu. No more interactions will be registered once this call returns.
     */
    public void closeMenu() {
        closeNotifier.tryEmitValue(InteractiveMenu.MenuTermination.COMPLETE);
    }
}