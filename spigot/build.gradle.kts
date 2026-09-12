import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar

plugins {
    java
    alias(libs.plugins.shadow)
}

dependencies {
    implementation(project(":common"))
    compileOnly(libs.spigot.api)
}

tasks {
    withType<ShadowJar> {
        archiveClassifier.set("")
    }
    withType<ProcessResources> {
        from(project(":common").sourceSets.main.get().resources)
        val props = mapOf(
            "version" to project.version,
            "adventure" to libs.versions.adventure.api.get(),
            "platform" to libs.versions.adventure.platform.bukkit.get(),
            "h2" to libs.versions.h2.get(),
            "mysql" to libs.versions.mysql.get(),
            "sqlite" to libs.versions.sqlite.get(),
            "hikari" to libs.versions.hikari.get()
        )
        filesMatching("plugin.yml") { expand(props) }
    }
    build { dependsOn(shadowJar) }
}
