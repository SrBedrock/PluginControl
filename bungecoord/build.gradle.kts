import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar

plugins {
    java
    alias(libs.plugins.shadow)
}

dependencies {
    implementation(project(":common"))
    compileOnly(libs.bungeecord.api)
    implementation(libs.adventure.api)
    implementation(libs.adventure.minimessage)
    implementation(libs.adventure.serializer.legacy)
}

java {
    toolchain.languageVersion.set(JavaLanguageVersion.of(libs.versions.java.get()))
}

tasks {
    withType<ShadowJar> {
        archiveClassifier.set("")
        duplicatesStrategy = DuplicatesStrategy.EXCLUDE
        relocate("org.yaml.snakeyaml", "com.armamc.plugincontrol.libs.snakeyaml")
        relocate("com.zaxxer.hikari", "com.armamc.plugincontrol.libs.hikari")
        relocate("net.kyori", "com.armamc.plugincontrol.libs.kyori")
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
