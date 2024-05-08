import React from 'react';
import clsx from 'clsx';
import Layout from '@theme/Layout';
import Link from '@docusaurus/Link';
import useDocusaurusContext from '@docusaurus/useDocusaurusContext';
import useBaseUrl from '@docusaurus/useBaseUrl';
import styles from './styles.module.css';

const features = [
    {
        title: 'Focused on modularity',
        imageUrl: 'img/undraw_building_blocks_n0nc.svg',
        description: (
            <>
                Botrino is a modern JDK 17+ framework utilizing Java modules to organize and
                encapsulate the components of the bot application.
            </>
        ),
    },
    {
        title: 'Powered by Discord4J',
        imageUrl: 'img/d4j.svg',
        description: (
            <>
                <Link href="https://discord4j.com">Discord4J</Link> is a modern Discord
                API wrapper for the JVM, natively supporting the reactive programming
                paradigm for the best performances at scale.
            </>
        ),
    },
    {
        title: 'Easy setup and integration',
        imageUrl: 'img/undraw_Setup_re_y9w8.svg',
        description: (
            <>
                Botrino comes with a Maven archetype allowing you to get started with a
                fully working bot in just a few minutes, and automatically configures a
                runtime package using JLink.
            </>
        ),
    },
    {
        title: 'Keep your code clean',
        imageUrl: 'img/undraw_proud_coder_7ain.svg',
        description: (
            <>
                The power of the framework resides in its ability to discover and
                auto-load classes defined in your modules. Focus on what matters: write
                one class for your command or your service, Botrino does the boring stuff
                for you.
            </>
        ),
    },
    {
        title: 'Extensible',
        imageUrl: 'img/undraw_add_file_4gfw.svg',
        description: (
            <>
                You can extend the possibilities of Botrino by implementing your own
                extensions, by expressing what to do when a class is discovered in your
                module.
            </>
        ),
    },
    {
        title: 'Translation ready',
        imageUrl: 'img/undraw_Around_the_world_re_n353.svg',
        description: (
            <>
                Want your bot to be available in multiple languages? Botrino supports
                i18n out of the box, the framework incentivizes the externalization of
                strings so they can be translated using services such as <Link
                to="https://crowdin.com/"
                target="_blank">
                Crowdin
            </Link>.
            </>
        ),
    },
];

function Feature({imageUrl, title, description}) {
    const imgUrl = useBaseUrl(imageUrl);
    return (
        <div className={clsx('col col--4', styles.feature)}>
            {imgUrl && (
                <div className="text--center">
                    <img className={styles.featureImage} src={imgUrl} alt={title}/>
                </div>
            )}
            <h3>{title}</h3>
            <p>{description}</p>
        </div>
    );
}

function Home() {
    const context = useDocusaurusContext();
    const {siteConfig = {}} = context;
    return (
        <Layout
            title="Homepage"
            description={siteConfig.tagline}>
            <header className={clsx('hero hero--primary', styles.heroBanner)}>
                <div className="container">
                    <img src={useBaseUrl('img/logo.svg')} alt="logo" width="150"/>
                    <h1 className="hero__title">{siteConfig.title}</h1>
                    <p className="hero__subtitle">{siteConfig.tagline}</p>
                    <div className={styles.buttons}>
                        <Link
                            className={clsx(
                                'button button--secondary button--lg',
                                styles.getStarted,
                            )}
                            to={useBaseUrl('docs/')}>
                            Get Started
                        </Link>
                    </div>
                </div>
            </header>
            <main>
                {features && features.length > 0 && (
                    <section className={styles.features}>
                        <div className="container">
                            <div className="row">
                                {features.map((props, idx) => (
                                    <Feature key={idx} {...props} />
                                ))}
                            </div>
                        </div>
                    </section>
                )}
            </main>
        </Layout>
    );
}

export default Home;
