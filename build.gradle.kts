import fr.brouillard.oss.jgitver.Strategies.MAVEN
import java.time.Duration

plugins {
    id("io.github.gradle-nexus.publish-plugin") version "1.0.0"
    id("fr.brouillard.oss.gradle.jgitver") version "0.9.1"
}

allprojects {
    group = "org.taymyr.lagom"
    repositories {
        mavenCentral()
    }
}

jgitver {
    strategy(MAVEN)
}

nexusPublishing {
    packageGroup.set("org.taymyr")
    clientTimeout.set(Duration.ofMinutes(60))
    repositories {
        sonatype()
    }
}

tasks.wrapper {
    distributionType = Wrapper.DistributionType.ALL
}
