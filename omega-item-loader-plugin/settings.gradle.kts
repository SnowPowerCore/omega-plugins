plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "0.9.0"
}

rootProject.name = "omega-item-loader-plugin"

includeBuild("../omega-shared")

include(
    ":core",
    ":infrastructure",
    ":plugin",
)

