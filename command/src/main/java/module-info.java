import botrino.api.extension.BotrinoExtension;
import botrino.command.CommandExtension;

module botrino.command {
    exports botrino.command;
    exports botrino.command.annotation;
    exports botrino.command.config;
    exports botrino.command.context;
    exports botrino.command.cooldown;
    exports botrino.command.grammar;
    exports botrino.command.privilege;

    opens botrino.command;
    opens botrino.command.config;
    opens botrino.command.context;

    provides BotrinoExtension with CommandExtension;

    requires botrino.api;

    requires static org.immutables.value;
}