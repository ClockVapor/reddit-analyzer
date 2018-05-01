package clockvapor.redditanalyzer.scraper

import clockvapor.redditanalyzer.common.Stuff
import net.dean.jraw.models.Comment

fun Stuff.add(subreddit: String, comment: Comment, mode: Scraper.Mode) {
    val subredditMap = data.getOrPut(subreddit.toLowerCase(), ::mutableMapOf)
    if (ids.add(comment.id)) {
        val wordMap = comment.body.getRedditCommentWordMap(mode)
        StringUtils.addToWordMap(subredditMap, wordMap)
    }
}
