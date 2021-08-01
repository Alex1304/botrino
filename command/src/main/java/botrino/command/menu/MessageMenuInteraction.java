package botrino.command.menu;

import botrino.command.CommandContext;
import botrino.command.TokenizedInput;
import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.object.entity.Message;
import reactor.core.publisher.Sinks;

import java.util.concurrent.ConcurrentHashMap;

/**
 * Represents a user interaction to a message-based menu item.
 */
public final class MessageMenuInteraction extends MenuInteraction {

    private final MessageCreateEvent event;
    private final TokenizedInput input;

    MessageMenuInteraction(CommandContext originalCommandContext, Message menuMessage,
                           ConcurrentHashMap<String, Object> contextVariables,
                           Sinks.One<InteractiveMenu.MenuTermination> closeNotifier, MessageCreateEvent event,
                           TokenizedInput input) {
        super(originalCommandContext, menuMessage, contextVariables, closeNotifier);
        this.event = event;
        this.input = input;
    }

    /**
     * Gets the event corresponding to this interaction.
     *
     * @return the event
     */
    public MessageCreateEvent getEvent() {
        return event;
    }

    /**
     * Gets the original input of the user.
     *
     * @return the input
     */
    public TokenizedInput getInput() {
        return input;
    }
}