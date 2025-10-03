import fr.brouillard.oss.jgitver.Strategies.MAVEN
import java.time.Duration

plugins {
    id("io.github.gradle-nexus.publish-plugin") version "2.0.0"
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
        // see https://central.sonatype.org/publish/publish-portal-ossrh-staging-api/#configuration
        sonatype {
            nexusUrl.set(uri("https://ossrh-staging-api.central.sonatype.com/service/local/"))
            snapshotRepositoryUrl.set(uri("https://central.sonatype.com/repository/maven-snapshots/"))
        }
    }
}

tasks.wrapper {
    distributionType = Wrapper.DistributionType.ALL
}
