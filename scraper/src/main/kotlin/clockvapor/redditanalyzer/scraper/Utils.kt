package clockvapor.redditanalyzer.scraper

import clockvapor.redditanalyzer.common.Stuff
import net.dean.jraw.models.Comment

fun Stuff.add(subreddit: String, comments: Iterable<Comment>) {
    val subredditMap = data.getOrPut(subreddit.toLowerCase(), ::mutableMapOf)
    for (comment in comments) {
        if (ids.add(comment.id)) {
            val wordMap = comment.body.getRedditCommentWordMap()
            StringUtils.addToWordMap(subredditMap, wordMap)
        }
    }
}
