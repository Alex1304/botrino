(window.webpackJsonp=window.webpackJsonp||[]).push([[2],{63:function(e,t,n){"use strict";n.r(t),n.d(t,"frontMatter",(function(){return c})),n.d(t,"metadata",(function(){return s})),n.d(t,"rightToc",(function(){return l})),n.d(t,"default",(function(){return d}));var r=n(3),o=n(8),a=(n(0),n(92)),i=n(95),c={title:"Introduction",slug:"/"},s={unversionedId:"intro",id:"intro",isDocsHomePage:!1,title:"Introduction",description:"\ud83d\udea7 The documentation for this framework is still work in progress. It will be complete before the first stable release which should happen very soon&trade;.",source:"@site/docs/intro.md",slug:"/",permalink:"/docs/",editUrl:"https://github.com/Alex1304/botrino/edit/main/website/docs/intro.md",version:"current",sidebar:"someSidebar",next:{title:"Getting Started",permalink:"/docs/getting-started"}},l=[{value:"What is Botrino?",id:"what-is-botrino",children:[]},{value:"Motivations",id:"motivations",children:[]},{value:"Overview",id:"overview",children:[]}],u={rightToc:l};function d(e){var t=e.components,n=Object(o.a)(e,["components"]);return Object(a.b)("wrapper",Object(r.a)({},u,n,{components:t,mdxType:"MDXLayout"}),Object(a.b)("div",{className:"admonition admonition-caution alert alert--warning"},Object(a.b)("div",Object(r.a)({parentName:"div"},{className:"admonition-heading"}),Object(a.b)("h5",{parentName:"div"},Object(a.b)("span",Object(r.a)({parentName:"h5"},{className:"admonition-icon"}),Object(a.b)("svg",Object(r.a)({parentName:"span"},{xmlns:"http://www.w3.org/2000/svg",width:"16",height:"16",viewBox:"0 0 16 16"}),Object(a.b)("path",Object(r.a)({parentName:"svg"},{fillRule:"evenodd",d:"M8.893 1.5c-.183-.31-.52-.5-.887-.5s-.703.19-.886.5L.138 13.499a.98.98 0 0 0 0 1.001c.193.31.53.501.886.501h13.964c.367 0 .704-.19.877-.5a1.03 1.03 0 0 0 .01-1.002L8.893 1.5zm.133 11.497H6.987v-2.003h2.039v2.003zm0-3.004H6.987V5.987h2.039v4.006z"})))),"caution")),Object(a.b)("div",Object(r.a)({parentName:"div"},{className:"admonition-content"}),Object(a.b)("p",{parentName:"div"},"\ud83d\udea7 ",Object(a.b)("strong",{parentName:"p"},"The documentation for this framework is still work in progress. It will be complete before the first stable release which should happen very soon","\u2122"),"."))),Object(a.b)("img",{style:{float:"right",padding:"5%"},src:Object(i.a)("img/logo.svg"),width:"20%"}),Object(a.b)("h2",{id:"what-is-botrino"},"What is Botrino?"),Object(a.b)("p",null,"Botrino is a Java framework that provides guidelines and a set of tools to build Discord bots in a more convenient way. Pretty much in the same spirit as ",Object(a.b)("a",Object(r.a)({parentName:"p"},{href:"https://spring.io/projects/spring-boot"}),"Spring Boot"),", it allows to easily setup standalone bot applications that are ready to run, embedding a few third-party libraries such as ",Object(a.b)("a",Object(r.a)({parentName:"p"},{href:"https://github.com/FasterXML/jackson-core"}),"Jackson")," for JSON-based configuration, ",Object(a.b)("a",Object(r.a)({parentName:"p"},{href:"https://alex1304.github.io/rdi/docs/intro"}),"RDI")," for a reactive IoC container, and ",Object(a.b)("a",Object(r.a)({parentName:"p"},{href:"https://discord4j.com"}),"Discord4J")," for the interface with the ",Object(a.b)("a",Object(r.a)({parentName:"p"},{href:"https://discord.com/developers/docs/intro"}),"Discord Bot API"),"."),Object(a.b)("h2",{id:"motivations"},"Motivations"),Object(a.b)("p",null,"Starting the development of a Discord bot follows most of the time the same pattern: you create a project, import your favorite Discord client library, export a configuration file or an environment variable with the bot token, and design a whole structure for your commands and your logic, before you can actually start to implement them. When working with Java, this structure is even more important otherwise you may adopt bad practices and end up with a bot that is difficult to maintain."),Object(a.b)("p",null,"This is how came the idea of this project: have something that can handle for you all the initial workflow of setting up a project with a solid structure, at the only cost of letting the framework choose some libraries for you, so that you can focus on what matters. Botrino is born."),Object(a.b)("p",null,"It also aims at providing a ",Object(a.b)("a",Object(r.a)({parentName:"p"},{href:"/docs/command-extension/overview"}),"command extension")," that integrates well with the structure of Botrino, while still letting you the choice of using your own."),Object(a.b)("h2",{id:"overview"},"Overview"),Object(a.b)("p",null,"Botrino utilizes Java modules, introduced in the JDK 9 and released in the JDK 11 as a LTS version. The classes of your application will reside in one or more modules with the following ",Object(a.b)("inlineCode",{parentName:"p"},"module-info.java")," structure:"),Object(a.b)("pre",null,Object(a.b)("code",Object(r.a)({parentName:"pre"},{className:"language-java"}),"import botrino.api.annotation.BotModule;\n\n@BotModule\nopen module com.example.myproject {\n\n    requires botrino.api;\n}\n")),Object(a.b)("p",null,"The annotation as well as the ",Object(a.b)("inlineCode",{parentName:"p"},"open")," modifier will allow Botrino to automatically scan through all the classes present in the module, in order to automatically register configuration entries, commands, services, etc."),Object(a.b)("p",null,"Inside your module, you can create services using ",Object(a.b)("a",Object(r.a)({parentName:"p"},{href:"https://alex1304.github.io/rdi/docs/annotation-based-configuration"}),"RDI annotations")," that are automatically loaded on startup:"),Object(a.b)("pre",null,Object(a.b)("code",Object(r.a)({parentName:"pre"},{className:"language-java"}),'package com.example.myproject;\n\nimport com.github.alex1304.rdi.finder.annotation.RdiFactory;\nimport com.github.alex1304.rdi.finder.annotation.RdiService;\nimport discord4j.core.GatewayDiscordClient;\nimport discord4j.core.event.domain.lifecycle.ReadyEvent;\nimport reactor.core.publisher.Mono;\nimport reactor.util.Logger;\nimport reactor.util.Loggers;\n\n@RdiService\npublic final class SampleService {\n\n    private static final Logger LOGGER = Loggers.getLogger(SampleService.class);\n\n    // We can inject other services, here we are injecting\n    // the GatewayDiscordClient provided by Botrino\n    @RdiFactory\n    public SampleService(GatewayDiscordClient gateway) {\n        gateway.on(ReadyEvent.class, ready -> Mono.fromRunnable(\n                        () -> LOGGER.info("Logged in as "\n                                + ready.getSelf().getTag())))\n                .subscribe();\n    }\n}\n\n')),Object(a.b)("p",null,"The bot is configured via a JSON configuration file with contents similar to this:"),Object(a.b)("pre",null,Object(a.b)("code",Object(r.a)({parentName:"pre"},{className:"language-json"}),'{\n    "bot": {\n        "token": "yourTokenHere",\n        "presence": {\n            "status": "online",\n            "activity_type": "playing",\n            "activity_text": "Hello world!"\n        },\n        "enabled_intents": 32509\n    },\n    "i18n": {\n        "default_locale": "en",\n        "supported_locales": ["en"]\n    }\n}\n')),Object(a.b)("p",null,"To go further and familiarize yourself with the framework, check out the ",Object(a.b)("a",Object(r.a)({parentName:"p"},{href:"/docs/getting-started"}),"Getting Started guide"),"."))}d.isMDXComponent=!0},92:function(e,t,n){"use strict";n.d(t,"a",(function(){return d})),n.d(t,"b",(function(){return m}));var r=n(0),o=n.n(r);function a(e,t,n){return t in e?Object.defineProperty(e,t,{value:n,enumerable:!0,configurable:!0,writable:!0}):e[t]=n,e}function i(e,t){var n=Object.keys(e);if(Object.getOwnPropertySymbols){var r=Object.getOwnPropertySymbols(e);t&&(r=r.filter((function(t){return Object.getOwnPropertyDescriptor(e,t).enumerable}))),n.push.apply(n,r)}return n}function c(e){for(var t=1;t<arguments.length;t++){var n=null!=arguments[t]?arguments[t]:{};t%2?i(Object(n),!0).forEach((function(t){a(e,t,n[t])})):Object.getOwnPropertyDescriptors?Object.defineProperties(e,Object.getOwnPropertyDescriptors(n)):i(Object(n)).forEach((function(t){Object.defineProperty(e,t,Object.getOwnPropertyDescriptor(n,t))}))}return e}function s(e,t){if(null==e)return{};var n,r,o=function(e,t){if(null==e)return{};var n,r,o={},a=Object.keys(e);for(r=0;r<a.length;r++)n=a[r],t.indexOf(n)>=0||(o[n]=e[n]);return o}(e,t);if(Object.getOwnPropertySymbols){var a=Object.getOwnPropertySymbols(e);for(r=0;r<a.length;r++)n=a[r],t.indexOf(n)>=0||Object.prototype.propertyIsEnumerable.call(e,n)&&(o[n]=e[n])}return o}var l=o.a.createContext({}),u=function(e){var t=o.a.useContext(l),n=t;return e&&(n="function"==typeof e?e(t):c(c({},t),e)),n},d=function(e){var t=u(e.components);return o.a.createElement(l.Provider,{value:t},e.children)},p={inlineCode:"code",wrapper:function(e){var t=e.children;return o.a.createElement(o.a.Fragment,{},t)}},b=o.a.forwardRef((function(e,t){var n=e.components,r=e.mdxType,a=e.originalType,i=e.parentName,l=s(e,["components","mdxType","originalType","parentName"]),d=u(n),b=r,m=d["".concat(i,".").concat(b)]||d[b]||p[b]||a;return n?o.a.createElement(m,c(c({ref:t},l),{},{components:n})):o.a.createElement(m,c({ref:t},l))}));function m(e,t){var n=arguments,r=t&&t.mdxType;if("string"==typeof e||r){var a=n.length,i=new Array(a);i[0]=b;var c={};for(var s in t)hasOwnProperty.call(t,s)&&(c[s]=t[s]);c.originalType=e,c.mdxType="string"==typeof e?e:r,i[1]=c;for(var l=2;l<a;l++)i[l]=n[l];return o.a.createElement.apply(null,i)}return o.a.createElement.apply(null,n)}b.displayName="MDXCreateElement"},95:function(e,t,n){"use strict";n.d(t,"b",(function(){return a})),n.d(t,"a",(function(){return i}));var r=n(21),o=n(97);function a(){var e=Object(r.default)().siteConfig,t=(e=void 0===e?{}:e).baseUrl,n=void 0===t?"/":t,a=e.url;return{withBaseUrl:function(e,t){return function(e,t,n,r){var a=void 0===r?{}:r,i=a.forcePrependBaseUrl,c=void 0!==i&&i,s=a.absolute,l=void 0!==s&&s;if(!n)return n;if(n.startsWith("#"))return n;if(Object(o.b)(n))return n;if(c)return t+n;var u=n.startsWith(t)?n:t+n.replace(/^\//,"");return l?e+u:u}(a,n,e,t)}}}function i(e,t){return void 0===t&&(t={}),(0,a().withBaseUrl)(e,t)}},97:function(e,t,n){"use strict";function r(e){return!0===/^(\w*:|\/\/)/.test(e)}function o(e){return void 0!==e&&!r(e)}n.d(t,"b",(function(){return r})),n.d(t,"a",(function(){return o}))}}]);