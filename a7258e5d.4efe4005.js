(window.webpackJsonp=window.webpackJsonp||[]).push([[14],{83:function(e,n,t){"use strict";t.r(n),t.d(n,"frontMatter",(function(){return o})),t.d(n,"metadata",(function(){return c})),t.d(n,"rightToc",(function(){return s})),t.d(n,"default",(function(){return p}));var r=t(3),i=t(8),a=(t(0),t(94)),o={title:"Privileges"},c={unversionedId:"command-extension/privileges",id:"command-extension/privileges",isDocsHomePage:!1,title:"Privileges",description:"Another common use case when making commands is to be able to restrict access to some commands that should not be used by everyone. The Privilege API provides a way to conveniently implement these kind of restrictions.",source:"@site/docs/command-extension/privileges.md",slug:"/command-extension/privileges",permalink:"/docs/command-extension/privileges",editUrl:"https://github.com/Alex1304/botrino/edit/main/website/docs/command-extension/privileges.md",version:"current",sidebar:"someSidebar",previous:{title:"Subcommands",permalink:"/docs/command-extension/subcommands"},next:{title:"Cooldowns",permalink:"/docs/command-extension/cooldowns"}},s=[{value:"The <code>Privilege</code> interface",id:"the-privilege-interface",children:[]},{value:"Privilege presets",id:"privilege-presets",children:[]},{value:"Composing privileges",id:"composing-privileges",children:[]}],l={rightToc:s};function p(e){var n=e.components,t=Object(i.a)(e,["components"]);return Object(a.b)("wrapper",Object(r.a)({},l,t,{components:n,mdxType:"MDXLayout"}),Object(a.b)("p",null,"Another common use case when making commands is to be able to restrict access to some commands that should not be used by everyone. The Privilege API provides a way to conveniently implement these kind of restrictions."),Object(a.b)("h2",{id:"the-privilege-interface"},"The ",Object(a.b)("inlineCode",{parentName:"h2"},"Privilege")," interface"),Object(a.b)("p",null,Object(a.b)("inlineCode",{parentName:"p"},"Privilege")," is a functional interface that is in charge of checking if access to the command is granted for a specific context. If granted, the abstract method of the interface returns a ",Object(a.b)("inlineCode",{parentName:"p"},"Mono")," that completes empty, which signals that the command can be run normally. If not granted, the returned ",Object(a.b)("inlineCode",{parentName:"p"},"Mono")," is expected to emit ",Object(a.b)("inlineCode",{parentName:"p"},"PrivilegeException")," (or a subclass of this exception), possibly carrying details on the reason of the failure. In this case, the command execution will be cancelled."),Object(a.b)("p",null,"An instance of this interface can be provided via a lambda expression, and can be attached to a command by overriding the ",Object(a.b)("inlineCode",{parentName:"p"},"privilege()")," method of ",Object(a.b)("inlineCode",{parentName:"p"},"Command")," (or the corresponding method in ",Object(a.b)("inlineCode",{parentName:"p"},"Command.Builder"),'). The example below defines a privilege which only grants users whose username starts with "A":'),Object(a.b)("pre",null,Object(a.b)("code",Object(r.a)({parentName:"pre"},{className:"language-java"}),'@Override\npublic Privilege privilege() {\n    return ctx -> Mono.justOrEmpty(ctx.event().getMessage().getAuthor())\n            .filter(author -> author.getUsername().startsWith("A"))\n            .switchIfEmpty(Mono.error(PrivilegeException::new))\n            .then();\n}\n')),Object(a.b)("div",{className:"admonition admonition-info alert alert--info"},Object(a.b)("div",Object(r.a)({parentName:"div"},{className:"admonition-heading"}),Object(a.b)("h5",{parentName:"div"},Object(a.b)("span",Object(r.a)({parentName:"h5"},{className:"admonition-icon"}),Object(a.b)("svg",Object(r.a)({parentName:"span"},{xmlns:"http://www.w3.org/2000/svg",width:"14",height:"16",viewBox:"0 0 14 16"}),Object(a.b)("path",Object(r.a)({parentName:"svg"},{fillRule:"evenodd",d:"M7 2.3c3.14 0 5.7 2.56 5.7 5.7s-2.56 5.7-5.7 5.7A5.71 5.71 0 0 1 1.3 8c0-3.14 2.56-5.7 5.7-5.7zM7 1C3.14 1 0 4.14 0 8s3.14 7 7 7 7-3.14 7-7-3.14-7-7-7zm1 3H6v5h2V4zm0 6H6v2h2v-2z"})))),"info")),Object(a.b)("div",Object(r.a)({parentName:"div"},{className:"admonition-content"}),Object(a.b)("p",{parentName:"div"},"Handling ",Object(a.b)("inlineCode",{parentName:"p"},"PrivilegeException")," (for example to display a user-friendly message) is documented on the ",Object(a.b)("a",Object(r.a)({parentName:"p"},{href:"/docs/command-extension/handling-errors"}),"Handling Errors")," page."))),Object(a.b)("h2",{id:"privilege-presets"},"Privilege presets"),Object(a.b)("p",null,"In most cases, checking if access to a command is granted will simply consist of checking if the user has a particular role or a particular permission in the guild. You can use one of the static methods of the ",Object(a.b)("inlineCode",{parentName:"p"},"Privileges")," class instead of implementing that yourself:"),Object(a.b)("pre",null,Object(a.b)("code",Object(r.a)({parentName:"pre"},{className:"language-java"}),"@Override\npublic Privilege privilege() {\n    return Privileges.checkPermissions(perms -> perms.contains(ADMINISTRATOR));\n}\n")),Object(a.b)("p",null,"Check out the ",Object(a.b)("a",Object(r.a)({parentName:"p"},{href:"https://javadoc.io/doc/com.alex1304.botrino/botrino-command/latest/botrino.command/botrino/command/privilege/Privileges.html"}),"Javadoc for the ",Object(a.b)("inlineCode",{parentName:"a"},"Privileges")," class")," for more presets like this one."),Object(a.b)("h2",{id:"composing-privileges"},"Composing privileges"),Object(a.b)("p",null,"You can compose several ",Object(a.b)("inlineCode",{parentName:"p"},"Privilege")," instances by using the ",Object(a.b)("inlineCode",{parentName:"p"},"and()")," and ",Object(a.b)("inlineCode",{parentName:"p"},"or()")," methods:"),Object(a.b)("pre",null,Object(a.b)("code",Object(r.a)({parentName:"pre"},{className:"language-java"}),"@Override\npublic Privilege privilege() {\n    return Privileges.checkRoles(roles -> !roles.isEmpty())\n            .or(Privileges.guildOwner());\n}\n")),Object(a.b)("p",null,'This code means "Grant if the user has at least one role OR if they are the owner of the server".'))}p.isMDXComponent=!0},94:function(e,n,t){"use strict";t.d(n,"a",(function(){return m})),t.d(n,"b",(function(){return u}));var r=t(0),i=t.n(r);function a(e,n,t){return n in e?Object.defineProperty(e,n,{value:t,enumerable:!0,configurable:!0,writable:!0}):e[n]=t,e}function o(e,n){var t=Object.keys(e);if(Object.getOwnPropertySymbols){var r=Object.getOwnPropertySymbols(e);n&&(r=r.filter((function(n){return Object.getOwnPropertyDescriptor(e,n).enumerable}))),t.push.apply(t,r)}return t}function c(e){for(var n=1;n<arguments.length;n++){var t=null!=arguments[n]?arguments[n]:{};n%2?o(Object(t),!0).forEach((function(n){a(e,n,t[n])})):Object.getOwnPropertyDescriptors?Object.defineProperties(e,Object.getOwnPropertyDescriptors(t)):o(Object(t)).forEach((function(n){Object.defineProperty(e,n,Object.getOwnPropertyDescriptor(t,n))}))}return e}function s(e,n){if(null==e)return{};var t,r,i=function(e,n){if(null==e)return{};var t,r,i={},a=Object.keys(e);for(r=0;r<a.length;r++)t=a[r],n.indexOf(t)>=0||(i[t]=e[t]);return i}(e,n);if(Object.getOwnPropertySymbols){var a=Object.getOwnPropertySymbols(e);for(r=0;r<a.length;r++)t=a[r],n.indexOf(t)>=0||Object.prototype.propertyIsEnumerable.call(e,t)&&(i[t]=e[t])}return i}var l=i.a.createContext({}),p=function(e){var n=i.a.useContext(l),t=n;return e&&(t="function"==typeof e?e(n):c(c({},n),e)),t},m=function(e){var n=p(e.components);return i.a.createElement(l.Provider,{value:n},e.children)},d={inlineCode:"code",wrapper:function(e){var n=e.children;return i.a.createElement(i.a.Fragment,{},n)}},b=i.a.forwardRef((function(e,n){var t=e.components,r=e.mdxType,a=e.originalType,o=e.parentName,l=s(e,["components","mdxType","originalType","parentName"]),m=p(t),b=r,u=m["".concat(o,".").concat(b)]||m[b]||d[b]||a;return t?i.a.createElement(u,c(c({ref:n},l),{},{components:t})):i.a.createElement(u,c({ref:n},l))}));function u(e,n){var t=arguments,r=n&&n.mdxType;if("string"==typeof e||r){var a=t.length,o=new Array(a);o[0]=b;var c={};for(var s in n)hasOwnProperty.call(n,s)&&(c[s]=n[s]);c.originalType=e,c.mdxType="string"==typeof e?e:r,o[1]=c;for(var l=2;l<a;l++)o[l]=t[l];return i.a.createElement.apply(null,o)}return i.a.createElement.apply(null,t)}b.displayName="MDXCreateElement"}}]);