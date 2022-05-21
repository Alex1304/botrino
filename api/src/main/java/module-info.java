import botrino.api.extension.BotrinoExtension;

module botrino.api {

    exports botrino.api;
    exports botrino.api.annotation;
    exports botrino.api.config;
    exports botrino.api.config.object;
    exports botrino.api.extension;
    exports botrino.api.i18n;
    exports botrino.api.util;

    opens botrino.api.config.object;

    requires static org.immutables.value;

    requires transitive com.google.errorprone.annotations;
    requires transitive discord4j.common;
    requires transitive discord4j.core;
    requires transitive discord4j.discordjson;
    requires transitive discord4j.discordjson.api;
    requires transitive discord4j.gateway;
    requires transitive discord4j.rest;
    requires transitive com.fasterxml.jackson.annotation;
    requires transitive com.fasterxml.jackson.core;
    requires transitive com.fasterxml.jackson.databind;
    requires transitive com.fasterxml.jackson.datatype.jdk8;
    requires transitive java.logging;
    requires transitive org.reactivestreams;
    requires transitive rdi;
    requires transitive reactor.core;
    requires transitive reactor.extra;

    uses BotrinoExtension;
}