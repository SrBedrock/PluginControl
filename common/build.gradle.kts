plugins {
    `java-library`
}

dependencies {
    implementation("org.yaml:snakeyaml:2.4")
    implementation(libs.hikari)
    runtimeOnly(libs.h2)
    testRuntimeOnly(libs.h2)
    runtimeOnly(libs.mysql)
    runtimeOnly(libs.sqlite)
    testImplementation("org.junit.jupiter:junit-jupiter:5.11.4")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher:1.11.4")
}

java {
    toolchain.languageVersion.set(JavaLanguageVersion.of(libs.versions.java.get()))
}

tasks.withType<JavaCompile>().configureEach {
    options.encoding = "UTF-8"
}
tasks.test {
    useJUnitPlatform()
}
