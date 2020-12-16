module.exports = {
    title: 'Botrino',
    tagline: 'A simple yet powerful framework to develop, configure and run Discord bots based on Discord4J.',
    url: 'https://botrino.alex1304.com',
    baseUrl: '/',
    onBrokenLinks: 'throw',
    onBrokenMarkdownLinks: 'warn',
    favicon: 'img/favicon.ico',
    organizationName: 'Alex1304', // Usually your GitHub org/user name.
    projectName: 'botrino', // Usually your repo name.
    themeConfig: {
        colorMode: {
            defaultMode: "dark"
        },
        navbar: {
            title: 'Botrino',
            logo: {
                alt: 'Botrino Logo',
                src: 'img/logo.svg',
            },
            items: [
                {
                    to: 'docs/',
                    activeBasePath: 'docs',
                    label: 'Docs',
                    position: 'left',
                },
                //{to: 'blog', label: 'Blog', position: 'left'},
                {
                    href: 'https://github.com/Alex1304/botrino',
                    label: 'GitHub',
                    position: 'right',
                },
            ],
        },
        footer: {
            style: 'dark',
            links: [
                {
                    title: 'Docs',
                    items: [
                        {
                            label: 'Introduction',
                            to: 'docs/',
                        },
                        {
                            label: 'Getting Started',
                            to: 'docs/getting-started',
                        },
                    ],
                },
                /*{
                    title: 'Community',
                    items: [
                        {
                            label: 'Discord',
                            href: 'https://discordapp.com/invite/docusaurus',
                        },
                    ],
                },*/
                {
                    title: 'More',
                    items: [
                        /*{
                            label: 'Blog',
                            to: 'blog',
                        },*/
                        {
                            label: 'GitHub',
                            href: 'https://github.com/Alex1304/botrino',
                        },
                    ],
                },
            ],
            copyright: `Copyright © ${new Date().getFullYear()} Alexandre Miranda (Alex1304). Built with Docusaurus.`,
        },
        prism: {
            theme: require('prism-react-renderer/themes/vsDark'),
            additionalLanguages: ['java', 'groovy', 'properties']
        },
        googleAnalytics: {
            trackingID: 'G-45YBPVQ8K9'
        },
        gtag: {
            trackingID: 'G-45YBPVQ8K9'
        }
    },
    presets: [
        [
            '@docusaurus/preset-classic',
            {
                docs: {
                    sidebarPath: require.resolve('./sidebars.js'),
                    // Please change this to your repo.
                    editUrl:
                    'https://github.com/Alex1304/botrino/edit/main/website/',
                },
                /*blog: {
                    showReadingTime: true,
                    // Please change this to your repo.
                    editUrl:
                    'https://github.com/facebook/docusaurus/edit/master/website/blog/',
                },*/
                theme: {
                    customCss: require.resolve('./src/css/custom.css'),
                },
            },
        ],
    ],
};
