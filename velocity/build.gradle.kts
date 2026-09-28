import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar

plugins {
    java
    alias(libs.plugins.shadow)
}

dependencies {
    implementation(project(":common"))
    compileOnly(libs.velocity.api)
    implementation(libs.adventure.minimessage)
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
    }
    withType<JavaCompile> {
        options.encoding = "UTF-8"
        options.compilerArgs.add("-parameters")
    }
    build { dependsOn(shadowJar) }
}
