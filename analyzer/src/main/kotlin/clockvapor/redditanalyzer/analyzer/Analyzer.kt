package clockvapor.redditanalyzer.analyzer

import clockvapor.redditanalyzer.common.Stuff
import clockvapor.redditanalyzer.common.checkedPlus
import clockvapor.redditanalyzer.common.checkedSum
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
        calculateWordCounts(stuff)
        val scores = stuff.calculateScores(options.weightExponent, options.limit)
        json.writeValue(options.outFile, scores)
        println(getMostDistinguishingWords(scores).joinToString("\n"))
    }

    private fun calculateWordCounts(stuff: Stuff) {
        for ((subreddit, wordMap) in stuff.data) {
            subredditAllWordCountMap[subreddit] = wordMap.values.checkedSum()
            for ((word, count) in wordMap) {
                globalWordCountMap.compute(word) { _, c -> c?.checkedPlus(count) ?: count }
            }
        }
        globalAllWordCount = subredditAllWordCountMap.values.checkedSum()
    }

    private fun Stuff.calculateScores(weightExponent: Double, limit: Int): Map<String, Map<String, Double>> {
        var result = mutableMapOf<String, MutableMap<String, Double>>()
        for ((subreddit, wordMap) in data) {
            val resultSubredditMap = result.getOrPut(subreddit) { mutableMapOf() }
            for (word in wordMap.keys) {
                resultSubredditMap[word] = score(subreddit, word, weightExponent)
            }
        }
        result = result.toList().sortedBy { (subreddit, _) -> subreddit }
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

    private fun Stuff.score(subreddit: String, word: String, weightExponent: Double): Double {
        val subredditWordCount = data[subreddit]!![word]!!
        val subredditAllWordCount = subredditAllWordCountMap[subreddit]!!
        val globalWordCount = globalWordCountMap[word]!!
        return (Math.pow(subredditWordCount.toDouble(), weightExponent) / globalWordCount) *
            (globalAllWordCount!! / subredditAllWordCount.toDouble())
    }

    private fun getMostDistinguishingWords(scores: Map<String, Map<String, Double>>) =
        scores.flatMap { (subreddit, wordMap) ->
            wordMap.map { (word, count) -> Triple(subreddit, word, count) }
        }.sortedByDescending { (_, _, score) -> score }

    private class Options(parser: ArgParser) {
        val inFile by parser.storing("-d", "--data", help = "path to data file") { File(this) }
        val outFile by parser.storing("-o", "--output", help = "path to output file") { File(this) }
        val limit by parser.storing("-l", "--limit", help = "number of top scorers per subreddit") { this.toInt() }
        val weightExponent by parser.storing("-w", "--weight", help = "exponent weight") { this.toDouble() }
    }
}
