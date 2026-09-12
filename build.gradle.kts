plugins {
    base
}

allprojects {
    group = "com.armamc"
    version = "1.3.0"

    repositories {
        mavenCentral()
        maven("https://jitpack.io")
        maven("https://hub.spigotmc.org/nexus/content/repositories/snapshots/")
        maven("https://repo.papermc.io/repository/maven-public/")
        maven("https://repo.md-5.net/content/groups/public/")
    }
}
