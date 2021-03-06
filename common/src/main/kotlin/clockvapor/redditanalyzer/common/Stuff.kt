package clockvapor.redditanalyzer.common

import com.fasterxml.jackson.databind.ObjectMapper
import java.io.File
import java.io.IOException

class Stuff {
    var data: SubredditData = subredditData()
    var ids: MutableSet<String> = mutableSetOf()

    fun write(file: File, mapper: ObjectMapper) {
        mapper.writeValue(file, this)
    }

    companion object {
        @Suppress("UNCHECKED_CAST")
        fun readSafe(file: File, mapper: ObjectMapper): Stuff? = try {
            read(file, mapper)
        } catch (e: IOException) {
            null
        }

        @Suppress("UNCHECKED_CAST")
        fun read(file: File, mapper: ObjectMapper): Stuff = mapper.readValue<Stuff>(file, Stuff::class.java)

        /**
         * Returns a sorted version of the given data map, such that subreddits are sorted alphabetically, and words
         * are sorted by number of occurrences first and alphabetically second.
         */
        fun sort(data: SubredditData): SubredditData {
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
