plugins {
    `java-library`
}

dependencies {
    api(libs.updatecheckerjava)
    compileOnly(libs.spigot.api)
    compileOnly(libs.h2)
    compileOnly(libs.mysql)
    compileOnly(libs.sqlite)
    compileOnly(libs.hikari)
    compileOnly(libs.bundles.adventure)
}

java {
    toolchain.languageVersion.set(JavaLanguageVersion.of(libs.versions.java.get()))
}

tasks.withType<JavaCompile>().configureEach {
    options.encoding = "UTF-8"
    options.compilerArgs.add("-parameters")
}
