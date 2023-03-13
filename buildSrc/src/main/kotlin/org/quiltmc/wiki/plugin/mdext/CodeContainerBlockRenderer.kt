package org.quiltmc.wiki.plugin.mdext

import com.vladsch.flexmark.html.HtmlWriter
import com.vladsch.flexmark.html.renderer.NodeRenderer
import com.vladsch.flexmark.html.renderer.NodeRendererContext
import com.vladsch.flexmark.html.renderer.NodeRendererFactory
import com.vladsch.flexmark.html.renderer.NodeRenderingHandler
import com.vladsch.flexmark.util.data.DataHolder
import com.vladsch.flexmark.util.data.DataKey
import org.quiltmc.wiki.plugin.FilesBlock
import org.quiltmc.wiki.plugin.Language
import org.quiltmc.wiki.plugin.lineNumber
import java.io.File
import java.nio.file.Path
import kotlin.io.path.Path
import kotlin.io.path.readText
import kotlin.io.path.relativeTo

data class Entry(
    val lang: String,
    val path: Path,
    val text: String,
    val lineRange: IntRange?,
) {
    override fun equals(other: Any?): Boolean = other is Entry && lang == other.lang
    override fun hashCode(): Int = lang.hashCode()
}

class CodeContainerBlockRenderer(options: DataHolder): NodeRenderer {
    private val languages = CodeContainerExtension.LANGUAGES.get(options)
    private val fileRoot = CodeContainerExtension.FILE_ROOT.get(options)
    private val srcRoot = Path(CodeContainerExtension.SRC_ROOT.get(options))
    private val linkRoot = CodeContainerExtension.LINK_ROOT.get(options)

    override fun getNodeRenderingHandlers() = mutableSetOf(
        NodeRenderingHandler(CodeContainerBlock::class.java, this::render),
        NodeRenderingHandler(LanguageSpecificContent::class.java, this::render),
    )

    @Suppress("UNUSED_PARAMETER")
    private fun render(node: CodeContainerBlock, context: NodeRendererContext, html: HtmlWriter) {
        val entries = mutableSetOf<Entry>()

        node.files.forEach {
            languages.mapTo(entries) { lang ->
                val langName = if (it.lang.isNull) lang.id else it.lang.toString()
                val pathStr = it.path.toString().replace("$", lang.id).replace("@", lang.ext)
                val path = Path(fileRoot, pathStr)
                var text = path.readText()

                println(path)
                println(node.name)

                val lineRange = if (node.name.isNotNull) {
                    val s = "//@start ${node.name}"
                    val start = text.indexOf(s) + s.length
                    val end = text.indexOf("//@end ${node.name}")

                    val startLine = text.substring(0..start).lineNumber
                    // - 1 to exclude the end line itself
                    val endLine = text.substring(0 .. end).lineNumber - 1

                    text = text.substring(start, end).trimIndent()
                    text = "// ... \n$text\n// ..."

                    startLine..endLine
                } else {
                    null
                }

                Entry(langName, path, text, lineRange)
            }
        }

        html.withAttr().attr("class", "code-container").tag("div") {
            html.withAttr().attr("class", "tabs-header").tag("div") {
                html.withAttr().attr("class", "tabs is-boxed").tag("div") {
                    html.tag("ul") {
                        if (entries.size > 1) {
                            entries.forEach { renderEntryTab(it, html) }
                        }
                    }
                }
                entries.forEach { renderSourceLink(it, html) }
            }
            html.tag("pre") {
                html.openPre()
                entries.forEach {
                    html.withAttr()
                        .attr("class", "language-${it.lang} is-${it.lang}")
                        .tag("code") {
                            html.text(it.text)
                        }
                }
                html.closePre()
            }
        }
    }

    private fun render(node: LanguageSpecificContent, context: NodeRendererContext, html: HtmlWriter) {
        val classes = node.langs
            .splitToSequence(' ')
            .joinToString(separator = " ") { "is-$it" }

        html.withAttr().attr("class", classes).tag("div") {
            context.renderChildren(node)
        }
    }


    private fun renderEntryTab(entry: Entry, html: HtmlWriter) {
        html.withAttr().attr("for", "lang-${entry.lang}").tag("label") {
            html.tag("li") {
                html.tag("a") {
                    val knownLanguages = Language.values()
                    val lang = knownLanguages.find { it.id == entry.lang }
                        ?: error("""
                            Encountered unknown language $${entry.lang} while rendering a tabbed code container!
                            Languages other than $languages cannot be used in code containers with more than one language!
                        """.trimIndent())
                    html.text(lang.displayName)
                }
            }
        }
    }
    private fun renderSourceLink(entry: Entry, html: HtmlWriter) {
        val href = buildString {
            append(linkRoot)
            append('/')
            append(srcRoot.relativize(entry.path))

            if (entry.lineRange != null) {
                append("#L${entry.lineRange.first}")
                if (entry.lineRange.last != entry.lineRange.first) {
                    // there are multiple lines
                    append("-L${entry.lineRange.last}")
                }
            }
        }

        html.withAttr()
            .attr("class", "source-link is-${entry.lang}")
            .attr("href", href)
            .tag("a") {
                html.text(entry.path.fileName.toString())
            }
    }

    object Factory: NodeRendererFactory {
        override fun apply(options: DataHolder) = CodeContainerBlockRenderer(options)
    }
}