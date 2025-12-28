plugins {
    java
    id("com.github.johnrengelman.shadow") version "8.1.1"
}

base {
    archivesName.set("OmegaInteractable")
}

dependencies {
    implementation(project(":core"))
    implementation(project(":infrastructure"))

    compileOnly("org.spigotmc:spigot-api:1.20.1-R0.1-SNAPSHOT")

    // Compile against OmegaRacing's shared API/classes directly (no reflection).
    // This is compileOnly so the classes are NOT embedded into OmegaInteractable's jar,
    // avoiding classloader type-identity issues at runtime.
    compileOnly(files("../../omega-racing-plugin/core/build/libs/OmegaRacingCore-0.1.0.jar"))

    // Compile against OmegaInvHolder + OmegaItemLoader core APIs (no reflection).
    // compileOnly ensures these classes are NOT embedded into OmegaInteractable's jar,
    // avoiding classloader type-identity issues at runtime.
    compileOnly(files("../../omega-inv-holder-plugin/core/build/libs/OmegaInvHolderCore-0.1.0.jar"))
    compileOnly(files("../../omega-item-loader-plugin/core/build/libs/OmegaItemLoaderCore-0.1.0.jar"))

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

        relocate("com.google.gson", "com.omega.interactable.libs.gson")
        relocate("com.google.inject", "com.omega.interactable.libs.guice")
    }

    // Best-effort: build core API jars before compilation (dev convenience).
    val buildRacingCore by registering(Exec::class) {
        val dir = file("../../omega-racing-plugin")
        workingDir = dir

        val isWindows = System.getProperty("os.name").lowercase().contains("windows")
        if (isWindows) {
            commandLine("cmd", "/c", "gradlew.bat", ":core:jar")
        } else {
            commandLine("./gradlew", ":core:jar")
        }
    }

    val buildInvHolderCore by registering(Exec::class) {
        val dir = file("../../omega-inv-holder-plugin")
        workingDir = dir

        val isWindows = System.getProperty("os.name").lowercase().contains("windows")
        if (isWindows) {
            commandLine("cmd", "/c", "gradlew.bat", ":core:jar")
        } else {
            commandLine("./gradlew", ":core:jar")
        }
    }

    val buildItemLoaderCore by registering(Exec::class) {
        val dir = file("../../omega-item-loader-plugin")
        workingDir = dir

        val isWindows = System.getProperty("os.name").lowercase().contains("windows")
        if (isWindows) {
            commandLine("cmd", "/c", "gradlew.bat", ":core:jar")
        } else {
            commandLine("./gradlew", ":core:jar")
        }
    }

    withType<JavaCompile>().configureEach {
        dependsOn(buildRacingCore)
        dependsOn(buildInvHolderCore)
        dependsOn(buildItemLoaderCore)
    }

    build {
        dependsOn(shadowJar)
    }
}
