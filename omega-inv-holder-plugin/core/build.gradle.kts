plugins {
    java
}

base {
    archivesName.set("OmegaInvHolderCore")
}

dependencies {
    compileOnly("org.spigotmc:spigot-api:1.20.1-R0.1-SNAPSHOT")
}

java {
    withSourcesJar()
}
