package clockvapor.redditanalyzer.common

import kotlin.system.exitProcess

typealias SubredditData = MutableMap<String, MutableMap<String, Int>>

fun subredditData(): SubredditData = mutableMapOf()

fun error(message: String): Nothing {
    System.err.println(message)
    exitProcess(1)
}

fun Int.checkedPlus(other: Int): Int {
    if (Int.MAX_VALUE - this < other) {
        throw Error("integer overflow")
    }
    return this + other
}

fun Iterable<Int>.checkedSum(): Int {
    var sum = 0
    for (i in this) {
        sum = sum.checkedPlus(i)
    }
    return sum
}
