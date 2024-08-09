import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar

plugins {
    id("java")
    id("com.github.johnrengelman.shadow") version "8.1.1"
    id("xyz.jpenilla.run-paper") version "2.3.0"
}

group = "com.armamc"
version = "1.2.1"

repositories {
    mavenCentral()
    maven("https://jitpack.io")
    maven("https://hub.spigotmc.org/nexus/content/repositories/snapshots/")
}

dependencies {
    implementation("com.github.TechnicJelle:UpdateCheckerJava:v2.5")
    compileOnly(dependencyNotation = "org.spigotmc:spigot-api:1.21.1-R0.1-SNAPSHOT")
    compileOnly(dependencyNotation = "net.kyori:adventure-api:4.17.0")
    compileOnly(dependencyNotation = "net.kyori:adventure-platform-bukkit:4.3.4")
    compileOnly(dependencyNotation = "net.kyori:adventure-text-minimessage:4.17.0")
    compileOnly(dependencyNotation = "net.kyori:adventure-text-serializer-legacy:4.17.0")
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

    build {
        dependsOn(shadowJar)
    }

    runServer {
        minecraftVersion("1.20.4")
        jvmArguments.add("-Dcom.mojang.eula.agree=true")
        jvmArguments.add("-Dnet.kyori.ansi.colorLevel=truecolor")
        jvmArguments.add("-Dfile.encoding=UTF8")
        systemProperty("terminal.jline", false)
        systemProperty("terminal.ansi", true)
    }
}
