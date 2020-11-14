import botrino.api.extension.BotrinoExtension;
import botrino.command.CommandExtension;

module botrino.command {
    exports botrino.command;
    exports botrino.command.config;
    exports botrino.command.doc;
    exports botrino.command.privilege;

    opens botrino.command;
    opens botrino.command.config;

    provides BotrinoExtension with CommandExtension;

    requires botrino.api;
}