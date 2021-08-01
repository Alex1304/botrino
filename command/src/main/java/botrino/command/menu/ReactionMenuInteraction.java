package botrino.command.menu;

import botrino.command.CommandContext;
import discord4j.core.object.entity.Message;
import reactor.core.publisher.Sinks;

import java.util.concurrent.ConcurrentHashMap;

/**
 * Represents a user interaction to a reaction-based menu item.
 */
public final class ReactionMenuInteraction extends MenuInteraction {

    private final ReactionToggleEvent event;

    ReactionMenuInteraction(CommandContext originalCommandContext, Message menuMessage,
                            ConcurrentHashMap<String, Object> contextVariables,
                            Sinks.One<InteractiveMenu.MenuTermination> closeNotifier, ReactionToggleEvent event) {
        super(originalCommandContext, menuMessage, contextVariables, closeNotifier);
        this.event = event;
    }

    /**
     * Gets the event corresponding to this interaction.
     *
     * @return the event
     */
    public ReactionToggleEvent getEvent() {
        return event;
    }
}