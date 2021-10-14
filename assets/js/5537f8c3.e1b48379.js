"use strict";(self.webpackChunkdocs=self.webpackChunkdocs||[]).push([[682],{3905:function(e,t,n){n.d(t,{Zo:function(){return d},kt:function(){return m}});var o=n(7294);function i(e,t,n){return t in e?Object.defineProperty(e,t,{value:n,enumerable:!0,configurable:!0,writable:!0}):e[t]=n,e}function r(e,t){var n=Object.keys(e);if(Object.getOwnPropertySymbols){var o=Object.getOwnPropertySymbols(e);t&&(o=o.filter((function(t){return Object.getOwnPropertyDescriptor(e,t).enumerable}))),n.push.apply(n,o)}return n}function a(e){for(var t=1;t<arguments.length;t++){var n=null!=arguments[t]?arguments[t]:{};t%2?r(Object(n),!0).forEach((function(t){i(e,t,n[t])})):Object.getOwnPropertyDescriptors?Object.defineProperties(e,Object.getOwnPropertyDescriptors(n)):r(Object(n)).forEach((function(t){Object.defineProperty(e,t,Object.getOwnPropertyDescriptor(n,t))}))}return e}function c(e,t){if(null==e)return{};var n,o,i=function(e,t){if(null==e)return{};var n,o,i={},r=Object.keys(e);for(o=0;o<r.length;o++)n=r[o],t.indexOf(n)>=0||(i[n]=e[n]);return i}(e,t);if(Object.getOwnPropertySymbols){var r=Object.getOwnPropertySymbols(e);for(o=0;o<r.length;o++)n=r[o],t.indexOf(n)>=0||Object.prototype.propertyIsEnumerable.call(e,n)&&(i[n]=e[n])}return i}var s=o.createContext({}),l=function(e){var t=o.useContext(s),n=t;return e&&(n="function"==typeof e?e(t):a(a({},t),e)),n},d=function(e){var t=l(e.components);return o.createElement(s.Provider,{value:t},e.children)},u={inlineCode:"code",wrapper:function(e){var t=e.children;return o.createElement(o.Fragment,{},t)}},p=o.forwardRef((function(e,t){var n=e.components,i=e.mdxType,r=e.originalType,s=e.parentName,d=c(e,["components","mdxType","originalType","parentName"]),p=l(n),m=i,f=p["".concat(s,".").concat(m)]||p[m]||u[m]||r;return n?o.createElement(f,a(a({ref:t},d),{},{components:n})):o.createElement(f,a({ref:t},d))}));function m(e,t){var n=arguments,i=t&&t.mdxType;if("string"==typeof e||i){var r=n.length,a=new Array(r);a[0]=p;var c={};for(var s in t)hasOwnProperty.call(t,s)&&(c[s]=t[s]);c.originalType=e,c.mdxType="string"==typeof e?e:i,a[1]=c;for(var l=2;l<r;l++)a[l]=n[l];return o.createElement.apply(null,a)}return o.createElement.apply(null,n)}p.displayName="MDXCreateElement"},8849:function(e,t,n){n.r(t),n.d(t,{frontMatter:function(){return c},metadata:function(){return s},toc:function(){return l},default:function(){return u}});var o=n(7462),i=n(3366),r=(n(7294),n(3905)),a=["components"],c={title:"Customizing the Discord client"},s={unversionedId:"api/customizing-the-discord-client",id:"api/customizing-the-discord-client",isDocsHomePage:!1,title:"Customizing the Discord client",description:"When you start a project with Botrino, the Discord client is constructed for you, so you don't have anything to do in order to run the bot. However, in most cases, you will want to take full control on how the Discord client is created. This section will show you how to fully customize the Discord client and the gateway login process.",source:"@site/docs/api/customizing-the-discord-client.md",sourceDirName:"api",slug:"/api/customizing-the-discord-client",permalink:"/docs/api/customizing-the-discord-client",editUrl:"https://github.com/Alex1304/botrino/edit/main/website/docs/api/customizing-the-discord-client.md",version:"current",frontMatter:{title:"Customizing the Discord client"},sidebar:"someSidebar",previous:{title:"Configuring your bot",permalink:"/docs/api/configuring-your-bot"},next:{title:"Extensions",permalink:"/docs/api/extensions"}},l=[{value:"The <code>LoginHandler</code> interface",id:"the-loginhandler-interface",children:[]}],d={toc:l};function u(e){var t=e.components,n=(0,i.Z)(e,a);return(0,r.kt)("wrapper",(0,o.Z)({},d,n,{components:t,mdxType:"MDXLayout"}),(0,r.kt)("p",null,"When you start a project with Botrino, the Discord client is constructed for you, so you don't have anything to do in order to run the bot. However, in most cases, you will want to take full control on how the Discord client is created. This section will show you how to fully customize the Discord client and the gateway login process."),(0,r.kt)("h2",{id:"the-loginhandler-interface"},"The ",(0,r.kt)("inlineCode",{parentName:"h2"},"LoginHandler")," interface"),(0,r.kt)("p",null,"All you need to do is to provide one implementation of the ",(0,r.kt)("inlineCode",{parentName:"p"},"LoginHandler")," interface. It defines one method, ",(0,r.kt)("inlineCode",{parentName:"p"},"Mono<GatewayDiscordClient> login(ConfigContainer configContainer)"),", that you can override to define yourself how your bot connects to the Discord gateway. The default implementation of this method builds the Discord client with default settings, using the token, presence status, and intents from the configuration. It can be recreated like this:"),(0,r.kt)("pre",null,(0,r.kt)("code",{parentName:"pre",className:"language-java"},"package com.example.myproject;\n\nimport botrino.api.config.object.BotConfig;\nimport discord4j.core.DiscordClient;\nimport discord4j.core.GatewayDiscordClient;\nimport discord4j.core.object.presence.Presence;\nimport discord4j.core.shard.MemberRequestFilter;\nimport discord4j.gateway.intent.IntentSet;\nimport reactor.core.publisher.Mono;\n\npublic final class DefaultLoginHandler implements LoginHandler {\n\n    @Override\n    public Mono<GatewayDiscordClient> login(ConfigContainer configContainer) {\n        var config = configContainer.get(BotConfig.class);\n        var discordClient = DiscordClient.create(config.token());\n        return discordClient.gateway()\n                .setInitialStatus(shard -> config.presence()\n                        .map(BotConfig.StatusConfig::toStatusUpdate)\n                        .orElseGet(Presence::online))\n                .setEnabledIntents(config.enabledIntents().stream().boxed()\n                        .map(IntentSet::of)\n                        .findAny()\n                        .orElseGet(IntentSet::nonPrivileged))\n                .setMemberRequestFilter(MemberRequestFilter.none())\n                .login()\n                .single();\n    }\n}\n")),(0,r.kt)("div",{className:"admonition admonition-caution alert alert--warning"},(0,r.kt)("div",{parentName:"div",className:"admonition-heading"},(0,r.kt)("h5",{parentName:"div"},(0,r.kt)("span",{parentName:"h5",className:"admonition-icon"},(0,r.kt)("svg",{parentName:"span",xmlns:"http://www.w3.org/2000/svg",width:"16",height:"16",viewBox:"0 0 16 16"},(0,r.kt)("path",{parentName:"svg",fillRule:"evenodd",d:"M8.893 1.5c-.183-.31-.52-.5-.887-.5s-.703.19-.886.5L.138 13.499a.98.98 0 0 0 0 1.001c.193.31.53.501.886.501h13.964c.367 0 .704-.19.877-.5a1.03 1.03 0 0 0 .01-1.002L8.893 1.5zm.133 11.497H6.987v-2.003h2.039v2.003zm0-3.004H6.987V5.987h2.039v4.006z"}))),"caution")),(0,r.kt)("div",{parentName:"div",className:"admonition-content"},(0,r.kt)("ul",{parentName:"div"},(0,r.kt)("li",{parentName:"ul"},"The implementation class must have a no-arg constructor."),(0,r.kt)("li",{parentName:"ul"},"If more than one implementation of ",(0,r.kt)("inlineCode",{parentName:"li"},"LoginHandler")," are found, it will result in an error as it is impossible to determine which one to use. If you don't want to remove the extra implementation(s), you can mark one of them with the ",(0,r.kt)("inlineCode",{parentName:"li"},"@Primary")," annotation to lift the ambiguity. You may alternatively use the ",(0,r.kt)("inlineCode",{parentName:"li"},"@Exclude")," annotation if you don't want one implementation to be picked up by Botrino.")))))}u.isMDXComponent=!0}}]);