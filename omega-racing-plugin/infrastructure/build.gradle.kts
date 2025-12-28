plugins {
    java
}

dependencies {
    implementation(project(":core"))

    compileOnly("org.spigotmc:spigot-api:1.20.1-R0.1-SNAPSHOT")

    implementation("com.google.code.gson:gson:2.11.0")
    implementation("jakarta.inject:jakarta.inject-api:2.0.1")
}

java {
    withSourcesJar()
}
