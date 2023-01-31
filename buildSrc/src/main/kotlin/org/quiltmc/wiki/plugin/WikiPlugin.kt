package org.quiltmc.wiki.plugin

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.file.SourceDirectorySet
import org.gradle.api.internal.tasks.DefaultSourceSet
import org.gradle.api.model.ObjectFactory
import org.gradle.api.plugins.BasePlugin
import org.gradle.api.plugins.JavaPluginExtension
import org.gradle.api.tasks.SourceSetContainer
import org.gradle.api.tasks.bundling.Jar
import javax.inject.Inject

class WikiPlugin
    @Inject @Suppress("MemberVisibilityCanBePrivate")
    constructor (val objectFactory: ObjectFactory)
: Plugin<Project> {

    override fun apply(target: Project) {
        val javaExtension = target.extensions.getByType(JavaPluginExtension::class.java)
        val sourceSets = javaExtension.sourceSets

        registerMarkdownSourceSet(sourceSets)
        registerLanguages(target, sourceSets)
    }

    private fun registerMarkdownSourceSet(sourceSets: SourceSetContainer) {
        sourceSets.forEach {
            val markdownSourceSet = DefaultMarkdownSourceSet("markdown", (it as DefaultSourceSet).displayName, objectFactory)
            it.extensions.add(MarkdownSourceDirectorySet::class.java, "markdown", markdownSourceSet.markdown)

            val markdownSource = markdownSourceSet.markdown
            markdownSource.srcDir("src/${it.name}/markdown")
        }
    }

    private fun registerLanguages(target: Project,sourceSets: SourceSetContainer) {
        val extension = target.extensions.create("wiki", WikiPluginExtension::class.java).also {
            it.languages.convention(listOf("kotlin", "java", "groovy", "scala"))
        }

        val langSourceSets = extension.languages.get().associateWith { lang ->
            sourceSets.register(lang) {
                it.java.srcDir("${target.buildDir}/$lang/src/java/")
                if (lang != "java") {
                    val set = it.extensions.getByName(lang) as SourceDirectorySet
                    set.srcDir("${target.buildDir}/$lang/src/$lang/")
                }

                // Adding the main resources *before* the language-specific resources
                // allows us to override main resources. This is why language-specific
                // QMJs can simply override the main QMJ.
                val main = sourceSets.getByName("main")
                val oldSrcDirs = it.resources.srcDirs
                it.resources.setSrcDirs(main.resources.srcDirs)
                it.resources.srcDir(oldSrcDirs)

                it.compileClasspath += main.compileClasspath
                it.runtimeClasspath += main.runtimeClasspath
            }
        }

        val langJarTasks = langSourceSets.map { (name, setProvider) ->
            target.tasks.register("${name}Jar", Jar::class.java) {
                it.group = BasePlugin.BUILD_GROUP
                it.archiveAppendix.set(name)

                it.from(setProvider.get().output)
            }
        }

        target.tasks.getByName("jar").dependsOn(langJarTasks)
    }
}
