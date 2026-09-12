import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar

plugins {
    java
    alias(libs.plugins.shadow)
}

dependencies {
    compileOnly(libs.bungeecord.api)
    implementation(libs.bundles.adventure)
}

java {
    toolchain.languageVersion.set(JavaLanguageVersion.of(libs.versions.java.get()))
}

tasks {
    withType<ShadowJar> {
        archiveClassifier.set("")
    }
    withType<JavaCompile> {
        options.encoding = "UTF-8"
        options.compilerArgs.add("-parameters")
    }
    withType<ProcessResources> {
        filesMatching("bungee.yml") {
            expand("version" to project.version)
        }
    }
    build { dependsOn(shadowJar) }
}
