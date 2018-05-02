package clockvapor.redditanalyzer.scraper

import clockvapor.redditanalyzer.common.Stuff
import net.dean.jraw.models.Comment

fun Stuff.add(subreddit: String, comment: Comment, mode: Scraper.Mode) {
    if (ids.add(comment.id)) {
        val subredditMap = data.getOrPut(subreddit.toLowerCase(), ::mutableMapOf)
        val wordMap = comment.body.getRedditCommentWordMap(mode).toMutableMap()
        StringUtils.addToWordMap(subredditMap, wordMap)
    }
}
