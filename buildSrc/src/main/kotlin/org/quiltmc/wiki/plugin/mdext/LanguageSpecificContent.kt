package org.quiltmc.wiki.plugin.mdext

import com.vladsch.flexmark.parser.block.*
import com.vladsch.flexmark.util.ast.Block
import com.vladsch.flexmark.util.data.DataHolder
import com.vladsch.flexmark.util.sequence.BasedSequence
import org.quiltmc.wiki.plugin.match
import java.util.regex.Pattern

data class LanguageSpecificContent(
    var openingMarker: BasedSequence = BasedSequence.NULL,
    var langs: BasedSequence = BasedSequence.NULL,
    var closingMarker: BasedSequence = BasedSequence.NULL,
): Block() {
    override fun getSegments() = arrayOf(openingMarker, langs, closingMarker)

    override fun getAstExtra(out: StringBuilder) {
        segmentSpanChars(out, openingMarker, "openingMarker")
        segmentSpanChars(out, langs, "langs")
        segmentSpanChars(out, closingMarker, "closingMarker")
    }
}

class LanguageSpecificContentParser: AbstractBlockParser() {
    val block = LanguageSpecificContent()

    override fun isContainer(): Boolean = true
    override fun canContain(state: ParserState, blockParser: BlockParser, block: Block): Boolean = true
    override fun isInterruptible(): Boolean = true
    override fun canInterruptBy(blockParserFactory: BlockParserFactory): Boolean
        = blockParserFactory is ParserFactory

    override fun getBlock(): Block = block

    override fun tryContinue(state: ParserState): BlockContinue {
        val nextNonSpace = state.nextNonSpaceIndex
        val line = state.line


        if (state.indent <= 3 && nextNonSpace < line.length) {
            val trySequence = line.subSequence(nextNonSpace, line.length)

            CLOSING_MARKER_PATTERN.match(trySequence) {
                // a new language-specific content block begins, or the code container ends!
                // anyway, stop now.
                block.closingMarker = subSequenceOfGroup(1)
                println("haha")
                return BlockContinue.finished()
            }
        }

        return BlockContinue.atIndex(nextNonSpace)
    }

    override fun closeBlock(state: ParserState) {
        block.setCharsFromContent()
    }

    object Factory: CustomBlockParserFactory {
        override fun apply(options: DataHolder): BlockParserFactory = ParserFactory(options)

        override fun getAfterDependents(): MutableSet<Class<*>>? = null
        override fun getBeforeDependents(): MutableSet<Class<*>> = mutableSetOf(CodeContainerBlock::class.java)
        override fun affectsGlobalScope(): Boolean = false
    }

    class ParserFactory(options: DataHolder): AbstractBlockParserFactory(options) {
        override fun tryStart(state: ParserState, matchedBlockParser: MatchedBlockParser): BlockStart? {

            val nextNonSpace = state.nextNonSpaceIndex
            val line = state.line
            if (state.indent >= 4) return BlockStart.none()

            val trySequence = line.subSequence(nextNonSpace, line.length)

            OPENING_MARKER_PATTERN.match(trySequence) {
                val blockParser = LanguageSpecificContentParser().apply {
                    block.openingMarker = subSequenceOfGroup(1)
                    block.langs = subSequenceOfGroup(2)
                }

                return BlockStart.of(blockParser).atIndex(line.length)
            }
            return BlockStart.none()
        }
    }

    companion object {
        private val OPENING_MARKER_PATTERN = Pattern.compile("^(=-=)(?:\\s+([a-z0-9._/\\- ]+))?\\s*$")
        private val CLOSING_MARKER_PATTERN = Pattern.compile("^(=-=)\\s*$")
    }
}