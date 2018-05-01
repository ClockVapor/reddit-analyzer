package clockvapor.redditanalyzer.scraper

import clockvapor.redditanalyzer.common.Stuff
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory
import com.xenomachina.argparser.ArgParser
import com.xenomachina.argparser.mainBody
import net.dean.jraw.http.OkHttpNetworkAdapter
import net.dean.jraw.http.UserAgent
import net.dean.jraw.models.CommentSort
import net.dean.jraw.models.SubredditSort
import net.dean.jraw.models.TimePeriod
import net.dean.jraw.oauth.Credentials
import net.dean.jraw.oauth.OAuthHelper
import net.dean.jraw.pagination.Paginator
import net.dean.jraw.references.CommentsRequest
import java.io.File
import java.util.*
import kotlin.math.min

object Scraper {
    private const val CONFIG_FILE_PATH = "config.yml"
    private const val ID = "id"
    private const val SECRET = "secret"
    private const val USERNAME = "username"

    @JvmStatic
    fun main(args: Array<String>) = mainBody {
        val options = ArgParser(args).parseInto(Scraper::Options)
        val yaml = ObjectMapper(YAMLFactory())
        val config = yaml.readValue<Map<*, *>>(File(CONFIG_FILE_PATH), Map::class.java)
        validateConfig(config)
        val userAgent =
            UserAgent(System.getProperty("os.name"), "clockvapor.redditanalyzer", "1.0", config[USERNAME].toString())
        val networkAdapter = OkHttpNetworkAdapter(userAgent)
        val credentials = Credentials.userless(config[ID].toString(), config[SECRET].toString(), UUID.randomUUID())
        val reddit = OAuthHelper.automatic(networkAdapter, credentials)
        val json = ObjectMapper()
        val stuff = Stuff.readSafe(options.file, json) ?: Stuff()

        for (subreddit in options.subreddits) {
            val submissionsPerPage = min(options.submissionLimit, Paginator.DEFAULT_LIMIT)
            val paginator = reddit.subreddit(subreddit).posts().sorting(SubredditSort.TOP)
                .timePeriod(TimePeriod.ALL).limit(submissionsPerPage).build()
            var numSubmissions = 0
            try {
                listing@ for (listing in paginator) {
                    for (submission in listing) {
                        val rootNode = reddit.submission(submission.id).comments(
                            CommentsRequest(sort = CommentSort.TOP, limit = options.commentLimit, depth = 1))
                        for (reply in rootNode.replies) {
                            stuff.add(subreddit, reply.subject)
                        }
                        numSubmissions++
                        println("$numSubmissions submissions processed")
                        if (numSubmissions >= options.submissionLimit) break@listing
                    }
                }
            } catch (e: Exception) {
                System.err.println("exception thrown while fetching submissions in /r/$subreddit:")
                e.printStackTrace()
                continue
            }
        }

        stuff.write(options.file, json)
    }

    private fun validateConfig(config: Map<*, *>) {
        for (key in listOf(ID,
            SECRET,
            USERNAME)) {
            if (!config.containsKey(key)) {
                throw RuntimeException("config file missing \"$key\" entry")
            }
        }
    }

    private class Options(parser: ArgParser) {
        val file by parser.storing("-d", "--data", help = "path to data file") { File(this) }
        val submissionLimit by parser.storing("-s", help = "number of top submissions to fetch per subreddit") {
            this.toInt()
        }
        val commentLimit by parser.storing("-c", help = "number of top comments to fetch per submission") {
            this.toInt()
        }
        val subreddits by parser.positionalList("SUBREDDITS", "list of subreddits to scrape")
    }
}
