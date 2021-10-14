"use strict";(self.webpackChunkdocs=self.webpackChunkdocs||[]).push([[863],{3905:function(e,t,n){n.d(t,{Zo:function(){return d},kt:function(){return h}});var i=n(7294);function a(e,t,n){return t in e?Object.defineProperty(e,t,{value:n,enumerable:!0,configurable:!0,writable:!0}):e[t]=n,e}function o(e,t){var n=Object.keys(e);if(Object.getOwnPropertySymbols){var i=Object.getOwnPropertySymbols(e);t&&(i=i.filter((function(t){return Object.getOwnPropertyDescriptor(e,t).enumerable}))),n.push.apply(n,i)}return n}function r(e){for(var t=1;t<arguments.length;t++){var n=null!=arguments[t]?arguments[t]:{};t%2?o(Object(n),!0).forEach((function(t){a(e,t,n[t])})):Object.getOwnPropertyDescriptors?Object.defineProperties(e,Object.getOwnPropertyDescriptors(n)):o(Object(n)).forEach((function(t){Object.defineProperty(e,t,Object.getOwnPropertyDescriptor(n,t))}))}return e}function s(e,t){if(null==e)return{};var n,i,a=function(e,t){if(null==e)return{};var n,i,a={},o=Object.keys(e);for(i=0;i<o.length;i++)n=o[i],t.indexOf(n)>=0||(a[n]=e[n]);return a}(e,t);if(Object.getOwnPropertySymbols){var o=Object.getOwnPropertySymbols(e);for(i=0;i<o.length;i++)n=o[i],t.indexOf(n)>=0||Object.prototype.propertyIsEnumerable.call(e,n)&&(a[n]=e[n])}return a}var l=i.createContext({}),c=function(e){var t=i.useContext(l),n=t;return e&&(n="function"==typeof e?e(t):r(r({},t),e)),n},d=function(e){var t=c(e.components);return i.createElement(l.Provider,{value:t},e.children)},p={inlineCode:"code",wrapper:function(e){var t=e.children;return i.createElement(i.Fragment,{},t)}},m=i.forwardRef((function(e,t){var n=e.components,a=e.mdxType,o=e.originalType,l=e.parentName,d=s(e,["components","mdxType","originalType","parentName"]),m=c(n),h=a,v=m["".concat(l,".").concat(h)]||m[h]||p[h]||o;return n?i.createElement(v,r(r({ref:t},d),{},{components:n})):i.createElement(v,r({ref:t},d))}));function h(e,t){var n=arguments,a=t&&t.mdxType;if("string"==typeof e||a){var o=n.length,r=new Array(o);r[0]=m;var s={};for(var l in t)hasOwnProperty.call(t,l)&&(s[l]=t[l]);s.originalType=e,s.mdxType="string"==typeof e?e:a,r[1]=s;for(var c=2;c<o;c++)r[c]=n[c];return i.createElement.apply(null,r)}return i.createElement.apply(null,n)}m.displayName="MDXCreateElement"},3159:function(e,t,n){n.r(t),n.d(t,{frontMatter:function(){return s},metadata:function(){return l},toc:function(){return c},default:function(){return p}});var i=n(7462),a=n(3366),o=(n(7294),n(3905)),r=["components"],s={title:"Extensions"},l={unversionedId:"api/extensions",id:"api/extensions",isDocsHomePage:!1,title:"Extensions",description:"When you start your application, Botrino automatically loads all the classes present in bot modules. According to the type of classes that are discovered, an action will be performed on them such as registering a service or adding a configuration entry. Extensions allow you to hook into this module scanning process to add your own logic when classes are loaded.",source:"@site/docs/api/extensions.md",sourceDirName:"api",slug:"/api/extensions",permalink:"/docs/api/extensions",editUrl:"https://github.com/Alex1304/botrino/edit/main/website/docs/api/extensions.md",version:"current",frontMatter:{title:"Extensions"},sidebar:"someSidebar",previous:{title:"Customizing the Discord client",permalink:"/docs/api/customizing-the-discord-client"},next:{title:"Internationalization",permalink:"/docs/api/i18n"}},c=[{value:"Declaring an extension",id:"declaring-an-extension",children:[]},{value:"Implementing an extension",id:"implementing-an-extension",children:[{value:"<code>void onClassDiscovered(Class&lt;?&gt; clazz)</code>",id:"void-onclassdiscoveredclass-clazz",children:[]},{value:"<code>void onServiceCreated(Object o)</code>",id:"void-onservicecreatedobject-o",children:[]},{value:"<code>Set&lt;ServiceDescriptor&gt; provideExtraServices()</code>",id:"setservicedescriptor-provideextraservices",children:[]},{value:"<code>Set&lt;Class&lt;?&gt;&gt; provideExtraDiscoverableClasses()</code>",id:"setclass-provideextradiscoverableclasses",children:[]},{value:"<code>Mono&lt;Void&gt; finishAndJoin()</code>",id:"monovoid-finishandjoin",children:[]}]},{value:"A concrete example: the interaction library",id:"a-concrete-example-the-interaction-library",children:[]}],d={toc:c};function p(e){var t=e.components,n=(0,a.Z)(e,r);return(0,o.kt)("wrapper",(0,i.Z)({},d,n,{components:t,mdxType:"MDXLayout"}),(0,o.kt)("p",null,"When you start your application, Botrino automatically loads all the classes present in bot modules. According to the type of classes that are discovered, an action will be performed on them such as registering a service or adding a configuration entry. Extensions allow you to hook into this module scanning process to add your own logic when classes are loaded."),(0,o.kt)("h2",{id:"declaring-an-extension"},"Declaring an extension"),(0,o.kt)("p",null,"Unlike other components of the framework, extensions do not need to reside in a module annotated with ",(0,o.kt)("inlineCode",{parentName:"p"},"@BotModule"),". Think of extensions like plugins for the framework itself and not for your bot application directly. Your module does not need to be ",(0,o.kt)("inlineCode",{parentName:"p"},"open")," either, extensions are loaded via  ",(0,o.kt)("a",{parentName:"p",href:"https://docs.oracle.com/en/java/javase/11/docs/api/java.base/java/util/ServiceLoader.html"},(0,o.kt)("inlineCode",{parentName:"a"},"java.util.ServiceLoader")),"."),(0,o.kt)("p",null,"The first step is to create a class implementing the ",(0,o.kt)("inlineCode",{parentName:"p"},"BotrinoExtension")," interface:"),(0,o.kt)("pre",null,(0,o.kt)("code",{parentName:"pre",className:"language-java"},"package com.example.myproject;\n\nimport botrino.api.extension.BotrinoExtension;\nimport com.github.alex1304.rdi.config.ServiceDescriptor;\nimport reactor.core.publisher.Mono;\n\nimport java.util.Set;\n\npublic final class MyExtension implements BotrinoExtension {\n\n    @Override\n    public void onClassDiscovered(Class<?> clazz) {\n        // ...\n    }\n\n    @Override\n    public void onServiceCreated(Object o) {\n        // ...\n    }\n\n    @Override\n    public Set<ServiceDescriptor> provideExtraServices() {\n        // ...\n    }\n\n    @Override\n    public Set<Class<?>> provideExtraDiscoverableClasses() {\n        // ...\n    }\n\n    @Override\n    public Mono<Void> finishAndJoin() {\n        // ...\n    }\n}\n")),(0,o.kt)("p",null,"Before going into the details of the methods to implement, let's register this class as a provider for ",(0,o.kt)("inlineCode",{parentName:"p"},"BotrinoExtension"),". This is done via the ",(0,o.kt)("inlineCode",{parentName:"p"},"module-info.java"),":"),(0,o.kt)("pre",null,(0,o.kt)("code",{parentName:"pre",className:"language-java"},"import botrino.api.extension.BotrinoExtension;\nimport com.example.extension.MyExtension;\n\nmodule com.example.extension {\n\n    requires botrino.api;\n    provides BotrinoExtension with MyExtension;\n}\n")),(0,o.kt)("div",{className:"admonition admonition-info alert alert--info"},(0,o.kt)("div",{parentName:"div",className:"admonition-heading"},(0,o.kt)("h5",{parentName:"div"},(0,o.kt)("span",{parentName:"h5",className:"admonition-icon"},(0,o.kt)("svg",{parentName:"span",xmlns:"http://www.w3.org/2000/svg",width:"14",height:"16",viewBox:"0 0 14 16"},(0,o.kt)("path",{parentName:"svg",fillRule:"evenodd",d:"M7 2.3c3.14 0 5.7 2.56 5.7 5.7s-2.56 5.7-5.7 5.7A5.71 5.71 0 0 1 1.3 8c0-3.14 2.56-5.7 5.7-5.7zM7 1C3.14 1 0 4.14 0 8s3.14 7 7 7 7-3.14 7-7-3.14-7-7-7zm1 3H6v5h2V4zm0 6H6v2h2v-2z"}))),"info")),(0,o.kt)("div",{parentName:"div",className:"admonition-content"},(0,o.kt)("p",{parentName:"div"},"You don't ",(0,o.kt)("em",{parentName:"p"},"have")," to create a separate module just for your extension. It is totally fine to add the ",(0,o.kt)("inlineCode",{parentName:"p"},"provides")," directive directly in your ",(0,o.kt)("inlineCode",{parentName:"p"},"@BotModule"),", this example just shows that you are not required to."))),(0,o.kt)("h2",{id:"implementing-an-extension"},"Implementing an extension"),(0,o.kt)("p",null,"Let's review each of the methods of ",(0,o.kt)("inlineCode",{parentName:"p"},"BotrinoExtension")," to implement."),(0,o.kt)("h3",{id:"void-onclassdiscoveredclass-clazz"},(0,o.kt)("inlineCode",{parentName:"h3"},"void onClassDiscovered(Class<?> clazz)")),(0,o.kt)("p",null,"This is a callback method invoked each time a class is discovered in a bot module. In most cases, you will check if this class implements a specific interface or is annotated with a specific annotation, and do some processing when it is relevant to do so."),(0,o.kt)("div",{className:"admonition admonition-caution alert alert--warning"},(0,o.kt)("div",{parentName:"div",className:"admonition-heading"},(0,o.kt)("h5",{parentName:"div"},(0,o.kt)("span",{parentName:"h5",className:"admonition-icon"},(0,o.kt)("svg",{parentName:"span",xmlns:"http://www.w3.org/2000/svg",width:"16",height:"16",viewBox:"0 0 16 16"},(0,o.kt)("path",{parentName:"svg",fillRule:"evenodd",d:"M8.893 1.5c-.183-.31-.52-.5-.887-.5s-.703.19-.886.5L.138 13.499a.98.98 0 0 0 0 1.001c.193.31.53.501.886.501h13.964c.367 0 .704-.19.877-.5a1.03 1.03 0 0 0 .01-1.002L8.893 1.5zm.133 11.497H6.987v-2.003h2.039v2.003zm0-3.004H6.987V5.987h2.039v4.006z"}))),"caution")),(0,o.kt)("div",{parentName:"div",className:"admonition-content"},(0,o.kt)("p",{parentName:"div"},"If you intend to create an instance of the class, it is highly recommended to skip classes annotated with ",(0,o.kt)("inlineCode",{parentName:"p"},"@RdiService")," from this method, as they are supposed to be instantiated by the RDI container. That's why the ",(0,o.kt)("inlineCode",{parentName:"p"},"onServiceCreated(Object)")," method exists."))),(0,o.kt)("h3",{id:"void-onservicecreatedobject-o"},(0,o.kt)("inlineCode",{parentName:"h3"},"void onServiceCreated(Object o)")),(0,o.kt)("p",null,"This is a callback method invoked each time a service is created. It allows to execute some action on the service object right after it's created."),(0,o.kt)("div",{className:"admonition admonition-info alert alert--info"},(0,o.kt)("div",{parentName:"div",className:"admonition-heading"},(0,o.kt)("h5",{parentName:"div"},(0,o.kt)("span",{parentName:"h5",className:"admonition-icon"},(0,o.kt)("svg",{parentName:"span",xmlns:"http://www.w3.org/2000/svg",width:"14",height:"16",viewBox:"0 0 14 16"},(0,o.kt)("path",{parentName:"svg",fillRule:"evenodd",d:"M7 2.3c3.14 0 5.7 2.56 5.7 5.7s-2.56 5.7-5.7 5.7A5.71 5.71 0 0 1 1.3 8c0-3.14 2.56-5.7 5.7-5.7zM7 1C3.14 1 0 4.14 0 8s3.14 7 7 7 7-3.14 7-7-3.14-7-7-7zm1 3H6v5h2V4zm0 6H6v2h2v-2z"}))),"info")),(0,o.kt)("div",{parentName:"div",className:"admonition-content"},(0,o.kt)("p",{parentName:"div"},"As this method returns ",(0,o.kt)("inlineCode",{parentName:"p"},"void"),", it is not suited for performing reactive tasks. Instead, store the service object in a field and perform this task in ",(0,o.kt)("inlineCode",{parentName:"p"},"finishAndJoin()"),"."))),(0,o.kt)("h3",{id:"setservicedescriptor-provideextraservices"},(0,o.kt)("inlineCode",{parentName:"h3"},"Set<ServiceDescriptor> provideExtraServices()")),(0,o.kt)("p",null,"Even though the extension may not be inside a bot module, it is still possible to register services that will be exposed to the bot application. You can do so via this method, allowing you to provide a set of ",(0,o.kt)("a",{parentName:"p",href:"https://alex1304.github.io/rdi/docs/service-descriptors"},"RDI service descriptors"),". This method is only useful if you want to provide complex services that require writing raw descriptors (for example registering a class from a third-party library as a service with a custom name). For simple services maintained by yourself, you can use RDI annotations and make the annotated class discoverable via ",(0,o.kt)("inlineCode",{parentName:"p"},"provideExtraDiscoverableClasses()")," instead of doing it via this method."),(0,o.kt)("h3",{id:"setclass-provideextradiscoverableclasses"},(0,o.kt)("inlineCode",{parentName:"h3"},"Set<Class<?>> provideExtraDiscoverableClasses()")),(0,o.kt)("p",null,"With this method you can explicitly specify a set of classes that Botrino will pick up just like if they were inside a bot module. It is guaranteed that each class contained in the set will eventually be passed to the ",(0,o.kt)("inlineCode",{parentName:"p"},"onClassDiscovered(Class)")," method (unless they have the ",(0,o.kt)("inlineCode",{parentName:"p"},"@Exclude")," annotation). As said earlier, it can be used as an alternative way to provide extra services, if the class contained in the set is annotated with RDI annotations. It can also be used to register new configuration entries, or new things you're defining yourself in your own extension!"),(0,o.kt)("h3",{id:"monovoid-finishandjoin"},(0,o.kt)("inlineCode",{parentName:"h3"},"Mono<Void> finishAndJoin()")),(0,o.kt)("p",null,"This is the last method that is invoked during the startup sequence. It allows you to perform a task, possibly reactive, based on the classes and objects you were able to collect via previous invocations of ",(0,o.kt)("inlineCode",{parentName:"p"},"onClassDiscovered(Class)")," and ",(0,o.kt)("inlineCode",{parentName:"p"},"onServiceCreated(Object)"),'. The "join" part of this method\'s name indicates the fact that the returned reactive sequence does not need to be a finite source: you can use it to start processes living during the entire lifetime of the application, for example installing event listeners or starting a web server. The subscription to the returned ',(0,o.kt)("inlineCode",{parentName:"p"},"Mono")," is automatically cancelled once the bot disconnects, allowing the application to shut down properly."),(0,o.kt)("div",{className:"admonition admonition-warning alert alert--danger"},(0,o.kt)("div",{parentName:"div",className:"admonition-heading"},(0,o.kt)("h5",{parentName:"div"},(0,o.kt)("span",{parentName:"h5",className:"admonition-icon"},(0,o.kt)("svg",{parentName:"span",xmlns:"http://www.w3.org/2000/svg",width:"12",height:"16",viewBox:"0 0 12 16"},(0,o.kt)("path",{parentName:"svg",fillRule:"evenodd",d:"M5.05.31c.81 2.17.41 3.38-.52 4.31C3.55 5.67 1.98 6.45.9 7.98c-1.45 2.05-1.7 6.53 3.53 7.7-2.2-1.16-2.67-4.52-.3-6.61-.61 2.03.53 3.33 1.94 2.86 1.39-.47 2.3.53 2.27 1.67-.02.78-.31 1.44-1.13 1.81 3.42-.59 4.78-3.42 4.78-5.56 0-2.84-2.53-3.22-1.25-5.61-1.52.13-2.03 1.13-1.89 2.75.09 1.08-1.02 1.8-1.86 1.33-.67-.41-.66-1.19-.06-1.78C8.18 5.31 8.68 2.45 5.05.32L5.03.3l.02.01z"}))),"warning")),(0,o.kt)("div",{parentName:"div",className:"admonition-content"},(0,o.kt)("p",{parentName:"div"},"If an exception is thrown or an error is emitted via the ",(0,o.kt)("inlineCode",{parentName:"p"},"Mono")," from this method, the exception will propagate to the main thread, which will result in the bot to forcefully disconnect and the application to be terminated."))),(0,o.kt)("h2",{id:"a-concrete-example-the-interaction-library"},"A concrete example: the interaction library"),(0,o.kt)("p",null,"The ",(0,o.kt)("a",{parentName:"p",href:"/docs/interaction-library/overview"},"interaction library")," of Botrino provides an implementation of ",(0,o.kt)("inlineCode",{parentName:"p"},"BotrinoExtension"),", which is in charge of collecting the classes implementing ",(0,o.kt)("inlineCode",{parentName:"p"},"XxxInteractionListener"),", ",(0,o.kt)("inlineCode",{parentName:"p"},"InteractionErrorHandler"),", ",(0,o.kt)("inlineCode",{parentName:"p"},"InteractionEventProcessor")," and so on, in order to register them in the ",(0,o.kt)("inlineCode",{parentName:"p"},"InteractionService"),". It also exposes a new entry in ",(0,o.kt)("inlineCode",{parentName:"p"},"config.json")," that allows to construct the ",(0,o.kt)("a",{parentName:"p",href:"/docs/interaction-library/configuration"},"configuration")," object."),(0,o.kt)("p",null,"You can check the source code of the extension class of the interaction library on GitHub ",(0,o.kt)("a",{parentName:"p",href:"https://github.com/Alex1304/botrino/blob/main/interaction/src/main/java/botrino/interaction/InteractionExtension.java"},"here"),". A few things to note to understand the code:"),(0,o.kt)("ul",null,(0,o.kt)("li",{parentName:"ul"},"Classes with the ",(0,o.kt)("inlineCode",{parentName:"li"},"@RdiService")," annotation are ignored, since we want to use the instance created by RDI in case ",(0,o.kt)("inlineCode",{parentName:"li"},"XxxInteractionListener"),", ",(0,o.kt)("inlineCode",{parentName:"li"},"InteractionErrorHandler")," and ",(0,o.kt)("inlineCode",{parentName:"li"},"InteractionEventProcessor")," are declared as services."),(0,o.kt)("li",{parentName:"ul"},"An ",(0,o.kt)("inlineCode",{parentName:"li"},"InstanceCache")," is used so that the same instance can be reused in case a class implements more than one interface."),(0,o.kt)("li",{parentName:"ul"},(0,o.kt)("inlineCode",{parentName:"li"},"InteractionService")," utilizes RDI annotations, so we provide it via ",(0,o.kt)("inlineCode",{parentName:"li"},"provideExtraDiscoverableClasses()")," and not ",(0,o.kt)("inlineCode",{parentName:"li"},"provideExtraServices()"),"."),(0,o.kt)("li",{parentName:"ul"},"All implementations that were found are finally registered in the ",(0,o.kt)("inlineCode",{parentName:"li"},"finishAndJoin()")," method, which runs the interaction service at the end.")))}p.isMDXComponent=!0}}]);