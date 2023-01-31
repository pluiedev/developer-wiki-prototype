// This is to suppress false warnings generated by a bug in IntelliJ
@file:Suppress("DSL_SCOPE_VIOLATION", "MISSING_DEPENDENCY_CLASS", "FUNCTION_CALL_EXPECTED", "PropertyName")

import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    groovy
    scala
    `java-gradle-plugin`
    alias(libs119.plugins.kotlin)
    `maven-publish`
}

repositories {
    mavenCentral()
}

val javaVersion = 17

tasks {
    withType<KotlinCompile> {
        kotlinOptions {
            jvmTarget = javaVersion.toString()
            // languageVersion: A.B of the kotlin plugin version A.B.C
            languageVersion = "1.8"
        }
    }
    withType<JavaCompile>().configureEach {
        options.encoding = "UTF-8"
        options.isDeprecation = true
        options.release.set(javaVersion)
    }
}

gradlePlugin {
    plugins {
        create("wiki") {
            id = "org.quiltmc.wiki-plugin"
            implementationClass = "org.quiltmc.wiki.plugin.WikiPlugin"
        }
    }
}
