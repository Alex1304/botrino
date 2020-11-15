import botrino.api.extension.BotrinoExtension;

module botrino.api {

    exports botrino.api;
    exports botrino.api.annotation;
    exports botrino.api.config;
    exports botrino.api.config.bot;
    exports botrino.api.config.i18n;
    exports botrino.api.extension;
    exports botrino.api.i18n;
    exports botrino.api.util;

    opens botrino.api.config.bot;
    opens botrino.api.config.i18n;

    requires org.apache.commons.lang3;

    requires transitive com.google.gson;
    requires transitive discord4j.common;
    requires transitive discord4j.core;
    requires transitive discord4j.discordjson;
    requires transitive discord4j.discordjson.api;
    requires transitive discord4j.gateway;
    requires transitive discord4j.rest;
    requires transitive discord4j.voice;
    requires transitive java.logging;
    requires transitive org.reactivestreams;
    requires transitive rdi;
    requires transitive reactor.core;
    requires transitive reactor.extra;
    requires transitive reactor.netty.core;
    requires transitive reactor.netty.http;

    uses BotrinoExtension;
}