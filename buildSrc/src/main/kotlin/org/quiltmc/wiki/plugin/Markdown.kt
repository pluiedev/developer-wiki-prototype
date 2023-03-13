package org.quiltmc.wiki.plugin

import com.vladsch.flexmark.ast.FencedCodeBlock
import com.vladsch.flexmark.html.HtmlWriter
import com.vladsch.flexmark.html.renderer.NodeRenderer
import com.vladsch.flexmark.html.renderer.NodeRendererContext
import com.vladsch.flexmark.html.renderer.NodeRenderingHandler
import com.vladsch.flexmark.parser.block.BlockPreProcessor
import com.vladsch.flexmark.parser.block.BlockPreProcessorFactory
import com.vladsch.flexmark.parser.block.ParserState
import com.vladsch.flexmark.util.ast.Block
import com.vladsch.flexmark.util.sequence.BasedSequence
import com.vladsch.flexmark.util.sequence.SequenceUtils
import org.gradle.api.Project
import java.io.File
import java.lang.StringBuilder


class FilesBlock(
    block: FencedCodeBlock,
    val entries: Set<Entry>,
) : FencedCodeBlock(
    block.chars,
    block.openingMarker,
    block.info,
    block.segments.toList(),
    block.closingMarker
) {
    data class Entry(
        val lang: BasedSequence, val path: BasedSequence, val section: BasedSequence,
        val substs: Map<BasedSequence, BasedSequence>,
        val file: File,
    ) : Block() {
        val content: Content by lazy {
            var text = file.readText()

            val lineRange = if (!section.equals("*")) {
                val s = "//@start $section"
                val start = text.indexOf(s) + s.length
                val end = text.indexOf("//@end $section")

                val startLine = text.substring(0..start).lineNumber
                // - 1 to exclude the end line itself
                val endLine = text.substring(0 .. end).lineNumber - 1

                text = text.substring(start, end).trimIndent()
                text = "// ... \n$text\n// ..."

                startLine..endLine
            } else {
                null
            }

            text = substs.entries.fold(text) { t, (k, v) ->
                t.replace("//$k", "// $v")
            }
            Content(text, lineRange)
        }

        override fun getAstExtra(out: StringBuilder) {
            segmentSpanChars(out, lang, "lang")
            segmentSpanChars(out, path, "path")
            segmentSpanChars(out, section, "section")
        }

        override fun getSegments() = arrayOf(lang, path, section)

        override fun equals(other: Any?): Boolean = other is Entry && lang == other.lang
        override fun hashCode(): Int = lang.hashCode()

        data class Content(val text: String, val lineRange: IntRange?)
    }
}

data class FilesBlockPreProcessor(val project: Project, val languages: List<Language>) : BlockPreProcessor {
    override fun preProcess(state: ParserState, block: Block) {
        val codeBlock = block as? FencedCodeBlock ?: return
        if (!codeBlock.info.equals("files")) return

        val substs = mutableMapOf<BasedSequence, BasedSequence>()
        val entries = mutableSetOf<FilesBlock.Entry>()

        codeBlock.contentLines.forEach {
            if (it.isBlank) return@forEach
            if (it.startsWith('#')) {
                // Substitution line
                // #key = value
                val (key, value) = it.split("=", 2, SequenceUtils.SPLIT_TRIM_PARTS)
                substs[key] = value
            } else {
                // File line
                // language;path;section
                //
                // We'll also run a substitution routine here - for each language:
                // for the source set and the path, $ is substituted to be the language's ID
                // for the path, @ is substituted to be the language's extension
                val (rawSourceSet, rawPath, section) = it.split(";", 3, SequenceUtils.SPLIT_TRIM_PARTS)

                languages.mapTo(entries) { lang ->
                    val langSeq = rawSourceSet.replace("$", lang.id)
                    val path = rawPath.replace("$", lang.id).replace("@", lang.ext)
                    val file = project.file(path)
                    FilesBlock.Entry(langSeq, path, section, substs, file)
                }
            }
        }
        val filesBlock = FilesBlock(codeBlock, entries)

        codeBlock.insertBefore(filesBlock)
        codeBlock.unlink()
        state.blockAddedWithChildren(filesBlock)
        state.blockRemoved(codeBlock)
    }

    data class Factory(val project: Project, val languages: List<Language>) : BlockPreProcessorFactory {
        override fun apply(state: ParserState): BlockPreProcessor =
            FilesBlockPreProcessor(project, languages)

        override fun getAfterDependents(): MutableSet<Class<*>>? = null
        override fun getBeforeDependents(): MutableSet<Class<*>>? = null
        override fun affectsGlobalScope(): Boolean = true

        override fun getBlockTypes(): MutableSet<Class<out Block>> = mutableSetOf(
            FencedCodeBlock::class.java
        )

    }
}

class FilesBlockRenderer(private val project: Project) : NodeRenderer {
    override fun getNodeRenderingHandlers(): MutableSet<NodeRenderingHandler<*>> = mutableSetOf(
        NodeRenderingHandler(FilesBlock::class.java, this::render)
    )

    @Suppress("UNUSED_PARAMETER")
    private fun render(node: FilesBlock, context: NodeRendererContext, html: HtmlWriter) {
        html.withAttr().attr("class", "code-container").tag("div") {
            html.withAttr().attr("class", "tabs-header").tag("div") {
                html.withAttr().attr("class", "tabs is-boxed").tag("div") {
                    html.tag("ul") {
                        if (node.entries.size > 1) {
                            node.entries.forEach { renderEntryTab(it, html) }
                        }
                    }
                }
                node.entries.forEach { renderSourceLink(it, html) }
            }
            html.tag("pre") {
                html.openPre()
                node.entries.forEach {
                    html.withAttr()
                        .attr("class", "language-${it.lang} is-${it.lang}")
                        .tag("code") {
                            html.text(it.content.text)
                        }
                }
                html.closePre()
            }
        }
    }


    private fun renderEntryTab(node: FilesBlock.Entry, html: HtmlWriter) {
        html.withAttr().attr("for", "lang-${node.lang}").tag("label") {
            html.tag("li") {
                html.tag("a") {
                    val knownLanguages = Language.values()
                    val langId = node.lang.toString()
                    val lang = knownLanguages.find { it.id == langId }
                        ?: error("""
                            Encountered unknown language $langId while rendering a tabbed code container!
                            Languages other than ${Language.values().toList()} cannot be used in code containers with more than one language!
                        """.trimIndent())
                    html.text(lang.displayName)
                }
            }
        }
    }
    private fun renderSourceLink(node: FilesBlock.Entry, html: HtmlWriter) {
        val href = buildString {
            append(project.property("git_repo_tree"))
            append('/')
            append(project.rootProject.relativePath(node.file.path))

            val lineRange = node.content.lineRange
            if (lineRange != null) {
                append("#L${lineRange.first}")
                if (lineRange.last != lineRange.first) {
                    // there are multiple lines
                    append("-L${lineRange.last}")
                }
            }
        }

        html.withAttr()
            .attr("class", "source-link is-${node.lang}")
            .attr("href", href)
            .tag("a") {
                html.text(node.path.toString().substringAfterLast('/'))
            }
    }
}