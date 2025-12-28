plugins {
    java
    id("com.github.johnrengelman.shadow") version "8.1.1"
}

base {
    archivesName.set("OmegaRacing")
}

dependencies {
    implementation(project(":core"))
    implementation(project(":infrastructure"))

    compileOnly("org.spigotmc:spigot-api:1.20.1-R0.1-SNAPSHOT")

    implementation("com.google.inject:guice:7.0.0")
    implementation("jakarta.inject:jakarta.inject-api:2.0.1")
}

java {
    withSourcesJar()
}

tasks {
    jar {
        enabled = false
    }

    shadowJar {
        archiveClassifier.set("")

        relocate("com.google.gson", "com.omega.racing.libs.gson")
        relocate("com.google.inject", "com.omega.racing.libs.guice")
    }

    build {
        dependsOn(shadowJar)
    }
}
