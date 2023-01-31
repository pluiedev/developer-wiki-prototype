package org.quiltmc.wiki.plugin

import org.gradle.api.provider.ListProperty

interface WikiPluginExtension {
    val languages: ListProperty<String>
}