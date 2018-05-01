package clockvapor.redditanalyzer.scraper

import clockvapor.redditanalyzer.common.Stuff
import net.dean.jraw.models.PublicContribution

fun Stuff.add(subreddit: String, contribution: PublicContribution<*>) {
    val subredditMap = data.getOrPut(subreddit.toLowerCase(), ::mutableMapOf)
    if (ids.add(contribution.id)) {
        val wordMap = contribution.body!!.getRedditCommentWordMap()
        StringUtils.addToWordMap(subredditMap, wordMap)
    }
}
