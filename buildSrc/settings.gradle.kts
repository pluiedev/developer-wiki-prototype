pluginManagement {
    repositories {
        gradlePluginPortal()
        mavenCentral()
    }
}
dependencyResolutionManagement {
    versionCatalogs {
        create("libs119") {
            from(files("../gradle/1.19.versions.toml"))
        }
    }
}