"use strict";(self.webpackChunkdocs=self.webpackChunkdocs||[]).push([[925],{3905:function(e,t,n){n.d(t,{Zo:function(){return m},kt:function(){return p}});var r=n(7294);function o(e,t,n){return t in e?Object.defineProperty(e,t,{value:n,enumerable:!0,configurable:!0,writable:!0}):e[t]=n,e}function a(e,t){var n=Object.keys(e);if(Object.getOwnPropertySymbols){var r=Object.getOwnPropertySymbols(e);t&&(r=r.filter((function(t){return Object.getOwnPropertyDescriptor(e,t).enumerable}))),n.push.apply(n,r)}return n}function i(e){for(var t=1;t<arguments.length;t++){var n=null!=arguments[t]?arguments[t]:{};t%2?a(Object(n),!0).forEach((function(t){o(e,t,n[t])})):Object.getOwnPropertyDescriptors?Object.defineProperties(e,Object.getOwnPropertyDescriptors(n)):a(Object(n)).forEach((function(t){Object.defineProperty(e,t,Object.getOwnPropertyDescriptor(n,t))}))}return e}function s(e,t){if(null==e)return{};var n,r,o=function(e,t){if(null==e)return{};var n,r,o={},a=Object.keys(e);for(r=0;r<a.length;r++)n=a[r],t.indexOf(n)>=0||(o[n]=e[n]);return o}(e,t);if(Object.getOwnPropertySymbols){var a=Object.getOwnPropertySymbols(e);for(r=0;r<a.length;r++)n=a[r],t.indexOf(n)>=0||Object.prototype.propertyIsEnumerable.call(e,n)&&(o[n]=e[n])}return o}var l=r.createContext({}),c=function(e){var t=r.useContext(l),n=t;return e&&(n="function"==typeof e?e(t):i(i({},t),e)),n},m=function(e){var t=c(e.components);return r.createElement(l.Provider,{value:t},e.children)},d={inlineCode:"code",wrapper:function(e){var t=e.children;return r.createElement(r.Fragment,{},t)}},u=r.forwardRef((function(e,t){var n=e.components,o=e.mdxType,a=e.originalType,l=e.parentName,m=s(e,["components","mdxType","originalType","parentName"]),u=c(n),p=o,h=u["".concat(l,".").concat(p)]||u[p]||d[p]||a;return n?r.createElement(h,i(i({ref:t},m),{},{components:n})):r.createElement(h,i({ref:t},m))}));function p(e,t){var n=arguments,o=t&&t.mdxType;if("string"==typeof e||o){var a=n.length,i=new Array(a);i[0]=u;var s={};for(var l in t)hasOwnProperty.call(t,l)&&(s[l]=t[l]);s.originalType=e,s.mdxType="string"==typeof e?e:o,i[1]=s;for(var c=2;c<a;c++)i[c]=n[c];return r.createElement.apply(null,i)}return r.createElement.apply(null,n)}u.displayName="MDXCreateElement"},3919:function(e,t,n){function r(e){return!0===/^(\w*:|\/\/)/.test(e)}function o(e){return void 0!==e&&!r(e)}n.d(t,{b:function(){return r},Z:function(){return o}})},4996:function(e,t,n){n.d(t,{C:function(){return a},Z:function(){return i}});var r=n(2263),o=n(3919);function a(){var e=(0,r.Z)().siteConfig,t=(e=void 0===e?{}:e).baseUrl,n=void 0===t?"/":t,a=e.url;return{withBaseUrl:function(e,t){return function(e,t,n,r){var a=void 0===r?{}:r,i=a.forcePrependBaseUrl,s=void 0!==i&&i,l=a.absolute,c=void 0!==l&&l;if(!n)return n;if(n.startsWith("#"))return n;if((0,o.b)(n))return n;if(s)return t+n;var m=n.startsWith(t)?n:t+n.replace(/^\//,"");return c?e+m:m}(a,n,e,t)}}}function i(e,t){return void 0===t&&(t={}),(0,a().withBaseUrl)(e,t)}},955:function(e,t,n){n.r(t),n.d(t,{frontMatter:function(){return l},metadata:function(){return c},toc:function(){return m},default:function(){return u}});var r=n(7462),o=n(3366),a=(n(7294),n(3905)),i=n(4996),s=["components"],l={title:"Filtering and adapting events"},c={unversionedId:"command-extension/filtering-and-adapting-events",id:"command-extension/filtering-and-adapting-events",isDocsHomePage:!1,title:"Filtering and adapting events",description:"Commands are triggered when a message create event is received via the Discord gateway. It is possible for you to intervene between the moment the event is received and the moment it parses the message content to trigger a command. This allows you to drop some events to prevent any command from being executed in a certain context, or to determine which prefix the command must start with and which locale to use according to the event received.",source:"@site/docs/command-extension/filtering-and-adapting-events.md",sourceDirName:"command-extension",slug:"/command-extension/filtering-and-adapting-events",permalink:"/docs/command-extension/filtering-and-adapting-events",editUrl:"https://github.com/Alex1304/botrino/edit/main/website/docs/command-extension/filtering-and-adapting-events.md",version:"current",frontMatter:{title:"Filtering and adapting events"},sidebar:"someSidebar",previous:{title:"Handling errors",permalink:"/docs/command-extension/handling-errors"},next:{title:"Documenting commands",permalink:"/docs/command-extension/documenting-commands"}},m=[{value:"The <code>CommandEventProcessor</code> interface",id:"the-commandeventprocessor-interface",children:[]}],d={toc:m};function u(e){var t=e.components,n=(0,o.Z)(e,s);return(0,a.kt)("wrapper",(0,r.Z)({},d,n,{components:t,mdxType:"MDXLayout"}),(0,a.kt)("p",null,"Commands are triggered when a message create event is received via the Discord gateway. It is possible for you to intervene between the moment the event is received and the moment it parses the message content to trigger a command. This allows you to drop some events to prevent any command from being executed in a certain context, or to determine which prefix the command must start with and which locale to use according to the event received."),(0,a.kt)("h2",{id:"the-commandeventprocessor-interface"},"The ",(0,a.kt)("inlineCode",{parentName:"h2"},"CommandEventProcessor")," interface"),(0,a.kt)("p",null,"Create a class in your bot module that implements ",(0,a.kt)("inlineCode",{parentName:"p"},"CommandEventProcessor"),". Botrino will pick it up and will automatically inject it in the internal listener to message create events."),(0,a.kt)("pre",null,(0,a.kt)("code",{parentName:"pre",className:"language-java"},'package com.example.myproject;\n\nimport botrino.command.CommandEventProcessor;\nimport discord4j.core.event.domain.message.MessageCreateEvent;\nimport discord4j.core.object.entity.channel.GuildMessageChannel;\nimport reactor.core.publisher.Mono;\n\nimport java.util.Locale;\n\npublic final class ExampleEventProcessor implements CommandEventProcessor {\n\n    @Override\n    public Mono<Boolean> filter(MessageCreateEvent event) {\n        return Mono.just(!event.getMessage().getAuthor()\n                            .map(User::isBot).orElse(true)\n                && !event.getMessage().getContent().contains("ignore"));\n    }\n\n    @Override\n    public Mono<String> prefixForEvent(MessageCreateEvent event) {\n        return event.getGuild()\n                .map(guild -> guild.getName().charAt(0))\n                .filter(c -> ("" + c).matches("[A-Za-z0-9]"))\n                .map(c -> c + "!");\n    }\n\n    @Override\n    public Mono<Locale> localeForEvent(MessageCreateEvent event) {\n        return event.getMessage().getChannel()\n                .ofType(GuildMessageChannel.class)\n                .flatMap(channel -> {\n                    if (channel.getName().endsWith("fr")) {\n                        return Mono.just(Locale.FRENCH);\n                    } else if (channel.getName().endsWith("de")) {\n                        return Mono.just(Locale.GERMAN);\n                    } else {\n                        return Mono.empty();\n                    }\n                });\n    }\n}\n')),(0,a.kt)("ul",null,(0,a.kt)("li",{parentName:"ul"},(0,a.kt)("inlineCode",{parentName:"li"},"filter(MessageCreateEvent)")," allows to decide whether to keep or to drop the given ",(0,a.kt)("inlineCode",{parentName:"li"},"MessageCreateEvent"),". The default implementation will drop events from bot accounts and webhooks (unless you know what you're doing, ",(0,a.kt)("strong",{parentName:"li"},"it is highly recommended to keep this behavior"),'). This method can also be useful if you want to implement a blacklist system to prevent some people or some guilds from using the commands of your bot. In this example, we are filtering out events coming from bots, but we are also filtering out events which message content contains the string "ignore".'),(0,a.kt)("li",{parentName:"ul"},(0,a.kt)("inlineCode",{parentName:"li"},"prefixForEvent(MessageCreateEvent)")," allows to dynamically change the command prefix according to the event received. In order to keep the default prefix, you are expected to return an empty ",(0,a.kt)("inlineCode",{parentName:"li"},"Mono"),". If you want to provide a way for your guilds to configure their own custom prefix, this is where you would implement it. In this example, we are taking the first letter of the guild name, and define the prefix as the letter followed by ",(0,a.kt)("inlineCode",{parentName:"li"},"!"),". If the message was sent in DMs or if the guild name doesn't start with an alphanumeric character, it will use the default prefix of the bot defined in the configuration file."),(0,a.kt)("li",{parentName:"ul"},(0,a.kt)("inlineCode",{parentName:"li"},"localeForEvent(MessageCreateEvent)")," allows to adapt the locale according to the event received. In order to keep the default locale, you are expected to return an empty ",(0,a.kt)("inlineCode",{parentName:"li"},"Mono"),". The returned locale will be accessible via the ",(0,a.kt)("inlineCode",{parentName:"li"},"CommandContext")," (which implements ",(0,a.kt)("inlineCode",{parentName:"li"},"Translator"),") once the event is recognized as a command. You will typically store the locale in a database (either per guild or per user) and retrieve it here using the data given by the message create event. In this example, we are interpreting the suffix of the channel name as the locale, with only French and German being supported. It will use the default locale defined in the configuration file in all other cases.")),(0,a.kt)("div",{className:"admonition admonition-caution alert alert--warning"},(0,a.kt)("div",{parentName:"div",className:"admonition-heading"},(0,a.kt)("h5",{parentName:"div"},(0,a.kt)("span",{parentName:"h5",className:"admonition-icon"},(0,a.kt)("svg",{parentName:"span",xmlns:"http://www.w3.org/2000/svg",width:"16",height:"16",viewBox:"0 0 16 16"},(0,a.kt)("path",{parentName:"svg",fillRule:"evenodd",d:"M8.893 1.5c-.183-.31-.52-.5-.887-.5s-.703.19-.886.5L.138 13.499a.98.98 0 0 0 0 1.001c.193.31.53.501.886.501h13.964c.367 0 .704-.19.877-.5a1.03 1.03 0 0 0 .01-1.002L8.893 1.5zm.133 11.497H6.987v-2.003h2.039v2.003zm0-3.004H6.987V5.987h2.039v4.006z"}))),"caution")),(0,a.kt)("div",{parentName:"div",className:"admonition-content"},(0,a.kt)("ul",{parentName:"div"},(0,a.kt)("li",{parentName:"ul"},"The implementation class must have a no-arg constructor."),(0,a.kt)("li",{parentName:"ul"},"If more than one implementation of ",(0,a.kt)("inlineCode",{parentName:"li"},"CommandEventProcessor")," are found, it will result in an error as it is impossible to determine which one to use. If you don't want to remove the extra implementation(s), you can mark one of them with the ",(0,a.kt)("inlineCode",{parentName:"li"},"@Primary")," annotation to lift the ambiguity. You may alternatively use the ",(0,a.kt)("inlineCode",{parentName:"li"},"@Exclude")," annotation if you don't want one implementation to be picked up by Botrino.")))),(0,a.kt)("p",null,'Here is the example implementation above in action, in a guild named "test". You can notice the bot now responds with ',(0,a.kt)("inlineCode",{parentName:"p"},"t!"),' instead of the default prefix as the guild name starts with "t", and the ',(0,a.kt)("inlineCode",{parentName:"p"},"t!ping ignore"),' produces no response because the event was dropped due to the presence of "ignore" in the message content.'),(0,a.kt)("img",{src:(0,i.Z)("img/eventProcessorExample.png"),alt:""}))}u.isMDXComponent=!0}}]);