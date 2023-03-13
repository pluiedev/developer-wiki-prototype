package org.quiltmc.wiki.plugin

enum class Language(val id: String, val displayName: String, val ext: String = id) {
    Java("java", "Java"),
    Kotlin("kotlin", "Kotlin", "kt"),
    Scala("scala", "Scala 3");

    override fun toString(): String = id
}