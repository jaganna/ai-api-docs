plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
}

rootProject.name = "ai-api-docs"

include(":lib") // Include a library module used by the plugin
