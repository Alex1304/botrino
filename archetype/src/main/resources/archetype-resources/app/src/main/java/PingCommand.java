#set( $symbol_pound = '#' )
#set( $symbol_dollar = '$' )
#set( $symbol_escape = '\' )
package ${package};

import botrino.command.Command;
import botrino.command.CommandContext;
import reactor.core.publisher.Mono;

import java.util.Set;

public final class PingCommand implements Command {

    @Override
    public Set<String> aliases() {
        return Set.of("ping");
    }

    @Override
    public Mono<Void> run(CommandContext ctx) {
        return ctx.channel().createMessage("Pong! :ping_pong:").then();
    }
}
