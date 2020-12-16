(window.webpackJsonp=window.webpackJsonp||[]).push([[7],{76:function(e,t,n){"use strict";n.r(t),n.d(t,"frontMatter",(function(){return a})),n.d(t,"metadata",(function(){return c})),n.d(t,"rightToc",(function(){return s})),n.d(t,"default",(function(){return d}));var o=n(3),i=n(8),r=(n(0),n(92)),a={title:"Customizing the Discord client"},c={unversionedId:"api/customizing-the-discord-client",id:"api/customizing-the-discord-client",isDocsHomePage:!1,title:"Customizing the Discord client",description:"When you start a project with Botrino, the Discord client is constructed for you, so you don't have anything to do in order to run the bot. However, in most cases, you will want to take full control on how the Discord client is created. This section will show you how to fully customize the Discord client and the gateway login process.",source:"@site/docs/api/customizing-the-discord-client.md",slug:"/api/customizing-the-discord-client",permalink:"/docs/api/customizing-the-discord-client",editUrl:"https://github.com/Alex1304/botrino/edit/main/website/docs/api/customizing-the-discord-client.md",version:"current",sidebar:"someSidebar",previous:{title:"Configuring your bot",permalink:"/docs/api/configuring-your-bot"},next:{title:"Extensions",permalink:"/docs/api/extensions"}},s=[{value:"The <code>LoginHandler</code> interface",id:"the-loginhandler-interface",children:[]}],l={rightToc:s};function d(e){var t=e.components,n=Object(i.a)(e,["components"]);return Object(r.b)("wrapper",Object(o.a)({},l,n,{components:t,mdxType:"MDXLayout"}),Object(r.b)("p",null,"When you start a project with Botrino, the Discord client is constructed for you, so you don't have anything to do in order to run the bot. However, in most cases, you will want to take full control on how the Discord client is created. This section will show you how to fully customize the Discord client and the gateway login process."),Object(r.b)("h2",{id:"the-loginhandler-interface"},"The ",Object(r.b)("inlineCode",{parentName:"h2"},"LoginHandler")," interface"),Object(r.b)("p",null,"All you need to do is to provide one implementation of the ",Object(r.b)("inlineCode",{parentName:"p"},"LoginHandler")," interface. It defines one method, ",Object(r.b)("inlineCode",{parentName:"p"},"Mono<GatewayDiscordClient> login(ConfigContainer configContainer)"),", that you can override to define yourself how your bot connects to the Discord gateway. The default implementation of this method builds the Discord client with default settings, using the token, presence status, and intents from the configuration. It can be recreated like this:"),Object(r.b)("pre",null,Object(r.b)("code",Object(o.a)({parentName:"pre"},{className:"language-java"}),"package com.example.myproject;\n\nimport botrino.api.config.object.BotConfig;\nimport discord4j.core.DiscordClient;\nimport discord4j.core.GatewayDiscordClient;\nimport discord4j.core.object.presence.Presence;\nimport discord4j.core.shard.MemberRequestFilter;\nimport discord4j.gateway.intent.IntentSet;\nimport reactor.core.publisher.Mono;\n\npublic final class DefaultLoginHandler implements LoginHandler {\n\n    @Override\n    public Mono<GatewayDiscordClient> login(ConfigContainer configContainer) {\n        var config = configContainer.get(BotConfig.class);\n        var discordClient = DiscordClient.create(config.token());\n        return discordClient.gateway()\n                .setInitialStatus(shard -> config.presence()\n                        .map(BotConfig.StatusConfig::toStatusUpdate)\n                        .orElseGet(Presence::online))\n                .setEnabledIntents(config.enabledIntents().stream().boxed()\n                        .map(IntentSet::of)\n                        .findAny()\n                        .orElseGet(IntentSet::nonPrivileged))\n                .setMemberRequestFilter(MemberRequestFilter.none())\n                .login()\n                .single();\n    }\n}\n")),Object(r.b)("div",{className:"admonition admonition-caution alert alert--warning"},Object(r.b)("div",Object(o.a)({parentName:"div"},{className:"admonition-heading"}),Object(r.b)("h5",{parentName:"div"},Object(r.b)("span",Object(o.a)({parentName:"h5"},{className:"admonition-icon"}),Object(r.b)("svg",Object(o.a)({parentName:"span"},{xmlns:"http://www.w3.org/2000/svg",width:"16",height:"16",viewBox:"0 0 16 16"}),Object(r.b)("path",Object(o.a)({parentName:"svg"},{fillRule:"evenodd",d:"M8.893 1.5c-.183-.31-.52-.5-.887-.5s-.703.19-.886.5L.138 13.499a.98.98 0 0 0 0 1.001c.193.31.53.501.886.501h13.964c.367 0 .704-.19.877-.5a1.03 1.03 0 0 0 .01-1.002L8.893 1.5zm.133 11.497H6.987v-2.003h2.039v2.003zm0-3.004H6.987V5.987h2.039v4.006z"})))),"caution")),Object(r.b)("div",Object(o.a)({parentName:"div"},{className:"admonition-content"}),Object(r.b)("ul",{parentName:"div"},Object(r.b)("li",{parentName:"ul"},"The implementation class must have a no-arg constructor."),Object(r.b)("li",{parentName:"ul"},"If more than one implementation of ",Object(r.b)("inlineCode",{parentName:"li"},"LoginHandler")," are found, it will result in an error as it is impossible to determine which one to use. If you don't want to remove the extra implementation(s), you can mark one of them with the ",Object(r.b)("inlineCode",{parentName:"li"},"@Primary")," annotation to lift the ambiguity. You may alternatively use the ",Object(r.b)("inlineCode",{parentName:"li"},"@Exclude")," annotation if you don't want one implementation to be picked up by Botrino.")))))}d.isMDXComponent=!0},92:function(e,t,n){"use strict";n.d(t,"a",(function(){return u})),n.d(t,"b",(function(){return b}));var o=n(0),i=n.n(o);function r(e,t,n){return t in e?Object.defineProperty(e,t,{value:n,enumerable:!0,configurable:!0,writable:!0}):e[t]=n,e}function a(e,t){var n=Object.keys(e);if(Object.getOwnPropertySymbols){var o=Object.getOwnPropertySymbols(e);t&&(o=o.filter((function(t){return Object.getOwnPropertyDescriptor(e,t).enumerable}))),n.push.apply(n,o)}return n}function c(e){for(var t=1;t<arguments.length;t++){var n=null!=arguments[t]?arguments[t]:{};t%2?a(Object(n),!0).forEach((function(t){r(e,t,n[t])})):Object.getOwnPropertyDescriptors?Object.defineProperties(e,Object.getOwnPropertyDescriptors(n)):a(Object(n)).forEach((function(t){Object.defineProperty(e,t,Object.getOwnPropertyDescriptor(n,t))}))}return e}function s(e,t){if(null==e)return{};var n,o,i=function(e,t){if(null==e)return{};var n,o,i={},r=Object.keys(e);for(o=0;o<r.length;o++)n=r[o],t.indexOf(n)>=0||(i[n]=e[n]);return i}(e,t);if(Object.getOwnPropertySymbols){var r=Object.getOwnPropertySymbols(e);for(o=0;o<r.length;o++)n=r[o],t.indexOf(n)>=0||Object.prototype.propertyIsEnumerable.call(e,n)&&(i[n]=e[n])}return i}var l=i.a.createContext({}),d=function(e){var t=i.a.useContext(l),n=t;return e&&(n="function"==typeof e?e(t):c(c({},t),e)),n},u=function(e){var t=d(e.components);return i.a.createElement(l.Provider,{value:t},e.children)},p={inlineCode:"code",wrapper:function(e){var t=e.children;return i.a.createElement(i.a.Fragment,{},t)}},m=i.a.forwardRef((function(e,t){var n=e.components,o=e.mdxType,r=e.originalType,a=e.parentName,l=s(e,["components","mdxType","originalType","parentName"]),u=d(n),m=o,b=u["".concat(a,".").concat(m)]||u[m]||p[m]||r;return n?i.a.createElement(b,c(c({ref:t},l),{},{components:n})):i.a.createElement(b,c({ref:t},l))}));function b(e,t){var n=arguments,o=t&&t.mdxType;if("string"==typeof e||o){var r=n.length,a=new Array(r);a[0]=m;var c={};for(var s in t)hasOwnProperty.call(t,s)&&(c[s]=t[s]);c.originalType=e,c.mdxType="string"==typeof e?e:o,a[1]=c;for(var l=2;l<r;l++)a[l]=n[l];return i.a.createElement.apply(null,a)}return i.a.createElement.apply(null,n)}m.displayName="MDXCreateElement"}}]);