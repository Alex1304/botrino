package botrino.command.menu;

import discord4j.common.util.Snowflake;
import discord4j.core.event.domain.message.ReactionAddEvent;
import discord4j.core.event.domain.message.ReactionRemoveEvent;
import discord4j.core.object.entity.Guild;
import discord4j.core.object.entity.Message;
import discord4j.core.object.entity.User;
import discord4j.core.object.entity.channel.MessageChannel;
import discord4j.core.object.reaction.ReactionEmoji;
import reactor.core.publisher.Mono;
import reactor.util.annotation.Nullable;

import java.util.Optional;
import java.util.function.Function;

public final class ReactionToggleEvent {

	private final ReactionAddEvent addEvent;
	private final ReactionRemoveEvent removeEvent;

    private ReactionToggleEvent(@Nullable ReactionAddEvent addEvent, @Nullable ReactionRemoveEvent removeEvent) {
        this.addEvent = addEvent;
        this.removeEvent = removeEvent;
    }

    public static ReactionToggleEvent add(ReactionAddEvent event) {
        return new ReactionToggleEvent(event, null);
    }

    public static ReactionToggleEvent remove(ReactionRemoveEvent event) {
        return new ReactionToggleEvent(null, event);
    }

    private <T> T takeEither(Function<ReactionAddEvent, T> fromAddEvent, Function<ReactionRemoveEvent, T> fromRemoveEvent) {
        if (addEvent != null) {
            return fromAddEvent.apply(addEvent);
        }
        if (removeEvent != null) {
            return fromRemoveEvent.apply(removeEvent);
        }
        throw new AssertionError();
	}
	
    public Snowflake getUserId() {
        return takeEither(ReactionAddEvent::getUserId, ReactionRemoveEvent::getUserId);
    }

    public Mono<User> getUser() {
        return takeEither(ReactionAddEvent::getUser, ReactionRemoveEvent::getUser);
    }

    public Snowflake getChannelId() {
        return takeEither(ReactionAddEvent::getChannelId, ReactionRemoveEvent::getChannelId);
    }

    public Mono<MessageChannel> getChannel() {
        return takeEither(ReactionAddEvent::getChannel, ReactionRemoveEvent::getChannel);
    }

    public Snowflake getMessageId() {
        return takeEither(ReactionAddEvent::getMessageId, ReactionRemoveEvent::getMessageId);
    }

    public Mono<Message> getMessage() {
        return takeEither(ReactionAddEvent::getMessage, ReactionRemoveEvent::getMessage);
    }

    public Optional<Snowflake> getGuildId() {
        return takeEither(ReactionAddEvent::getGuildId, ReactionRemoveEvent::getGuildId);
    }

    public Mono<Guild> getGuild() {
        return takeEither(ReactionAddEvent::getGuild, ReactionRemoveEvent::getGuild);
    }

    public ReactionEmoji getEmoji() {
        return takeEither(ReactionAddEvent::getEmoji, ReactionRemoveEvent::getEmoji);
    }
    
    public boolean isAddEvent() {
    	return addEvent != null;
    }
}
