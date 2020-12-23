#set( $symbol_pound = '#' )
#set( $symbol_dollar = '$' )
#set( $symbol_escape = '\' )
package ${package};

import botrino.command.Command;
import botrino.command.CommandContext;
import botrino.command.annotation.Alias;
import botrino.command.annotation.TopLevelCommand;
import reactor.core.publisher.Mono;

import java.util.Set;

@TopLevelCommand
@Alias("ping")
public final class PingCommand implements Command {

    @Override
    public Mono<Void> run(CommandContext ctx) {
        return ctx.channel().createMessage(ctx.translate(Strings.APP, "pong")).then();
    }
}
