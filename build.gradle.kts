import java.time.Duration

val ossrhUsername: String? by project
val ossrhPassword: String? by project
val projectVersion: String by project

plugins {
    id("io.github.gradle-nexus.publish-plugin") version "1.0.0"
}

allprojects {
    group = "org.taymyr.lagom"
    version = projectVersion
    repositories {
        mavenCentral()
        jcenter()
    }
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
