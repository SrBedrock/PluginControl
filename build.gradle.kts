import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar

plugins {
    id("java")
    alias(libs.plugins.shadow)
    alias(libs.plugins.run.paper)
}

group = "com.armamc"
version = "1.3.0"

repositories {
    mavenCentral()
    maven("https://jitpack.io")
    maven("https://hub.spigotmc.org/nexus/content/repositories/snapshots/")
}

dependencies {
    implementation(libs.updatecheckerjava)
    compileOnly(libs.h2)
    compileOnly(libs.mysql)
    compileOnly(libs.sqlite)
    compileOnly(libs.spigot.api)
    compileOnly(libs.bundles.adventure)
}

java {
    toolchain.languageVersion.set(JavaLanguageVersion.of(libs.versions.java.get()))
}

tasks {
    withType<ShadowJar> {
        archiveClassifier.set("")
    }

    withType<ProcessResources> {
        val props = mapOf(
            "version" to project.version,
            "adventure" to libs.versions.adventure.api.get(),
            "platform" to libs.versions.adventure.platform.bukkit.get(),
            "h2" to libs.versions.h2.get(),
            "mysql" to libs.versions.mysql.get(),
            "sqlite" to libs.versions.sqlite.get()
        )
        filesMatching("plugin.yml") {
            expand(props)
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
        minecraftVersion("1.21.10")
        jvmArguments.add("-Dcom.mojang.eula.agree=true")
        jvmArguments.add("-Dnet.kyori.ansi.colorLevel=truecolor")
        jvmArguments.add("-Dfile.encoding=UTF8")
        systemProperty("terminal.jline", false)
        systemProperty("terminal.ansi", true)
    }
}