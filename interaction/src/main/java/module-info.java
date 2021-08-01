import botrino.api.extension.BotrinoExtension;
import botrino.interaction.InteractionExtension;

module botrino.interaction {
    exports botrino.interaction;
    exports botrino.interaction.config;
    exports botrino.interaction.context;
    exports botrino.interaction.cooldown;
    exports botrino.interaction.grammar;
    exports botrino.interaction.privilege;

    opens botrino.interaction;
    opens botrino.interaction.config;
    opens botrino.interaction.context;

    provides BotrinoExtension with InteractionExtension;

    requires botrino.api;

    requires static org.immutables.value;
}