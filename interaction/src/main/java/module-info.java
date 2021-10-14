import botrino.api.extension.BotrinoExtension;
import botrino.interaction.InteractionExtension;

module botrino.interaction {
    exports botrino.interaction;
    exports botrino.interaction.annotation;
    exports botrino.interaction.config;
    exports botrino.interaction.context;
    exports botrino.interaction.cooldown;
    exports botrino.interaction.grammar;
    exports botrino.interaction.listener;
    exports botrino.interaction.privilege;
    exports botrino.interaction.util;

    opens botrino.interaction;
    opens botrino.interaction.annotation;
    opens botrino.interaction.config;
    opens botrino.interaction.context;
    opens botrino.interaction.listener;

    provides BotrinoExtension with InteractionExtension;

    requires transitive botrino.api;
    requires com.github.benmanes.caffeine;

    requires static org.immutables.value;
}