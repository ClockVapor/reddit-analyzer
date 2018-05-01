package clockvapor.redditanalyzer.common

import kotlin.system.exitProcess

typealias SubredditData = MutableMap<String, MutableMap<String, Int>>

fun subredditData(): SubredditData = mutableMapOf()

fun error(message: String): Nothing {
    System.err.println(message)
    exitProcess(1)
}
