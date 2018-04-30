package clockvapor.redditanalyzer.scraper

object StringUtils {
    val whitespaceRegex = Regex("\\s+")
    val punctuationRegex = Regex("[`~!@#$%^&*()-_=+\\[],<.>/?\\\\\\|]")

    fun mergeWordMaps(a: Map<String, Int>, b: Map<String, Int>): MutableMap<String, Int> {
        val result = mutableMapOf<String, Int>()
        addToWordMap(result, a)
        addToWordMap(result, b)
        return result
    }

    fun addToWordMap(base: MutableMap<String, Int>, other: Map<String, Int>) {
        for ((word, count) in other) {
            base.compute(word) { _, c -> c?.plus(count) ?: count }
        }
    }
}

fun String.getWordMap() = getWordMap(split(StringUtils.whitespaceRegex))

fun String.getRedditCommentWordMap() = getWordMap(splitRedditCommentIntoWords())

fun getWordMap(words: Iterable<String>): Map<String, Int> {
    val map = hashMapOf<String, Int>()
    for (word in words) {
        map.compute(word) { _, count -> count?.plus(1) ?: 1 }
    }
    return map
}

// TODO: strip contents out of tables, then strip link text out of links, then remove punctuation
fun String.splitRedditCommentIntoWords(): List<String> =
    stripLinks().replace(StringUtils.punctuationRegex, " ").toLowerCase().trim().split(StringUtils.whitespaceRegex)

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
            results += substring(startI, i)
            break@start
        }
    }
    return results.joinToString(separator = "").trim()
}
