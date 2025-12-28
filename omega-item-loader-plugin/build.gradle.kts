import org.gradle.api.tasks.testing.logging.TestLogEvent

plugins {
    base
}

allprojects {
    group = "com.omega"
    version = "0.1.0"

    repositories {
        mavenCentral()
        maven("https://hub.spigotmc.org/nexus/content/repositories/snapshots/")
        maven("https://oss.sonatype.org/content/repositories/snapshots")
        maven("https://repo.dmulloy2.net/repository/public/")
    }
}

subprojects {
    plugins.withType<JavaPlugin> {
        the<JavaPluginExtension>().toolchain {
            languageVersion.set(JavaLanguageVersion.of(17))
        }

        dependencies {
            add("implementation", "com.omega:omega-shared:$version")
        }

        tasks.withType<JavaCompile>().configureEach {
            options.encoding = "UTF-8"
        }

        tasks.withType<Test>().configureEach {
            testLogging {
                events = setOf(TestLogEvent.FAILED)
            }
        }
    }
}
