package clockvapor.redditanalyzer.scraper

import clockvapor.redditanalyzer.common.checkedPlus

object StringUtils {
    val whitespaceRegex = Regex("[\\s\\p{Z}]+")
    val punctuationRegex = Regex("[`~!@#$%^&*()\\-_=+\\[\\],<.>/?\\\\|;:\"]")
    val dashRegex = Regex("\\p{Pd}")
    val subredditLinkRegex = Regex("/?r/\\w+")
    val userLinkRegex = Regex("/?u/\\w+")
    val numberWithCommasRegex = Regex("(?:\\d+,)+\\d+")
    val urlRegex = Regex("[a-zA-Z]+://\\S+")

    fun mergeWordMaps(a: Map<String, Int>, b: Map<String, Int>): MutableMap<String, Int> {
        val result = mutableMapOf<String, Int>()
        addToWordMap(result, a)
        addToWordMap(result, b)
        return result
    }

    fun addToWordMap(base: MutableMap<String, Int>, other: Map<String, Int>) {
        for ((word, count) in other) {
            base.compute(word) { _, c -> c?.checkedPlus(count) ?: count }
        }
    }

    fun getWordMap(words: Iterable<String>, mode: Scraper.Mode = Scraper.Mode.DEFAULT): Map<String, Int> {
        val map = hashMapOf<String, Int>()
        if (mode == Scraper.Mode.DEFAULT) {
            for (word in words) {
                map.compute(word) { _, count -> count?.checkedPlus(1) ?: 1 }
            }
        } else if (mode == Scraper.Mode.COMMENT) {
            for (word in words) {
                map.computeIfAbsent(word) { 1 }
            }
        }
        return map
    }
}

fun String.getWordMap() = StringUtils.getWordMap(split(StringUtils.whitespaceRegex))

fun String.getRedditCommentWordMap(mode: Scraper.Mode) = StringUtils.getWordMap(splitRedditCommentIntoWords(), mode)

// TODO: pick up words like "A E S T H E T I C" (usually all caps)
fun String.splitRedditCommentIntoWords(): List<String> = (
    stripLinks()
        .replace(StringUtils.urlRegex, " ")
        .replace("“", "\"").replace("”", "\"").replace("‘", "'").replace("’", "'")
        .replace("&nbsp", "")
        .replace(StringUtils.dashRegex, "-")
        .replace(StringUtils.subredditLinkRegex, " ")
        .replace(StringUtils.userLinkRegex, " ")
        .replace(StringUtils.numberWithCommasRegex, " ")
        .replace(StringUtils.punctuationRegex, " ")
        .toLowerCase().trim().split(StringUtils.whitespaceRegex) +
        StringUtils.subredditLinkRegex.findAll(this).map { it.value.toLowerCase().startingWith("/") } +
        StringUtils.userLinkRegex.findAll(this).map { it.value.toLowerCase().startingWith("/") } +
        StringUtils.numberWithCommasRegex.findAll(this).map { it.value }
    ).toMutableList().filter(String::isNotBlank)

fun String.stripLinks(): String {
    var startI = 0
    val results = mutableListOf<String>()
    start@ while (startI < length) {
        var linkStartI: Int? = null
        var linkTextEndI: Int? = null
        var linkTargetStartI: Int? = null
        var linkEndI: Int? = null
        var newline = false
        var i = startI
        while (i < length) {
            val c = this[i]
            if (linkStartI == null) {
                if (c == '\\') {
                    i++
                } else if (c == '[') {
                    linkStartI = i
                }
            } else if (linkTextEndI == null) {
                if (c == '\\') {
                    i++
                } else if (c == '[') {
                    results += substring(startI, i)
                    startI = i
                    continue@start
                } else if (c == ']') {
                    linkTextEndI = i
                }
            } else if (linkTargetStartI == null) {
                // can only have one newline between link text and link target
                if (c == '\n') {
                    if (newline) {
                        results += substring(startI, i + 1)
                        startI = i + 1
                        continue@start
                    } else {
                        newline = true
                    }
                } else if (c == '(') {
                    linkTargetStartI = i
                } else if (!c.isWhitespace()) {
                    results += substring(startI, i)
                    startI = i
                    continue@start
                }
            } else if (linkEndI == null) {
                if (c == '\\') {
                    i++
                } else if (c == ')') {
                    linkEndI = i
                    break
                }
            }
            i++
        }
        if (linkStartI != null && linkTextEndI != null && linkTargetStartI != null && linkEndI != null) {
            results += substring(startI, linkStartI) + ' ' + substring(linkStartI + 1, linkTextEndI) + ' '
            startI = linkEndI + 1
        } else {
            results += substring(startI, length)
            break@start
        }
    }
    return results.joinToString(separator = "").trim()
}

fun String.startingWith(prefix: String): String =
    if (this.startsWith(prefix)) this
    else "$prefix$this"
