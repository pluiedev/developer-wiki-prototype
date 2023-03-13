package org.quiltmc.wiki.plugin

import com.vladsch.flexmark.html.HtmlRenderer
import com.vladsch.flexmark.parser.Parser
import com.vladsch.flexmark.util.data.DataKey
import com.vladsch.flexmark.util.data.DataSet
import com.vladsch.flexmark.util.data.MutableDataHolder
import com.vladsch.flexmark.util.data.MutableDataSet
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.file.SourceDirectorySet
import org.gradle.api.internal.tasks.DefaultSourceSet
import org.gradle.api.model.ObjectFactory
import org.gradle.api.plugins.BasePlugin
import org.gradle.api.plugins.JavaPluginExtension
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.SourceSetContainer
import org.gradle.api.tasks.SourceTask
import org.gradle.api.tasks.TaskAction
import org.gradle.api.tasks.bundling.Jar
import org.quiltmc.wiki.plugin.mdext.CodeContainerExtension
import java.nio.file.Files
import javax.inject.Inject

class WikiPlugin
    @Inject @Suppress("MemberVisibilityCanBePrivate")
    constructor (val objectFactory: ObjectFactory)
: Plugin<Project> {

    override fun apply(target: Project) {
        val extension = target.extensions.create("wiki", WikiPluginExtension::class.java)
        val javaExtension = target.extensions.getByType(JavaPluginExtension::class.java)
        val sourceSets = javaExtension.sourceSets

        registerMarkdownSourceSet(sourceSets)
        registerLanguages(target, extension, sourceSets)

        target.tasks.apply {
            register("generateMarkdown", GenerateMarkdownTask::class.java) { task ->
                sourceSets.forEach {
                    val set = it.extensions.findByName("markdown") as? SourceDirectorySet ?: return@forEach
                    task.source(set.sourceDirectories)
                }
            }
        }
    }

    private fun registerMarkdownSourceSet(sourceSets: SourceSetContainer) {
        sourceSets.forEach {
            val displayName = (it as DefaultSourceSet).displayName
            val markdownSet = objectFactory.sourceDirectorySet("markdown", "$displayName Markdown source")
                .also { set ->
                    set.srcDir("src/${it.name}/markdown")
                    set.filter.include("**/*.markdown", "**/*.md")
                }

            it.extensions.add(SourceDirectorySet::class.java, "markdown", markdownSet)
        }
    }

    private fun registerLanguages(target: Project, extension: WikiPluginExtension, sourceSets: SourceSetContainer) {
        extension.languages.convention(Language.values().toList())

        val langSourceSets = extension.languages.get().associateWith { lang ->
            sourceSets.register(lang.id) {
                it.java.srcDir("${target.buildDir}/$lang/src/java/")
                if (lang.id != "java") {
                    val set = it.extensions.getByName(lang.id) as SourceDirectorySet
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

        val langJarTasks = langSourceSets.map { (lang, setProvider) ->
            target.tasks.register("${lang}Jar", Jar::class.java) {
                it.group = BasePlugin.BUILD_GROUP
                it.archiveAppendix.set(lang.id)

                it.from(setProvider.get().output)
            }
        }

        target.tasks.getByName("jar").dependsOn(langJarTasks)
    }
}


abstract class GenerateMarkdownTask: SourceTask() {
    @get:OutputDirectory
    abstract val outputDirectory: DirectoryProperty

    private val options = buildDataSet {
        Parser.EXTENSIONS to listOf(CodeContainerExtension)
        CodeContainerExtension.LANGUAGES to project.wiki.languages.get()
        CodeContainerExtension.LINK_ROOT to project.property("git_repo_tree")
        CodeContainerExtension.FILE_ROOT to project.projectDir
        CodeContainerExtension.SRC_ROOT to project.rootProject.projectDir
    }

    private val parser = Parser.builder(options).build()
    private val renderer = HtmlRenderer.builder(options).build()

    init {
        group = "wiki"
        outputDirectory.set(project.file("out/markdown/"))
    }

    @TaskAction
    fun generateMarkdown() {
        println(project.projectDir)
        val output = outputDirectory.get()

        source.visit {
            if (it.isDirectory) {
                project.mkdir(output.file(it.path))
                return@visit
            }
            val doc = parser.parse(it.file.readText())
            doc.children.forEach { println(it.toAstString(true)) }
            println()
            doc.children.forEach {
                it.children.forEach {
                    println(it.toAstString(true))
                }
            }
            println()
            doc.children.forEach {
                it.children.forEach {
                    it.children.forEach {
                        println(it.toAstString(true))
                    }
                }
            }


            val html = renderer.render(doc)

            val f = output.file(it.path.replaceAfterLast('.', "html")).asFile
            f.createNewFile()
            f.writeText(html)

        }
    }
}