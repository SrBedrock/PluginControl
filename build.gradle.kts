import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar

plugins {
    id("java")
    id("com.github.johnrengelman.shadow") version "8.1.1"
}

group = "com.armamc"
version = "1.1.0"

repositories {
    mavenCentral()
    maven("https://hub.spigotmc.org/nexus/content/repositories/snapshots/")
}

dependencies {
    compileOnly(dependencyNotation = "org.spigotmc:spigot-api:1.20.1-R0.1-SNAPSHOT")
    compileOnly(dependencyNotation = "net.kyori:adventure-api:4.14.0")
    compileOnly(dependencyNotation = "net.kyori:adventure-platform-bukkit:4.3.0")
    compileOnly(dependencyNotation = "net.kyori:adventure-text-minimessage:4.14.0")
    compileOnly(dependencyNotation = "net.kyori:adventure-text-serializer-legacy:4.14.0")

}

java {
    toolchain.languageVersion.set(JavaLanguageVersion.of(17))
}

tasks {
    withType<ShadowJar> {
        archiveClassifier.set("")
    }

    withType<ProcessResources> {
        filesMatching("plugin.yml") {
            expand("version" to project.version)
        }
    }

    withType<JavaCompile> {
        options.encoding = "UTF-8"
        options.compilerArgs.add("-parameters")
    }

    shadowJar {
        minimize()
    }

    build {
        dependsOn(shadowJar)
    }
}