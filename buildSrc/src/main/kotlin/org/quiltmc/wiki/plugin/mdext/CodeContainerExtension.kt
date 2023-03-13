package org.quiltmc.wiki.plugin.mdext

import com.vladsch.flexmark.html.HtmlRenderer
import com.vladsch.flexmark.parser.Parser
import com.vladsch.flexmark.util.data.DataKey
import com.vladsch.flexmark.util.data.MutableDataHolder
import org.quiltmc.wiki.plugin.Language

object CodeContainerExtension: Parser.ParserExtension, HtmlRenderer.HtmlRendererExtension {
    val LANGUAGES = DataKey("CODE_CONTAINER_LANGUAGES", Language.values().toList())
    val FILE_ROOT = DataKey("CODE_CONTAINER_FILE_ROOT", "")
    val SRC_ROOT = DataKey("CODE_CONTAINER_SRC_ROOT", "")
    val LINK_ROOT = DataKey("CODE_CONTAINER_LINK_ROOT", "")

    override fun parserOptions(options: MutableDataHolder) {}

    override fun extend(parserBuilder: Parser.Builder) {
        parserBuilder
            .customBlockParserFactory(CodeContainerBlockParser.Factory)
            .customBlockParserFactory(LanguageSpecificContentParser.Factory)
    }

    override fun rendererOptions(options: MutableDataHolder) {}

    override fun extend(htmlRendererBuilder: HtmlRenderer.Builder, rendererType: String) {
        htmlRendererBuilder
            .nodeRendererFactory(CodeContainerBlockRenderer.Factory)
    }
}