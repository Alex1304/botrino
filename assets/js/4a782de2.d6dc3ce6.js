"use strict";(self.webpackChunkdocs=self.webpackChunkdocs||[]).push([[884],{7540:(e,n,t)=>{t.r(n),t.d(n,{assets:()=>d,contentTitle:()=>a,default:()=>h,frontMatter:()=>r,metadata:()=>c,toc:()=>s});var i=t(4848),o=t(8453);const r={title:"Acknowledging interactions"},a=void 0,c={id:"interaction-library/acknowledging-interactions",title:"Acknowledging interactions",description:"When an interaction event is received from the gateway, the library is able to acknowledge them automatically. This",source:"@site/docs/interaction-library/acknowledging-interactions.mdx",sourceDirName:"interaction-library",slug:"/interaction-library/acknowledging-interactions",permalink:"/docs/interaction-library/acknowledging-interactions",draft:!1,unlisted:!1,editUrl:"https://github.com/Alex1304/botrino/edit/main/website/docs/interaction-library/acknowledging-interactions.mdx",tags:[],version:"current",frontMatter:{title:"Acknowledging interactions"},sidebar:"someSidebar",previous:{title:"Creating commands",permalink:"/docs/interaction-library/creating-commands"},next:{title:"Dealing with components",permalink:"/docs/interaction-library/dealing-with-components"}},d={},s=[{value:"Modifying the default acknowledgment behavior",id:"modifying-the-default-acknowledgment-behavior",level:2},{value:"Overriding the acknowledgment mode on a per-command basis",id:"overriding-the-acknowledgment-mode-on-a-per-command-basis",level:2}];function l(e){const n={a:"a",admonition:"admonition",code:"code",h2:"h2",p:"p",pre:"pre",table:"table",tbody:"tbody",td:"td",th:"th",thead:"thead",tr:"tr",...(0,o.R)(),...e.components};return(0,i.jsxs)(i.Fragment,{children:[(0,i.jsxs)(n.p,{children:["When an interaction event is received from the gateway, the library is able to acknowledge them automatically. This\nallows to simplify your code a lot, as you can directly use ",(0,i.jsx)(n.code,{children:"createFollowup()"})," or ",(0,i.jsx)(n.code,{children:"editReply()"})," without worrying about\nchoosing between ",(0,i.jsx)(n.code,{children:"reply()"}),",  ",(0,i.jsx)(n.code,{children:"edit()"}),", ",(0,i.jsx)(n.code,{children:"deferReply()"})," or ",(0,i.jsx)(n.code,{children:"deferEdit()"})," first. If you have a specific use case that\nrequires you to take full control over the acknowledgment process, the library gets you covered by offering a way to\ndisable automatic acknowledgment on a per-command basis."]}),"\n",(0,i.jsx)(n.h2,{id:"modifying-the-default-acknowledgment-behavior",children:"Modifying the default acknowledgment behavior"}),"\n",(0,i.jsxs)(n.p,{children:["This was partially covered in the ",(0,i.jsx)(n.a,{href:"/docs/interaction-library/configuration",children:"Configuration"})," page, the default behavior can be set via\nthe ",(0,i.jsx)(n.code,{children:"default_ack_mode"})," field of ",(0,i.jsx)(n.code,{children:"config.json"})," if you are using the Botrino framework, or\nvia ",(0,i.jsx)(n.code,{children:"InteractionConfig.Builder#defaultACKMode(String)"})," when building the configuration manually. Here's a table\ndescribing the possible values and their behavior:"]}),"\n",(0,i.jsxs)(n.table,{children:[(0,i.jsx)(n.thead,{children:(0,i.jsxs)(n.tr,{children:[(0,i.jsx)(n.th,{children:"value"}),(0,i.jsx)(n.th,{children:"behavior"})]})}),(0,i.jsxs)(n.tbody,{children:[(0,i.jsxs)(n.tr,{children:[(0,i.jsx)(n.td,{children:(0,i.jsx)(n.code,{children:"default"})}),(0,i.jsxs)(n.td,{children:["Equivalent to ",(0,i.jsx)(n.code,{children:"defer"}),"."]})]}),(0,i.jsxs)(n.tr,{children:[(0,i.jsx)(n.td,{children:(0,i.jsx)(n.code,{children:"defer"})}),(0,i.jsxs)(n.td,{children:["Automatically calls ",(0,i.jsx)(n.code,{children:"deferReply()"})," (for commands) or ",(0,i.jsx)(n.code,{children:"deferEdit()"})," (for components)."]})]}),(0,i.jsxs)(n.tr,{children:[(0,i.jsx)(n.td,{children:(0,i.jsx)(n.code,{children:"defer_ephemeral"})}),(0,i.jsxs)(n.td,{children:["Automatically calls ",(0,i.jsx)(n.code,{children:"deferReply().withEphemeral(true)"})," (for commands) or ",(0,i.jsx)(n.code,{children:"deferEdit().withEphemeral(true)"})," (for components)."]})]}),(0,i.jsxs)(n.tr,{children:[(0,i.jsx)(n.td,{children:(0,i.jsx)(n.code,{children:"none"})}),(0,i.jsx)(n.td,{children:"Does not call any acknowledgment method."})]})]})]}),"\n",(0,i.jsx)(n.h2,{id:"overriding-the-acknowledgment-mode-on-a-per-command-basis",children:"Overriding the acknowledgment mode on a per-command basis"}),"\n",(0,i.jsxs)(n.p,{children:["Let's say you have ",(0,i.jsx)(n.code,{children:"defer"})," as default behavior in your config, and you want to make a command that replies exclusively\nwith ephemeral messages. There would be no way to achieve this without overriding the acknowledgment behavior for this\nspecific command so that it can be ephemeral. This is as simple as adding an ",(0,i.jsx)(n.code,{children:"@Acknowledge"})," annotation with the desired\nmode as value:"]}),"\n",(0,i.jsx)(n.pre,{children:(0,i.jsx)(n.code,{className:"language-java",metastring:"{9}",children:'package testbot1;\n\nimport botrino.interaction.annotation.Acknowledge;\nimport botrino.interaction.annotation.ChatInputCommand;\nimport botrino.interaction.listener.ChatInputInteractionListener;\nimport botrino.interaction.context.ChatInputInteractionContext;\nimport org.reactivestreams.Publisher;\n\n@Acknowledge(Acknowledge.Mode.DEFER_EPHEMERAL)\n@ChatInputCommand(name = "ping", description = "Pings the bot to check if it is alive.")\npublic final class PingCommand implements ChatInputInteractionListener {\n\n    @Override\n    public Publisher<?> run(ChatInputInteractionContext ctx) {\n        return ctx.event().createFollowup("Pong!").withEphemeral(true);\n    }\n}\n'})}),"\n",(0,i.jsxs)(n.p,{children:["Since this is a very simple command, you could even completely disable automatic acknowledgment and use ",(0,i.jsx)(n.code,{children:"reply()"}),"\ninstead of ",(0,i.jsx)(n.code,{children:"createFollowup()"}),":"]}),"\n",(0,i.jsx)(n.pre,{children:(0,i.jsx)(n.code,{className:"language-java",children:'package testbot1;\n\nimport botrino.interaction.annotation.Acknowledge;\nimport botrino.interaction.annotation.ChatInputCommand;\nimport botrino.interaction.listener.ChatInputInteractionListener;\nimport botrino.interaction.context.ChatInputInteractionContext;\nimport org.reactivestreams.Publisher;\n\n@Acknowledge(Acknowledge.Mode.NONE)\n@ChatInputCommand(name = "ping", description = "Pings the bot to check if it is alive.")\npublic final class PingCommand implements ChatInputInteractionListener {\n\n    @Override\n    public Publisher<?> run(ChatInputInteractionContext ctx) {\n        return ctx.event().reply("Pong!").withEphemeral(true);\n    }\n}\n'})}),"\n",(0,i.jsx)(n.admonition,{type:"warning",children:(0,i.jsxs)(n.p,{children:["If your command is made of subcommands or subcommand groups, the ",(0,i.jsx)(n.code,{children:"@Acknowledge"})," annotation must be used on the listener\nimplementation class of individual subcommands; putting it on the parent class alongside ",(0,i.jsx)(n.code,{children:"@ChatInputCommand"})," will have\nno effect."]})})]})}function h(e={}){const{wrapper:n}={...(0,o.R)(),...e.components};return n?(0,i.jsx)(n,{...e,children:(0,i.jsx)(l,{...e})}):l(e)}},8453:(e,n,t)=>{t.d(n,{R:()=>a,x:()=>c});var i=t(6540);const o={},r=i.createContext(o);function a(e){const n=i.useContext(r);return i.useMemo((function(){return"function"==typeof e?e(n):{...n,...e}}),[n,e])}function c(e){let n;return n=e.disableParentContext?"function"==typeof e.components?e.components(o):e.components||o:a(e.components),i.createElement(r.Provider,{value:n},e.children)}}}]);