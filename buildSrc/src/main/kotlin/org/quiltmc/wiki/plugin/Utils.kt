package org.quiltmc.wiki.plugin

import com.vladsch.flexmark.util.data.DataKey
import com.vladsch.flexmark.util.data.DataSet
import com.vladsch.flexmark.util.data.MutableDataHolder
import com.vladsch.flexmark.util.data.MutableDataSet
import com.vladsch.flexmark.util.sequence.BasedSequence
import java.util.regex.MatchResult
import java.util.regex.Matcher
import java.util.regex.Pattern

class BuildMutableDataSetScope(val set: MutableDataSet): MutableDataHolder by set {
    infix fun <T: Any> DataKey<T>.to(value: T) {
        set.set(this, value)
    }
    fun build(): DataSet = set.toImmutable()
}

inline fun buildDataSet(action: BuildMutableDataSetScope.() -> Unit): DataSet =
    MutableDataSet()
        .let(::BuildMutableDataSetScope)
        .apply(action)
        .build()

class MatcherScope(val input: BasedSequence, val matcher: Matcher): MatchResult by matcher {
    fun subSequenceOfGroup(group: Int): BasedSequence = subSequenceOfGroupOrNull(group) ?: BasedSequence.NULL

    fun subSequenceOfGroupOrNull(group: Int): BasedSequence? = if (start(group) != -1)
        input.subSequence(start(group), end(group))
    else null
}
inline fun Pattern.match(input: BasedSequence, action: MatcherScope.() -> Unit) =
    MatcherScope(input, matcher(input)).also {
        if (it.matcher.find()) it.action()
    }

val String.lineNumber
    get() = count { it == '\n' } + 1