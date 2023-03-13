package org.quiltmc.wiki.plugin

import org.gradle.api.Project
import org.gradle.api.provider.ListProperty

interface WikiPluginExtension {
    val languages: ListProperty<Language>

}

val Project.wiki: WikiPluginExtension
    get() = extensions.getByType(WikiPluginExtension::class.java)