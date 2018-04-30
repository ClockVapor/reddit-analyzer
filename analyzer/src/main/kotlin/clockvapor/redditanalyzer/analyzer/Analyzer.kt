package clockvapor.redditanalyzer.analyzer

import com.xenomachina.argparser.ArgParser
import com.xenomachina.argparser.mainBody
import java.io.File

object Analyzer {
    fun main(args: Array<String>) = mainBody {
        val options = ArgParser(args).parseInto(::Options)
    }

    private class Options(parser: ArgParser) {
        val file by parser.storing("-d", "--data", help = "path to data file") { File(this) }
    }
}
