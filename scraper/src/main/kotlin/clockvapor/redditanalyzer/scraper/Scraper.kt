package clockvapor.redditanalyzer.scraper

import clockvapor.redditanalyzer.common.Stuff
import clockvapor.redditanalyzer.common.error
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory
import com.xenomachina.argparser.ArgParser
import com.xenomachina.argparser.default
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
    private const val CLIENT_ID = "clientId"
    private const val CLIENT_SECRET = "clientSecret"
    private const val APP_ID = "appId"
    private const val APP_VERSION = "appVersion"
    private const val USERNAME = "username"

    @JvmStatic
    fun main(args: Array<String>): Unit = mainBody {
        val options = ArgParser(args).parseInto(Scraper::Options)
        val yaml = ObjectMapper(YAMLFactory())
        val config = yaml.readValue<Map<*, *>>(options.configFile, Map::class.java)
        validateConfig(config)
        val userAgent = UserAgent(System.getProperty("os.name"), config[APP_ID].toString(),
            config[APP_VERSION].toString(), config[USERNAME].toString())
        val networkAdapter = OkHttpNetworkAdapter(userAgent)
        val credentials = Credentials.userless(config[CLIENT_ID].toString(), config[CLIENT_SECRET].toString(),
            UUID.randomUUID())
        val reddit = OAuthHelper.automatic(networkAdapter, credentials)
        val json = ObjectMapper()
        val stuff = Stuff.readSafe(options.dataFile, json) ?: Stuff()

        for (subreddit in options.subreddits) {
            val submissionsPerPage = min(options.submissionLimit, Paginator.DEFAULT_LIMIT)
            val paginator = reddit.subreddit(subreddit).posts().sorting(SubredditSort.TOP)
                .timePeriod(options.timePeriod).limit(submissionsPerPage).build()
            var numSubmissions = 0
            try {
                listing@ for (listing in paginator) {
                    for (submission in listing) {
                        val rootNode = reddit.submission(submission.id).comments(
                            CommentsRequest(sort = CommentSort.TOP, limit = options.commentLimit, depth = 1))
                        for (reply in rootNode.replies) {
                            stuff.add(subreddit, reply.subject, options.mode)
                        }
                        numSubmissions++
                        println("$numSubmissions submissions processed in /r/$subreddit")
                        if (numSubmissions >= options.submissionLimit) break@listing
                    }
                }
            } catch (e: Exception) {
                System.err.println("exception thrown while fetching submissions in /r/$subreddit:")
                e.printStackTrace()
            }
            println("finished with /r/$subreddit")
        }

        stuff.apply {
            data = Stuff.sort(data)
            write(options.dataFile, json)
        }
    }

    private fun validateConfig(config: Map<*, *>) {
        for (key in listOf(CLIENT_ID, CLIENT_SECRET, APP_ID, APP_VERSION, USERNAME)) {
            if (!config.containsKey(key)) {
                error("config file missing \"$key\" entry")
            }
        }
    }

    private class Options(parser: ArgParser) {
        val configFile by parser.storing("-c", help = "configuration yaml file", transform = ::File)
        val dataFile by parser.storing("-d", help = "file to store output data in", transform = ::File)

        val submissionLimit by parser.storing("-s", help = "number of top submissions to fetch per subreddit",
            transform = String::toInt)

        val commentLimit by parser.storing("-C", help = "number of top comments to fetch per submission",
            transform = String::toInt)

        val mode by parser.storing("-m", help = "mode of operation (default, comment)") {
            Mode.valueOf(this.toUpperCase())
        }.default(Mode.DEFAULT)

        val timePeriod by parser.storing("-p", help = "time period (all, year, month, week, day, hour)") {
            TimePeriod.valueOf(this.toUpperCase())
        }.default(TimePeriod.MONTH)

        val subreddits by parser.positionalList("SUBREDDITS", "list of subreddits to scrape")
    }

    enum class Mode {
        /**
         * DEFAULT mode counts words as many times as they appear in comments.
         */
        DEFAULT,

        /**
         * COMMENT mode only counts each word one time per comment.
         */
        COMMENT
    }
}
