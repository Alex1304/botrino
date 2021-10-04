#set( $symbol_pound = '#' )
#set( $symbol_dollar = '$' )
#set( $symbol_escape = '\' )
package ${package};

import botrino.interaction.ChatInputCommand;
import botrino.interaction.ChatInputInteractionListener;
import botrino.interaction.context.ChatInputInteractionContext;
import org.reactivestreams.Publisher;

@ChatInputCommand(name = "ping", description = "Pings the bot to check if it is alive.")
public final class PingCommand implements ChatInputInteractionListener {

    @Override
    public Publisher<?> run(ChatInputInteractionContext ctx) {
        return ctx.event().reply(ctx.translate(Strings.APP, "pong")).withEphemeral(true);
    }
}
