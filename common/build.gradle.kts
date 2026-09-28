plugins {
    `java-library`
}

dependencies {
    implementation("org.yaml:snakeyaml:2.7")
    implementation(libs.hikari)
    runtimeOnly(libs.h2)
    testRuntimeOnly(libs.h2)
    runtimeOnly(libs.mysql)
    runtimeOnly(libs.sqlite)
    testImplementation("org.junit.jupiter:junit-jupiter:6.1.3")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher:6.1.3")
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
