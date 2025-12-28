plugins {
    `java-library`
}

dependencies {
    api(project(":core"))

    compileOnly("org.spigotmc:spigot-api:1.20.1-R0.1-SNAPSHOT")
    compileOnly("jakarta.inject:jakarta.inject-api:2.0.1")

    implementation("com.google.code.gson:gson:2.11.0")
}
