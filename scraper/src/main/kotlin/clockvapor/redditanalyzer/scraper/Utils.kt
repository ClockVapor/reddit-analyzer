package clockvapor.redditanalyzer.scraper

typealias SubredditData = MutableMap<String, MutableMap<String, Int>>

fun subredditData(): SubredditData = mutableMapOf()

private val whitespaceRegex = Regex("\\s+")
private val punctuationRegex = Regex("[`~!@#$%^&*()-_=+\\[],<.>/?\\\\\\|]")

fun String.getWordMap(): Map<String, Int> {
    val words = splitIntoWords()
    val map = hashMapOf<String, Int>()
    for (word in words) {
        map.compute(word) { _, count -> count?.plus(1) ?: 1 }
    }
    return map
}

// TODO: strip contents out of tables, then strip link text out of links, then remove punctuation
fun String.splitIntoWords(): List<String> =
    stripLinks().replace(punctuationRegex, " ").toLowerCase().trim().split(whitespaceRegex)

fun String.stripLinks(): String {
    val linkStartI = indexOfIgnoringEscapes('[') ?: return this
    val linkTextEndI = indexOfIgnoringEscapes(']', linkStartI + 1) ?: return this
    var linkTargetStartI: Int? = null

    var i = linkTextEndI + 1
    var newline = false
    while (i < length) {
        val c = this[i]
        // can only have one newline between link text and link target
        if (c == '\n') {
            if (newline) return this
            else newline = true
        } else if (c == '(') {
            linkTargetStartI = i
            break
        } else if (c != ' ') {
            return this
        }
        i++
    }
    if (linkTargetStartI == null) return this

    val linkEndI = indexOfIgnoringEscapes(')', linkTargetStartI + 1) ?: return this

    return substring(0, linkStartI) + ' ' + substring(linkStartI + 1, linkTextEndI) + ' ' +
        substring(linkEndI + 1).stripLinks()
}

fun String.indexOfIgnoringEscapes(c: Char, startI: Int = 0): Int? {
    var i = startI
    while (i < length) {
        val a = this[i]
        if (a == '\\') {
            i++
        } else if (a == c) {
            return i
        }
        i++
    }
    return null
}
