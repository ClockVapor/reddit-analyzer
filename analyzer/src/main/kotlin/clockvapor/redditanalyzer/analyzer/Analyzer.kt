package clockvapor.redditanalyzer.analyzer

import clockvapor.redditanalyzer.common.Stuff
import com.fasterxml.jackson.databind.ObjectMapper
import com.xenomachina.argparser.ArgParser
import com.xenomachina.argparser.mainBody
import java.io.File

object Analyzer {
    private val globalWordCountMap = mutableMapOf<String, Int>()
    private val subredditAllWordCountMap = mutableMapOf<String, Int>()
    private var globalAllWordCount: Int? = null

    @JvmStatic
    fun main(args: Array<String>) = mainBody {
        val options = ArgParser(args).parseInto(::Options)
        val json = ObjectMapper()
        val stuff = Stuff.read(options.inFile, json)
        globalAllWordCount = stuff.data.values.fold(0) { count, wordMap -> count + wordMap.values.sum() }
        val scores = stuff.calculateScores(limit = 10)
        json.writeValue(options.outFile, scores)
    }

    private fun Stuff.calculateScores(limit: Int): Map<String, Map<String, Double>> {
        var result = mutableMapOf<String, MutableMap<String, Double>>()
        for ((subreddit, wordMap) in data) {
            val resultSubredditMap = result.getOrPut(subreddit) { mutableMapOf() }
            for (word in wordMap.keys) {
                resultSubredditMap[word] = score(subreddit, word)
            }
        }
        result = result.toList().sortedByDescending { (subreddit, _) -> subredditAllWordCountMap[subreddit] ?: 0 }
            .toMap().toMutableMap()
        for (subreddit in result.keys) {
            result.computeIfPresent(subreddit) { _, wordMap ->
                wordMap.toList().sortedWith(Comparator { o1, o2 ->
                    val c = o2.second.compareTo(o1.second)
                    if (c != 0) {
                        c
                    } else {
                        o1.first.compareTo(o2.first)
                    }
                }).take(limit).toMap().toMutableMap()
            }
        }
        return result
    }

    private fun Stuff.score(subreddit: String, word: String): Double {
        val subredditWordMap = data[subreddit]!!
        val subredditWordCount = subredditWordMap[word]!!
        val subredditAllWordCount = subredditAllWordCountMap.getOrPut(subreddit) { subredditWordMap.values.sum() }
        val globalWordCount = globalWordCountMap.getOrPut(word) {
            data.values.fold(0) { count, wordMap -> count + (wordMap[word] ?: 0) }
        }
        return (subredditWordCount * subredditWordCount / globalWordCount.toDouble()) *
            (globalAllWordCount!! / subredditAllWordCount.toDouble())
    }

    private class Options(parser: ArgParser) {
        val inFile by parser.storing("-d", "--data", help = "path to data file") { File(this) }
        val outFile by parser.storing("-o", "--output", help = "path to output file") { File(this) }
    }
}
