"use strict";(self.webpackChunkdocs=self.webpackChunkdocs||[]).push([[162],{3905:function(e,t,n){n.d(t,{Zo:function(){return c},kt:function(){return m}});var a=n(7294);function i(e,t,n){return t in e?Object.defineProperty(e,t,{value:n,enumerable:!0,configurable:!0,writable:!0}):e[t]=n,e}function r(e,t){var n=Object.keys(e);if(Object.getOwnPropertySymbols){var a=Object.getOwnPropertySymbols(e);t&&(a=a.filter((function(t){return Object.getOwnPropertyDescriptor(e,t).enumerable}))),n.push.apply(n,a)}return n}function o(e){for(var t=1;t<arguments.length;t++){var n=null!=arguments[t]?arguments[t]:{};t%2?r(Object(n),!0).forEach((function(t){i(e,t,n[t])})):Object.getOwnPropertyDescriptors?Object.defineProperties(e,Object.getOwnPropertyDescriptors(n)):r(Object(n)).forEach((function(t){Object.defineProperty(e,t,Object.getOwnPropertyDescriptor(n,t))}))}return e}function l(e,t){if(null==e)return{};var n,a,i=function(e,t){if(null==e)return{};var n,a,i={},r=Object.keys(e);for(a=0;a<r.length;a++)n=r[a],t.indexOf(n)>=0||(i[n]=e[n]);return i}(e,t);if(Object.getOwnPropertySymbols){var r=Object.getOwnPropertySymbols(e);for(a=0;a<r.length;a++)n=r[a],t.indexOf(n)>=0||Object.prototype.propertyIsEnumerable.call(e,n)&&(i[n]=e[n])}return i}var p=a.createContext({}),d=function(e){var t=a.useContext(p),n=t;return e&&(n="function"==typeof e?e(t):o(o({},t),e)),n},c=function(e){var t=d(e.components);return a.createElement(p.Provider,{value:t},e.children)},u={inlineCode:"code",wrapper:function(e){var t=e.children;return a.createElement(a.Fragment,{},t)}},s=a.forwardRef((function(e,t){var n=e.components,i=e.mdxType,r=e.originalType,p=e.parentName,c=l(e,["components","mdxType","originalType","parentName"]),s=d(n),m=i,h=s["".concat(p,".").concat(m)]||s[m]||u[m]||r;return n?a.createElement(h,o(o({ref:t},c),{},{components:n})):a.createElement(h,o({ref:t},c))}));function m(e,t){var n=arguments,i=t&&t.mdxType;if("string"==typeof e||i){var r=n.length,o=new Array(r);o[0]=s;var l={};for(var p in t)hasOwnProperty.call(t,p)&&(l[p]=t[p]);l.originalType=e,l.mdxType="string"==typeof e?e:i,o[1]=l;for(var d=2;d<r;d++)o[d]=n[d];return a.createElement.apply(null,o)}return a.createElement.apply(null,n)}s.displayName="MDXCreateElement"},3919:function(e,t,n){function a(e){return!0===/^(\w*:|\/\/)/.test(e)}function i(e){return void 0!==e&&!a(e)}n.d(t,{b:function(){return a},Z:function(){return i}})},4996:function(e,t,n){n.d(t,{C:function(){return r},Z:function(){return o}});var a=n(2263),i=n(3919);function r(){var e=(0,a.Z)().siteConfig,t=(e=void 0===e?{}:e).baseUrl,n=void 0===t?"/":t,r=e.url;return{withBaseUrl:function(e,t){return function(e,t,n,a){var r=void 0===a?{}:a,o=r.forcePrependBaseUrl,l=void 0!==o&&o,p=r.absolute,d=void 0!==p&&p;if(!n)return n;if(n.startsWith("#"))return n;if((0,i.b)(n))return n;if(l)return t+n;var c=n.startsWith(t)?n:t+n.replace(/^\//,"");return d?e+c:c}(r,n,e,t)}}}function o(e,t){return void 0===t&&(t={}),(0,r().withBaseUrl)(e,t)}},8215:function(e,t,n){var a=n(7294);t.Z=function(e){var t=e.children,n=e.hidden,i=e.className;return a.createElement("div",{role:"tabpanel",hidden:n,className:i},t)}},1395:function(e,t,n){n.d(t,{Z:function(){return c}});var a=n(7294),i=n(944),r=n(6010),o="tabItem_1uMI",l="tabItemActive_2DSg";var p=37,d=39;var c=function(e){var t=e.lazy,n=e.block,c=e.defaultValue,u=e.values,s=e.groupId,m=e.className,h=(0,i.Z)(),k=h.tabGroupChoices,f=h.setTabGroupChoices,v=(0,a.useState)(c),g=v[0],y=v[1],N=a.Children.toArray(e.children),b=[];if(null!=s){var w=k[s];null!=w&&w!==g&&u.some((function(e){return e.value===w}))&&y(w)}var C=function(e){var t=e.currentTarget,n=b.indexOf(t),a=u[n].value;y(a),null!=s&&(f(s,a),setTimeout((function(){var e,n,a,i,r,o,p,d;(e=t.getBoundingClientRect(),n=e.top,a=e.left,i=e.bottom,r=e.right,o=window,p=o.innerHeight,d=o.innerWidth,n>=0&&r<=d&&i<=p&&a>=0)||(t.scrollIntoView({block:"center",behavior:"smooth"}),t.classList.add(l),setTimeout((function(){return t.classList.remove(l)}),2e3))}),150))},j=function(e){var t,n;switch(e.keyCode){case d:var a=b.indexOf(e.target)+1;n=b[a]||b[0];break;case p:var i=b.indexOf(e.target)-1;n=b[i]||b[b.length-1]}null==(t=n)||t.focus()};return a.createElement("div",{className:"tabs-container"},a.createElement("ul",{role:"tablist","aria-orientation":"horizontal",className:(0,r.Z)("tabs",{"tabs--block":n},m)},u.map((function(e){var t=e.value,n=e.label;return a.createElement("li",{role:"tab",tabIndex:g===t?0:-1,"aria-selected":g===t,className:(0,r.Z)("tabs__item",o,{"tabs__item--active":g===t}),key:t,ref:function(e){return b.push(e)},onKeyDown:j,onFocus:C,onClick:C},n)}))),t?(0,a.cloneElement)(N.filter((function(e){return e.props.value===g}))[0],{className:"margin-vert--md"}):a.createElement("div",{className:"margin-vert--md"},N.map((function(e,t){return(0,a.cloneElement)(e,{key:t,hidden:e.props.value!==g})}))))}},9443:function(e,t,n){var a=(0,n(7294).createContext)(void 0);t.Z=a},944:function(e,t,n){var a=n(7294),i=n(9443);t.Z=function(){var e=(0,a.useContext)(i.Z);if(null==e)throw new Error("`useUserPreferencesContext` is used outside of `Layout` Component.");return e}},601:function(e,t,n){n.r(t),n.d(t,{frontMatter:function(){return c},metadata:function(){return u},toc:function(){return s},default:function(){return h}});var a=n(7462),i=n(3366),r=(n(7294),n(3905)),o=n(4996),l=n(1395),p=n(8215),d=["components"],c={title:"Getting Started"},u={unversionedId:"getting-started",id:"getting-started",isDocsHomePage:!1,title:"Getting Started",description:"Prerequisites",source:"@site/docs/getting-started.md",sourceDirName:".",slug:"/getting-started",permalink:"/docs/getting-started",editUrl:"https://github.com/Alex1304/botrino/edit/main/website/docs/getting-started.md",version:"current",frontMatter:{title:"Getting Started"},sidebar:"someSidebar",previous:{title:"Introduction",permalink:"/docs/"},next:{title:"Working with services",permalink:"/docs/api/working-with-services"}},s=[{value:"Prerequisites",id:"prerequisites",children:[]},{value:"From the Maven archetype",id:"from-the-maven-archetype",children:[]},{value:"From a blank project",id:"from-a-blank-project",children:[]},{value:"Running your bot",id:"running-your-bot",children:[{value:"During development",id:"during-development",children:[]},{value:"In a production environment",id:"in-a-production-environment",children:[]},{value:"Adding system modules to the JLink runtime image",id:"adding-system-modules-to-the-jlink-runtime-image",children:[]}]}],m={toc:s};function h(e){var t=e.components,n=(0,i.Z)(e,d);return(0,r.kt)("wrapper",(0,a.Z)({},m,n,{components:t,mdxType:"MDXLayout"}),(0,r.kt)("h2",{id:"prerequisites"},"Prerequisites"),(0,r.kt)("ul",null,(0,r.kt)("li",{parentName:"ul"},"JDK 11 or above. You can download the OpenJDK ",(0,r.kt)("a",{parentName:"li",href:"https://adoptopenjdk.net/?variant=openjdk11&jvmVariant=hotspot"},"here")),(0,r.kt)("li",{parentName:"ul"},"Apache Maven 3, preferably the latest version available ",(0,r.kt)("a",{parentName:"li",href:"https://maven.apache.org/download.cgi"},"here"),".")),(0,r.kt)("p",null,"This documentation assumes you have decent knowledge of the Java programming language. Being familiar with Discord4J and reactive programming is not required, although recommended. The ",(0,r.kt)("a",{parentName:"p",href:"https://wiki.discord4j.com"},"Discord4J documentation")," provides great guides to get started with ",(0,r.kt)("a",{parentName:"p",href:"https://wiki.discord4j.com/en/latest/Reactive-(Reactor)-Tutorial/"},"reactive programming")," and ",(0,r.kt)("a",{parentName:"p",href:"https://wiki.discord4j.com/en/latest/Lambda-Tutorial/"},"advanced Java features"),"."),(0,r.kt)("h2",{id:"from-the-maven-archetype"},"From the Maven archetype"),(0,r.kt)("p",null,"The recommended way to start a project with Botrino is to use the Maven archetype (replace ",(0,r.kt)("inlineCode",{parentName:"p"},"[VERSION]")," with the latest version available): ",(0,r.kt)("a",{parentName:"p",href:"https://search.maven.org/artifact/com.alex1304.botrino/botrino-api"},(0,r.kt)("img",{parentName:"a",src:"https://img.shields.io/maven-central/v/com.alex1304.botrino/botrino-api",alt:"Maven Central"}))),(0,r.kt)("pre",null,(0,r.kt)("code",{parentName:"pre"},"mvn archetype:generate -DarchetypeGroupId=com.alex1304.botrino -DarchetypeArtifactId=botrino-archetype -DarchetypeVersion=[VERSION]\n")),(0,r.kt)("p",null,"You will be asked to enter the ",(0,r.kt)("inlineCode",{parentName:"p"},"groupId"),", the ",(0,r.kt)("inlineCode",{parentName:"p"},"artifactId"),", the ",(0,r.kt)("inlineCode",{parentName:"p"},"version")," and the ",(0,r.kt)("inlineCode",{parentName:"p"},"package")," of your project. If successful, it should generate a project with the following contents:"),(0,r.kt)("pre",null,(0,r.kt)("code",{parentName:"pre"},"myproject\n\u251c\u2500\u2500 app\n\u2502\xa0\xa0 \u251c\u2500\u2500 pom.xml\n\u2502\xa0\xa0 \u2514\u2500\u2500 src\n\u2502\xa0\xa0     \u2514\u2500\u2500 main\n\u2502\xa0\xa0         \u251c\u2500\u2500 external-resources\n\u2502\xa0\xa0         \u2502\xa0\xa0 \u251c\u2500\u2500 config.json\n\u2502\xa0\xa0         \u2502\xa0\xa0 \u251c\u2500\u2500 launcher.cmd\n\u2502\xa0\xa0         \u2502\xa0\xa0 \u2514\u2500\u2500 logback.xml\n\u2502\xa0\xa0         \u251c\u2500\u2500 java\n\u2502\xa0\xa0         \u2502\xa0\xa0 \u251c\u2500\u2500 com\n\u2502\xa0\xa0         \u2502\xa0\xa0 \u2502\xa0\xa0 \u2514\u2500\u2500 example\n\u2502\xa0\xa0         \u2502\xa0\xa0 \u2502\xa0\xa0     \u2514\u2500\u2500 myproject\n\u2502\xa0\xa0         \u2502\xa0\xa0 \u2502\xa0\xa0         \u251c\u2500\u2500 Main.java\n\u2502\xa0\xa0         \u2502\xa0\xa0 \u2502\xa0\xa0         \u251c\u2500\u2500 PingCommand.java\n\u2502\xa0\xa0         \u2502\xa0\xa0 \u2502\xa0\xa0         \u251c\u2500\u2500 SampleService.java\n\u2502\xa0\xa0         \u2502\xa0\xa0 \u2502\xa0\xa0         \u2514\u2500\u2500 Strings.java\n\u2502\xa0\xa0         \u2502\xa0\xa0 \u2514\u2500\u2500 module-info.java\n\u2502\xa0\xa0         \u2514\u2500\u2500 resources\n\u2502\xa0\xa0             \u2514\u2500\u2500 AppStrings.properties\n\u251c\u2500\u2500 delivery\n\u2502\xa0\xa0 \u2514\u2500\u2500 pom.xml\n\u251c\u2500\u2500 launcher\n\u2502\xa0\xa0 \u251c\u2500\u2500 pom.xml\n\u2502\xa0\xa0 \u2514\u2500\u2500 src\n\u2502\xa0\xa0     \u2514\u2500\u2500 main\n\u2502\xa0\xa0         \u2514\u2500\u2500 java\n\u2502\xa0\xa0             \u251c\u2500\u2500 com\n\u2502\xa0\xa0             \u2502\xa0\xa0 \u2514\u2500\u2500 example\n\u2502\xa0\xa0             \u2502\xa0\xa0     \u2514\u2500\u2500 myproject\n\u2502\xa0\xa0             \u2502\xa0\xa0         \u2514\u2500\u2500 Launcher.java\n\u2502\xa0\xa0             \u2514\u2500\u2500 module-info.java\n\u2514\u2500\u2500 pom.xml\n")),(0,r.kt)("ul",null,(0,r.kt)("li",{parentName:"ul"},"The ",(0,r.kt)("inlineCode",{parentName:"li"},"app/")," directory corresponds to the main module of your bot application. It already contains pre-generated classes with a main method, an example command and an example service. It also shows how to externalize strings via a ",(0,r.kt)("inlineCode",{parentName:"li"},".properties")," files in the root of ",(0,r.kt)("inlineCode",{parentName:"li"},"src/main/resources"),", and a class ",(0,r.kt)("inlineCode",{parentName:"li"},"Strings")," containing constants to reference them. The ",(0,r.kt)("inlineCode",{parentName:"li"},"src/main/external-resources")," directory contains the configuration files necessary to run the bot."),(0,r.kt)("li",{parentName:"ul"},"The ",(0,r.kt)("inlineCode",{parentName:"li"},"delivery/")," directory only contains a ",(0,r.kt)("inlineCode",{parentName:"li"},"pom.xml")," that is capable of generating a runtime image of the bot application using the ",(0,r.kt)("inlineCode",{parentName:"li"},"jlink")," utility, bundled with the JDK 11."),(0,r.kt)("li",{parentName:"ul"},"The ",(0,r.kt)("inlineCode",{parentName:"li"},"launcher")," directory contains the module used by ",(0,r.kt)("inlineCode",{parentName:"li"},"delivery")," to create a basic launcher for the runtime image."),(0,r.kt)("li",{parentName:"ul"},"The ",(0,r.kt)("inlineCode",{parentName:"li"},"pom.xml")," which configures the project by importing the libraries and configuring the multi-module build.")),(0,r.kt)("div",{className:"admonition admonition-info alert alert--info"},(0,r.kt)("div",{parentName:"div",className:"admonition-heading"},(0,r.kt)("h5",{parentName:"div"},(0,r.kt)("span",{parentName:"h5",className:"admonition-icon"},(0,r.kt)("svg",{parentName:"span",xmlns:"http://www.w3.org/2000/svg",width:"14",height:"16",viewBox:"0 0 14 16"},(0,r.kt)("path",{parentName:"svg",fillRule:"evenodd",d:"M7 2.3c3.14 0 5.7 2.56 5.7 5.7s-2.56 5.7-5.7 5.7A5.71 5.71 0 0 1 1.3 8c0-3.14 2.56-5.7 5.7-5.7zM7 1C3.14 1 0 4.14 0 8s3.14 7 7 7 7-3.14 7-7-3.14-7-7-7zm1 3H6v5h2V4zm0 6H6v2h2v-2z"}))),"info")),(0,r.kt)("div",{parentName:"div",className:"admonition-content"},(0,r.kt)("p",{parentName:"div"},"The archetype will automatically include the ",(0,r.kt)("a",{parentName:"p",href:"/docs/interaction-library/overview"},"interaction library")," in your project dependencies."))),(0,r.kt)("p",null,"This project is ready to be opened in your favorite IDE (Eclipse, IntelliJ...), and you can directly jump to the ",(0,r.kt)("a",{parentName:"p",href:"#running-your-bot"},"Running your bot")," section."),(0,r.kt)("h2",{id:"from-a-blank-project"},"From a blank project"),(0,r.kt)("p",null,"If you don't want the JLink runtime image, or if you want to use a build tool other than Maven, you may as well start from a blank project and import Botrino yourself. Be aware that it will require a bit more effort to set up than using the archetype."),(0,r.kt)("p",null,"Import the following dependency:"),(0,r.kt)(l.Z,{groupId:"build-tools",defaultValue:"maven",values:[{label:"Maven",value:"maven"},{label:"Gradle",value:"gradle"}],mdxType:"Tabs"},(0,r.kt)(p.Z,{value:"maven",mdxType:"TabItem"},(0,r.kt)("pre",null,(0,r.kt)("code",{parentName:"pre",className:"language-xml"},"<dependency>\n    <groupId>com.alex1304.botrino</groupId>\n    <artifactId>botrino-api</artifactId>\n    <version>[VERSION]</version>\n</dependency>\n"))),(0,r.kt)(p.Z,{value:"gradle",mdxType:"TabItem"},(0,r.kt)("pre",null,(0,r.kt)("code",{parentName:"pre",className:"language-groovy"},"repositories {\n    mavenCentral()\n}\n\ndependencies {\n    implementation 'com.alex1304.botrino:botrino-api:[VERSION]'\n}\n")))),(0,r.kt)("p",null,"As usual, replace ",(0,r.kt)("inlineCode",{parentName:"p"},"[VERSION]")," with the latest version available: ",(0,r.kt)("a",{parentName:"p",href:"https://search.maven.org/artifact/com.alex1304.botrino/botrino-api"},(0,r.kt)("img",{parentName:"a",src:"https://img.shields.io/maven-central/v/com.alex1304.botrino/botrino-api",alt:"Maven Central"}))),(0,r.kt)("p",null,"Create a ",(0,r.kt)("inlineCode",{parentName:"p"},"module-info.java")," annotated with ",(0,r.kt)("inlineCode",{parentName:"p"},"@BotModule"),", with the ",(0,r.kt)("inlineCode",{parentName:"p"},"open")," modifier and that requires the ",(0,r.kt)("inlineCode",{parentName:"p"},"botrino.api")," module:"),(0,r.kt)("pre",null,(0,r.kt)("code",{parentName:"pre",className:"language-java"},"import botrino.api.annotation.BotModule;\n\n@BotModule\nopen module com.example.myproject {\n\n    requires botrino.api;\n}\n")),(0,r.kt)("p",null,"The module transitively requires all libraries necessary to work, including Discord4J, Reactor, Netty, RDI and Jackson, so you don't need to put ",(0,r.kt)("inlineCode",{parentName:"p"},"requires")," for those libraries."),(0,r.kt)("div",{className:"admonition admonition-caution alert alert--warning"},(0,r.kt)("div",{parentName:"div",className:"admonition-heading"},(0,r.kt)("h5",{parentName:"div"},(0,r.kt)("span",{parentName:"h5",className:"admonition-icon"},(0,r.kt)("svg",{parentName:"span",xmlns:"http://www.w3.org/2000/svg",width:"16",height:"16",viewBox:"0 0 16 16"},(0,r.kt)("path",{parentName:"svg",fillRule:"evenodd",d:"M8.893 1.5c-.183-.31-.52-.5-.887-.5s-.703.19-.886.5L.138 13.499a.98.98 0 0 0 0 1.001c.193.31.53.501.886.501h13.964c.367 0 .704-.19.877-.5a1.03 1.03 0 0 0 .01-1.002L8.893 1.5zm.133 11.497H6.987v-2.003h2.039v2.003zm0-3.004H6.987V5.987h2.039v4.006z"}))),"caution")),(0,r.kt)("div",{parentName:"div",className:"admonition-content"},(0,r.kt)("p",{parentName:"div"},"If you get compilation errors, remember to configure your project to use JDK 11 or above."))),(0,r.kt)("p",null,"Finally, add a class with a ",(0,r.kt)("inlineCode",{parentName:"p"},"main")," method:"),(0,r.kt)("pre",null,(0,r.kt)("code",{parentName:"pre",className:"language-java"},"package com.example.myproject;\n\nimport botrino.api.Botrino;\n\npublic final class Main {\n\n    public static void main(String[] args) {\n        Botrino.run(args);\n    }\n}\n")),(0,r.kt)("div",{className:"admonition admonition-info alert alert--info"},(0,r.kt)("div",{parentName:"div",className:"admonition-heading"},(0,r.kt)("h5",{parentName:"div"},(0,r.kt)("span",{parentName:"h5",className:"admonition-icon"},(0,r.kt)("svg",{parentName:"span",xmlns:"http://www.w3.org/2000/svg",width:"14",height:"16",viewBox:"0 0 14 16"},(0,r.kt)("path",{parentName:"svg",fillRule:"evenodd",d:"M7 2.3c3.14 0 5.7 2.56 5.7 5.7s-2.56 5.7-5.7 5.7A5.71 5.71 0 0 1 1.3 8c0-3.14 2.56-5.7 5.7-5.7zM7 1C3.14 1 0 4.14 0 8s3.14 7 7 7 7-3.14 7-7-3.14-7-7-7zm1 3H6v5h2V4zm0 6H6v2h2v-2z"}))),"info")),(0,r.kt)("div",{parentName:"div",className:"admonition-content"},(0,r.kt)("p",{parentName:"div"},"If you want to include the interaction library in your project, refer to ",(0,r.kt)("a",{parentName:"p",href:"/docs/interaction-library/overview#option-1-using-botrino-framework"},"this page"),"."))),(0,r.kt)("h2",{id:"running-your-bot"},"Running your bot"),(0,r.kt)("h3",{id:"during-development"},"During development"),(0,r.kt)("p",null,"When you are developing your bot, you may prefer running the bot directly in your IDE rather than package your application every time."),(0,r.kt)("p",null,"If you used the archetype, copy the contents of ",(0,r.kt)("inlineCode",{parentName:"p"},"app/src/main/external-resources")," in a new directory on your hard drive, ",(0,r.kt)("strong",{parentName:"p"},"outside of the project workspace"),". If you aren't using the archetype, create a directory outside of your project and add a ",(0,r.kt)("inlineCode",{parentName:"p"},"config.json")," file with the following contents (insert your bot token in the ",(0,r.kt)("inlineCode",{parentName:"p"},'"token"')," field, and remove the ",(0,r.kt)("inlineCode",{parentName:"p"},'"interaction"')," field if you aren't using the interaction library):"),(0,r.kt)("pre",null,(0,r.kt)("code",{parentName:"pre",className:"language-json"},'{\n    "bot": {\n        "token": "yourTokenHere",\n        "presence": {\n            "status": "online",\n            "activity_type": "playing",\n            "activity_text": "Hello world!"\n        },\n        "enabled_intents": 32509\n    },\n    "i18n": {\n        "default_locale": "en",\n        "supported_locales": ["en"]\n    },\n    "interaction": {}\n}\n')),(0,r.kt)("p",null,"Use the tabs below depending on whether you use Eclipse or IntelliJ. If you use another IDE, it should be similar enough so you can figure out by yourself."),(0,r.kt)(l.Z,{groupId:"ide",defaultValue:"intellij",values:[{label:"IntelliJ",value:"intellij"},{label:"Eclipse",value:"eclipse"}],mdxType:"Tabs"},(0,r.kt)(p.Z,{value:"intellij",mdxType:"TabItem"},(0,r.kt)("ol",null,(0,r.kt)("li",{parentName:"ol"},(0,r.kt)("p",{parentName:"li"},"Open ",(0,r.kt)("inlineCode",{parentName:"p"},"Run")," > ",(0,r.kt)("inlineCode",{parentName:"p"},"Edit Configurations..."))),(0,r.kt)("li",{parentName:"ol"},(0,r.kt)("p",{parentName:"li"},'If you are using the archetype, it should detect a run configuration called "Main" automatically. If so, jump to step 7, otherwise continue')),(0,r.kt)("li",{parentName:"ol"},(0,r.kt)("p",{parentName:"li"},"Click ",(0,r.kt)("inlineCode",{parentName:"p"},"+")," then ",(0,r.kt)("inlineCode",{parentName:"p"},"Application"))),(0,r.kt)("li",{parentName:"ol"},(0,r.kt)("p",{parentName:"li"},"Select Java 11 (or whatever JDK 11+ you have installed)")),(0,r.kt)("li",{parentName:"ol"},(0,r.kt)("p",{parentName:"li"},'In the "Main class" field, enter the fully qualified name of the class containing the main method')),(0,r.kt)("li",{parentName:"ol"},(0,r.kt)("p",{parentName:"li"},'In the "VM options" field, copy and paste the following: ',(0,r.kt)("inlineCode",{parentName:"p"},"--add-modules=ALL-MODULE-PATH -cp . -p $MODULE_DIR$/target/dependency:$MODULE_DIR$/target/classes"))),(0,r.kt)("li",{parentName:"ol"},(0,r.kt)("p",{parentName:"li"},'In the "Working directory" field, enter the absolute path (or click the folder icon to browse) to the directory where you copied/created the configuration files earlier'))),(0,r.kt)("img",{src:(0,o.Z)("img/intellij.png"),alt:""}),(0,r.kt)("ol",{start:8},(0,r.kt)("li",{parentName:"ol"},'Click "OK" and run'))),(0,r.kt)(p.Z,{value:"eclipse",mdxType:"TabItem"},(0,r.kt)("ol",null,(0,r.kt)("li",{parentName:"ol"},(0,r.kt)("p",{parentName:"li"},"Open ",(0,r.kt)("inlineCode",{parentName:"p"},"Run")," > ",(0,r.kt)("inlineCode",{parentName:"p"},"Run Configurations..."))),(0,r.kt)("li",{parentName:"ol"},(0,r.kt)("p",{parentName:"li"},"Right click ",(0,r.kt)("inlineCode",{parentName:"p"},"Java Application")," then click ",(0,r.kt)("inlineCode",{parentName:"p"},"New Configuration"))),(0,r.kt)("li",{parentName:"ol"},(0,r.kt)("p",{parentName:"li"},'In the "Project" field, select your project containing the main class')),(0,r.kt)("li",{parentName:"ol"},(0,r.kt)("p",{parentName:"li"},'In the "Main class" field, enter the fully qualified name of the class containing the main method'))),(0,r.kt)("img",{src:(0,o.Z)("img/eclipse1.png"),alt:""}),(0,r.kt)("ol",{start:5},(0,r.kt)("li",{parentName:"ol"},(0,r.kt)("p",{parentName:"li"},'Go to the "Dependencies" tab, highlight "Classpath Entries", then click "Advanced...", select "Add External Folder", "OK", and browse to the directory where you copied/created the configuration files earlier')),(0,r.kt)("li",{parentName:"ol"},(0,r.kt)("p",{parentName:"li"},'Still in the "Dependencies" tab, find the "Add modules" dropdown and select ',(0,r.kt)("inlineCode",{parentName:"p"},"ALL-MODULE-PATH")))),(0,r.kt)("img",{src:(0,o.Z)("img/eclipse2.png"),alt:""}),(0,r.kt)("ol",{start:7},(0,r.kt)("li",{parentName:"ol"},'Click "Apply" then "Run"')))),(0,r.kt)("h3",{id:"in-a-production-environment"},"In a production environment"),(0,r.kt)("p",null,"If you aren't using the archetype, you would need to configure yourself the packaging for the production environment, including scripts to launch the bot with the correct VM arguments, etc, just like any other Java application. If you are using the archetype, you can build the JLink runtime image with the following command:"),(0,r.kt)("pre",null,(0,r.kt)("code",{parentName:"pre"},"mvn package -Dtoken=<BOT_TOKEN>\n")),(0,r.kt)("p",null,"The bot token property is not required, but saves you from manually editing the json file to insert the token later on. This command will produce a ",(0,r.kt)("inlineCode",{parentName:"p"},".zip")," file found in ",(0,r.kt)("inlineCode",{parentName:"p"},"delivery/target")," directory. You can unzip it in your production environment, and just run ",(0,r.kt)("inlineCode",{parentName:"p"},"./bin/<launcher name>"),". ",(0,r.kt)("inlineCode",{parentName:"p"},"<launcher name>")," by default corresponds to the ",(0,r.kt)("inlineCode",{parentName:"p"},"artifactId")," of your project."),(0,r.kt)("div",{className:"admonition admonition-info alert alert--info"},(0,r.kt)("div",{parentName:"div",className:"admonition-heading"},(0,r.kt)("h5",{parentName:"div"},(0,r.kt)("span",{parentName:"h5",className:"admonition-icon"},(0,r.kt)("svg",{parentName:"span",xmlns:"http://www.w3.org/2000/svg",width:"14",height:"16",viewBox:"0 0 14 16"},(0,r.kt)("path",{parentName:"svg",fillRule:"evenodd",d:"M7 2.3c3.14 0 5.7 2.56 5.7 5.7s-2.56 5.7-5.7 5.7A5.71 5.71 0 0 1 1.3 8c0-3.14 2.56-5.7 5.7-5.7zM7 1C3.14 1 0 4.14 0 8s3.14 7 7 7 7-3.14 7-7-3.14-7-7-7zm1 3H6v5h2V4zm0 6H6v2h2v-2z"}))),"info")),(0,r.kt)("div",{parentName:"div",className:"admonition-content"},(0,r.kt)("p",{parentName:"div"},"By default, the application will be attached to the current command line window, meaning the bot would disconnect if you close the terminal. You can run the application with the ",(0,r.kt)("inlineCode",{parentName:"p"},"--detached")," flag to launch the bot in the background. You can combine it with the ",(0,r.kt)("inlineCode",{parentName:"p"},"--batch-mode")," flag so that it won't ask you to press a key to exit."))),(0,r.kt)("h3",{id:"adding-system-modules-to-the-jlink-runtime-image"},"Adding system modules to the JLink runtime image"),(0,r.kt)("p",null,"If you build the bot using the JLink runtime image generated by the archetype, the runtime image will include the minimal set of Java system modules required for a Botrino application to work. If you open the parent ",(0,r.kt)("inlineCode",{parentName:"p"},"pom.xml")," and find the ",(0,r.kt)("inlineCode",{parentName:"p"},"maven-jlink-plugin")," configuration, you can see the following list of system modules:"),(0,r.kt)("pre",null,(0,r.kt)("code",{parentName:"pre",className:"language-xml"},"<addModules>\n    <addModule>java.logging</addModule>\n    <addModule>jdk.unsupported</addModule>\n    <addModule>java.xml</addModule>\n    <addModule>java.naming</addModule>\n    <addModule>java.desktop</addModule>\n    <addModule>jdk.crypto.ec</addModule>\n</addModules>\n")),(0,r.kt)("p",null,"If your application needs another module from the JDK, for example ",(0,r.kt)("inlineCode",{parentName:"p"},"java.sql"),", all you need to do is to edit this configuration and add more ",(0,r.kt)("inlineCode",{parentName:"p"},"<addModule>")," tags."))}h.isMDXComponent=!0},6010:function(e,t,n){function a(e){var t,n,i="";if("string"==typeof e||"number"==typeof e)i+=e;else if("object"==typeof e)if(Array.isArray(e))for(t=0;t<e.length;t++)e[t]&&(n=a(e[t]))&&(i&&(i+=" "),i+=n);else for(t in e)e[t]&&(i&&(i+=" "),i+=t);return i}function i(){for(var e,t,n=0,i="";n<arguments.length;)(e=arguments[n++])&&(t=a(e))&&(i&&(i+=" "),i+=t);return i}n.d(t,{Z:function(){return i}})}}]);