#set( $symbol_pound = '#' )
#set( $symbol_dollar = '$' )
#set( $symbol_escape = '\' )
import botrino.api.annotation.BotModule;

@BotModule
open module ${package} {

    requires botrino.api;
    requires botrino.command;
}