package org.quiltmc.wiki.plugin

import org.gradle.api.file.SourceDirectorySet
import org.gradle.api.internal.file.DefaultSourceDirectorySet
import org.gradle.api.model.ObjectFactory

interface MarkdownSourceSet {
    val markdown: MarkdownSourceDirectorySet
}
class DefaultMarkdownSourceSet(name: String, displayName: String, objectFactory: ObjectFactory): MarkdownSourceSet {
    override val markdown: MarkdownSourceDirectorySet =
        objectFactory.sourceDirectorySet(name, "$displayName Markdown source")
            .let(::DefaultMarkdownSourceDirectorySet)
            .also {
                it.filter.include("**/*.markdown", "**/*.md")
            }

}

interface MarkdownSourceDirectorySet: SourceDirectorySet

class DefaultMarkdownSourceDirectorySet(
    sourceDirectorySet: SourceDirectorySet
): DefaultSourceDirectorySet(sourceDirectorySet), MarkdownSourceDirectorySet
