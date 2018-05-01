package clockvapor.redditanalyzer.scraper

import clockvapor.redditanalyzer.common.Stuff
import net.dean.jraw.models.PublicContribution

fun Stuff.add(subreddit: String, contribution: PublicContribution<*>, mode: Scraper.Mode) {
    val subredditMap = data.getOrPut(subreddit.toLowerCase(), ::mutableMapOf)
    if (ids.add(contribution.id)) {
        val wordMap = contribution.body!!.getRedditCommentWordMap(mode)
        StringUtils.addToWordMap(subredditMap, wordMap)
    }
}
