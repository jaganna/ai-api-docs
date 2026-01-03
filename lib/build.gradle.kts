plugins {
    kotlin("jvm")
    `java-library`
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(21))
    }
}

repositories {
    mavenCentral()
}

dependencies {
    api(kotlin("stdlib"))

    // Use JUnit Jupiter (JUnit 5) for tests. Add engine at runtime.
    testImplementation("org.junit.jupiter:junit-jupiter:5.10.0")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.10.0")

    // Ensure JUnit Platform launcher is available on the test runtime classpath for Gradle
    testImplementation("org.junit.platform:junit-platform-launcher:1.10.0")
}

// Configure tests to use JUnit Platform (JUnit 5)
@Suppress("UNUSED_VARIABLE")
val test by tasks.getting(Test::class) {
    useJUnitPlatform()
}

// Keep consistent with root project versioning
version = rootProject.version
