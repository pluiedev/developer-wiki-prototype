package org.quiltmc.wiki.plugin.mdext

import com.vladsch.flexmark.parser.block.*
import com.vladsch.flexmark.util.ast.Block
import com.vladsch.flexmark.util.data.DataHolder
import com.vladsch.flexmark.util.sequence.BasedSequence
import java.util.regex.Pattern
import org.quiltmc.wiki.plugin.match

class CodeContainerBlock: Block() {
    var openingMarker: BasedSequence = BasedSequence.NULL
    var name: BasedSequence = BasedSequence.NULL
    var closingMarker: BasedSequence = BasedSequence.NULL
    val files = mutableListOf<File>()

    override fun getSegments() = arrayOf(openingMarker, name, closingMarker)
    override fun getAstExtra(out: StringBuilder) {
        segmentSpanChars(out, openingMarker, "openingMarker")
        segmentSpanChars(out, name, "name")
        files
            .joinTo(out, prefix = " files: {", postfix = "}") {
                buildString {
                    segmentSpanChars(
                        this,
                        it.path.startOffset,
                        it.path.endOffset,
                        null,
                        it.toString()
                    )
                }
            }
        segmentSpanChars(out, closingMarker, "closingMarker")
    }

    data class File(val path: BasedSequence, val lang: BasedSequence = BasedSequence.NULL)
}

class CodeContainerBlockParser: AbstractBlockParser() {
    val block = CodeContainerBlock()

    override fun isContainer(): Boolean = true
    override fun canContain(state: ParserState, blockParser: BlockParser, block: Block): Boolean =
        block is LanguageSpecificContent

    override fun getBlock(): Block = block

    override fun tryContinue(state: ParserState): BlockContinue {
        val nextNonSpace = state.nextNonSpaceIndex
        val line = state.line

        if (state.indent <= 3 && nextNonSpace < line.length) {
            val trySequence = line.subSequence(nextNonSpace, line.length)

            ADD_FILE_PATTERN.match(trySequence) {
                val file = CodeContainerBlock.File(subSequenceOfGroup(1), subSequenceOfGroup(2))

                block.files.add(file)
                return BlockContinue.atIndex(nextNonSpace + line.length)
            }
            CLOSING_MARKER_PATTERN.match(trySequence) {
                block.closingMarker = subSequenceOfGroup(1)
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
        override fun getBeforeDependents(): MutableSet<Class<*>>? = null
        override fun affectsGlobalScope(): Boolean = false
    }

    class ParserFactory(options: DataHolder): AbstractBlockParserFactory(options) {
        override fun tryStart(state: ParserState, matchedBlockParser: MatchedBlockParser): BlockStart? {
            val nextNonSpace = state.nextNonSpaceIndex
            val line = state.line

            if (state.indent >= 4) return BlockStart.none()

            val trySequence = line.subSequence(nextNonSpace, line.length)

            OPENING_MARKER_PATTERN.match(trySequence) {
                val blockParser = CodeContainerBlockParser().apply {
                    block.openingMarker = subSequenceOfGroup(1)
                    block.name = subSequenceOfGroup(2)
                }

                return BlockStart.of(blockParser).atIndex(line.length)
            }
            return BlockStart.none()
        }
    }

    companion object {
        private val OPENING_MARKER_PATTERN = Pattern.compile("^(===)\\s+([a-z0-9._/\\-]+)?\\s*$")
        private val ADD_FILE_PATTERN = Pattern.compile("^=\\+=\\s+([a-zA-Z0-9/._\\-$@]+)(?:\\s+([a-zA-Z0-9/._\\-\$@]+))?\\s*$")
        private val CLOSING_MARKER_PATTERN = Pattern.compile("^(=/=)\\s*$")
    }
}
