rootProject.name = "developer-wiki"

pluginManagement {
    repositories {
        gradlePluginPortal()
        maven("https://maven.quiltmc.org/repository/release") {
            name = "Quilt"
        }
        maven("https://maven.fabricmc.net/") {
            name = "Fabric"
        }
    }
}

dependencyResolutionManagement {
    versionCatalogs {
        create("libs119") {
            from(files("gradle/1.19.versions.toml"))
        }
    }
}

include("tutorial-1.19")