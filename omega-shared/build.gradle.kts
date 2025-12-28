import org.gradle.api.tasks.testing.logging.TestLogEvent

plugins {
    `java-library`
}

group = "com.omega"
version = "0.1.0"

repositories {
    mavenCentral()

    // Match plugin projects: Spigot snapshots + Sonatype snapshots
    maven("https://hub.spigotmc.org/nexus/content/repositories/snapshots/")
    maven("https://oss.sonatype.org/content/repositories/snapshots")
}

dependencies {
    compileOnly("org.spigotmc:spigot-api:1.20.1-R0.1-SNAPSHOT")
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(17))
    }
    withSourcesJar()
}

tasks.withType<JavaCompile>().configureEach {
    options.encoding = "UTF-8"
}

tasks.withType<Test>().configureEach {
    testLogging {
        events = setOf(TestLogEvent.FAILED)
    }
}
