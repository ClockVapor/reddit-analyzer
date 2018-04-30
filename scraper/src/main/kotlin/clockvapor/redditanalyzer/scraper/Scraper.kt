package clockvapor.redditanalyzer.scraper

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory
import com.xenomachina.argparser.ArgParser
import com.xenomachina.argparser.mainBody
import net.dean.jraw.http.OkHttpNetworkAdapter
import net.dean.jraw.http.UserAgent
import net.dean.jraw.oauth.Credentials
import net.dean.jraw.oauth.OAuthHelper
import java.io.File
import java.util.*

object Scraper {
    private const val CONFIG_FILE_PATH = "config.yml"
    private const val ID = "id"
    private const val SECRET = "secret"
    private const val USERNAME = "username"

    @JvmStatic
    fun main(args: Array<String>) = mainBody {
        val options = ArgParser(args).parseInto(::Options)
        val yaml = ObjectMapper(YAMLFactory())
        val config = yaml.readValue<Map<*, *>>(File(CONFIG_FILE_PATH), Map::class.java)
        validateConfig(config)
        val userAgent =
            UserAgent(System.getProperty("os.name"), "clockvapor.redditanalyzer", "1.0", config[USERNAME].toString())
        val networkAdapter = OkHttpNetworkAdapter(userAgent)
        val credentials = Credentials.userless(config[ID].toString(), config[SECRET].toString(), UUID.randomUUID())
        val reddit = OAuthHelper.automatic(networkAdapter, credentials)
        val json = ObjectMapper()
        val stuff = Stuff.read(options.file, json) ?: Stuff()

        for (subreddit in options.subreddits) {
            val paginator = reddit.subreddit(subreddit).comments().limit(options.limit).build()
            for (listing in paginator) {
                stuff.add(subreddit, listing.children)
            }
        }

        stuff.write(options.file, json)
    }

    private fun validateConfig(config: Map<*, *>) {
        for (key in listOf(ID, SECRET, USERNAME)) {
            if (!config.containsKey(key)) {
                throw RuntimeException("config file missing \"$key\" entry")
            }
        }
    }

    private class Options(parser: ArgParser) {
        val file by parser.storing("-d", "--data", help = "path to data file") {
            File(this)
        }
        val limit by parser.storing("-l", "--limit", help = "number of comments to fetch per subreddit") {
            this.toInt()
        }
        val subreddits by parser.positionalList("SUBREDDITS", "list of subreddits to scrape")
    }
}
