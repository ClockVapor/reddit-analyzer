package clockvapor.redditanalyzer.scraper

import org.junit.Assert
import org.junit.Test

class UtilsTest {
    @Test
    fun testStripLinks() {
        val text = "my link"
        val textWithLinks = "[$text](https://www.google.com)"
        Assert.assertEquals(text, textWithLinks.stripLinks().trim())
    }

    @Test
    fun testStripLinks2() {
        val text = "my text"
        Assert.assertEquals(text, text.stripLinks().trim())
    }

    @Test
    fun testStripLinks3() {
        val text = "[my text"
        Assert.assertEquals(text, text.stripLinks().trim())
    }

    @Test
    fun testStripLinks4() {
        val text = "[my text]"
        Assert.assertEquals(text, text.stripLinks().trim())
    }

    @Test
    fun testStripLinks5() {
        val text = "[my text](https://www.google.com"
        Assert.assertEquals(text, text.stripLinks().trim())
    }

    @Test
    fun testStripLinks6() {
        val text = "[my text]  (https://www.google.com"
        Assert.assertEquals(text, text.stripLinks().trim())
    }

    @Test
    fun testStripLinks7() {
        val text = "[my text]  (https://www.google.com\\)"
        Assert.assertEquals(text, text.stripLinks().trim())
    }

    @Test
    fun testStripLinks8() {
        val text = "[my text] \n\n (https://www.google.com)"
        Assert.assertEquals(text, text.stripLinks().trim())
    }

    @Test
    fun testStripLinks9() {
        val text = "[my text]a(https://www.google.com)"
        Assert.assertEquals(text, text.stripLinks().trim())
    }

    @Test
    fun testStripLinks10() {
        val text = "\\[my text](https://www.google.com)"
        Assert.assertEquals(text, text.stripLinks().trim())
    }

    @Test
    fun testStripLinks11() {
        val text = "[my text\\](https://www.google.com)"
        Assert.assertEquals(text, text.stripLinks().trim())
    }

    @Test
    fun testStripLinks12() {
        val text = "[my text]\\(https://www.google.com)"
        Assert.assertEquals(text, text.stripLinks().trim())
    }

    @Test
    fun testStripLinks13() {
        val text = "my link"
        val textWithLinks = "[$text]   (https://www.google.com)"
        Assert.assertEquals(text, textWithLinks.stripLinks().trim())
    }

    @Test
    fun testStripLinks14() {
        val text = "my link"
        val textWithLinks = "[$text]  \n   (https://www.google.com)"
        Assert.assertEquals(text, textWithLinks.stripLinks().trim())
    }

    @Test
    fun testStripLinks15() {
        val text = "my link\\]"
        val textWithLinks = "[$text](https://www.google.com)"
        Assert.assertEquals(text, textWithLinks.stripLinks().trim())
    }


    @Test
    fun testStripLinks16() {
        val text = "my link"
        val textWithLinks = "[$text](https://www.google.com?foo=escaped\\))"
        Assert.assertEquals(text, textWithLinks.stripLinks().trim())
    }
}
