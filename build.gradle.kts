// build.gradle.kts

plugins {
    java
    application
    id("com.gradleup.shadow") version "9.3.1"
}

group = "dev.rezu"
version = "1.0-SNAPSHOT"

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(21))
    }
}

repositories {
    mavenCentral()
}

dependencies {
    implementation("net.sourceforge.tess4j:tess4j:5.18.0")
    implementation("com.github.kwhat:jnativehook:2.2.2")
    implementation("org.slf4j:slf4j-api:2.0.9")
    implementation("org.slf4j:slf4j-simple:2.0.9")
    implementation("com.google.code.gson:gson:2.10.1")
}

application {
    mainClass.set("dev.rezu.Main")
}

// Configure the ShadowJar task
tasks {
    // shadowJar is created automatically by the com.gradleup.shadow plugin
    shadowJar {
        // Optional: set classifier to "" so output JAR isn't named with "-all"
        archiveClassifier.set("")

        // ⚠ If you want service merging for META-INF/services:
        mergeServiceFiles()

        // If you want the shadow JAR to run when you do `gradle build`, uncomment:
        // build { dependsOn(shadowJar) }
    }
}

tasks.compileJava {
    options.encoding = "UTF-8"
    options.release.set(21)
}