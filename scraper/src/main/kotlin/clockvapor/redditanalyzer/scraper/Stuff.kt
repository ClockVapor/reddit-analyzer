package clockvapor.redditanalyzer.scraper

import com.fasterxml.jackson.databind.ObjectMapper
import net.dean.jraw.models.Comment
import java.io.File
import java.io.IOException

class Stuff {
    var data: SubredditData = subredditData()
    var ids: MutableSet<String> = mutableSetOf()

    fun add(subreddit: String, comments: Iterable<Comment>) {
        val subredditMap = data.getOrPut(subreddit.toLowerCase(), ::mutableMapOf)
        for (comment in comments) {
            if (ids.add(comment.id)) {
                val wordMap = comment.body.getRedditCommentWordMap()
                StringUtils.addToWordMap(subredditMap, wordMap)
            }
        }
    }

    fun write(file: File, mapper: ObjectMapper) {
        data = sort(data)
        mapper.writeValue(file, this)
    }

    companion object {
        @Suppress("UNCHECKED_CAST")
        fun read(file: File, mapper: ObjectMapper): Stuff? = try {
            mapper.readValue<Stuff>(file, Stuff::class.java)
        } catch (e: IOException) {
            null
        }

        /**
         * Returns a sorted version of the given data map, such that subreddits are sorted alphabetically, and words
         * are sorted by number of occurrences first and alphabetically second.
         */
        private fun sort(data: SubredditData): SubredditData {
            data.replaceAll { _, wordMap ->
                wordMap.toList().sortedWith(kotlin.Comparator { o1, o2 ->
                    val c = o2.second.compareTo(o1.second)
                    if (c != 0) {
                        c
                    } else {
                        o1.first.compareTo(o2.first)
                    }
                }).toMap().toMutableMap()
            }
            return data.toList().sortedBy { it.first }.toMap().toMutableMap()
        }
    }
}
