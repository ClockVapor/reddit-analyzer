package clockvapor.redditanalyzer.scraper

import org.junit.Assert
import org.junit.Test

class StringUtilsTest {
    @Test
    fun testStripLinks() {
        val text = "my link"
        val textWithLinks = "[$text](https://www.google.com)"
        Assert.assertEquals(text.getWordMap(), textWithLinks.stripLinks().getWordMap())
    }

    @Test
    fun testStripLinks2() {
        val text = "my text"
        Assert.assertEquals(text.getWordMap(), text.stripLinks().getWordMap())
    }

    @Test
    fun testStripLinks3() {
        val text = "[my text"
        Assert.assertEquals(text.getWordMap(), text.stripLinks().getWordMap())
    }

    @Test
    fun testStripLinks4() {
        val text = "[my text]"
        Assert.assertEquals(text, text.stripLinks())
    }

    @Test
    fun testStripLinks5() {
        val text = "[my text](https://www.google.com"
        Assert.assertEquals(text, text.stripLinks())
    }

    @Test
    fun testStripLinks6() {
        val text = "[my text]  (https://www.google.com"
        Assert.assertEquals(text, text.stripLinks())
    }

    @Test
    fun testStripLinks7() {
        val text = "[my text]  (https://www.google.com\\)"
        Assert.assertEquals(text, text.stripLinks())
    }

    @Test
    fun testStripLinks8() {
        val text = "[my text]\n\n(https://www.google.com)"
        Assert.assertEquals(text, text.stripLinks())
    }

    @Test
    fun testStripLinks9() {
        val text = "[my text]a(https://www.google.com)"
        Assert.assertEquals(text, text.stripLinks())
    }

    @Test
    fun testStripLinks10() {
        val text = "\\[my text](https://www.google.com)"
        Assert.assertEquals(text, text.stripLinks())
    }

    @Test
    fun testStripLinks11() {
        val text = "[my text\\](https://www.google.com)"
        Assert.assertEquals(text, text.stripLinks())
    }

    @Test
    fun testStripLinks12() {
        val text = "[my text]\\(https://www.google.com)"
        Assert.assertEquals(text, text.stripLinks())
    }

    @Test
    fun testStripLinks13() {
        val text = "my link"
        val textWithLinks = "[$text]   (https://www.google.com)"
        Assert.assertEquals(text, textWithLinks.stripLinks())
    }

    @Test
    fun testStripLinks14() {
        val text = "my link"
        val textWithLinks = "[$text]  \n   (https://www.google.com)"
        Assert.assertEquals(text, textWithLinks.stripLinks())
    }

    @Test
    fun testStripLinks15() {
        val text = "my link\\]"
        val textWithLinks = "[$text](https://www.google.com)"
        Assert.assertEquals(text, textWithLinks.stripLinks())
    }

    @Test
    fun testStripLinks16() {
        val text = "my link"
        val textWithLinks = "[$text](https://www.google.com?foo=escaped\\))"
        Assert.assertEquals(text, textWithLinks.stripLinks())
    }

    @Test
    fun testStripLinksMultipleLinks() {
        val text = "my link"
        val text2 = "my other link"
        val textWithLinks = "[$text](https://www.google.com)[$text2](https://www.duckduckgo.com)"
        Assert.assertEquals(
            StringUtils.mergeWordMaps(text.getWordMap(), text2.getWordMap()),
            textWithLinks.stripLinks().getWordMap()
        )
    }

    @Test
    fun testStripLinksMultipleLinks2() {
        val text = "my link"
        val textWithLinks = "[not a link][$text](https://www.duckduckgo.com)"
        Assert.assertEquals(
            "[not a link] $text".getWordMap(),
            textWithLinks.stripLinks().getWordMap()
        )
    }

    @Test
    fun testSplitRedditCommentIntoWords() {
        // ni🅱️🅱️as
        val comment = "ni\uD83C\uDD71️\uD83C\uDD71️️as"
        Assert.assertEquals(listOf(comment), comment.splitRedditCommentIntoWords())
    }

    // maybe do this later if i add a word whitelist or something
    /*@Test
    fun testSplitRedditCommentIntoWords2() {
        val comment = "( ͡° ͜ʖ ͡°)"
        Assert.assertEquals(listOf(comment), comment.splitRedditCommentIntoWords())
    }*/

    @Test
    fun testSplitRedditCommentIntoWords3() {
        val comment = "test\n\nfoo"
        Assert.assertEquals(listOf("test", "foo"), comment.splitRedditCommentIntoWords())
    }

    @Test
    fun testSplitRedditCommentIntoWords4() {
        val comment = "there were 123,456,789 of them"
        Assert.assertEquals(
            comment.split(StringUtils.whitespaceRegex).toSet(),
            comment.splitRedditCommentIntoWords().toSet()
        )
    }

    @Test
    fun testSplitRedditCommentIntoWords5() {
        val comment = "there were 123,456,789, of them"
        val comment2 = "there were 123,456,789 of them"
        Assert.assertEquals(
            comment2.split(StringUtils.whitespaceRegex).toSet(),
            comment.splitRedditCommentIntoWords().toSet()
        )
    }

    @Test
    fun testSplitRedditCommentIntoWords6() {
        val comment = "foo /r/subreddit bar"
        Assert.assertEquals(
            comment.split(StringUtils.whitespaceRegex).toSet(),
            comment.splitRedditCommentIntoWords().toSet()
        )
    }
}
