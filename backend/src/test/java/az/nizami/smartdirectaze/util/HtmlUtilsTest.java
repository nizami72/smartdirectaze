package az.nizami.smartdirectaze.util;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class HtmlUtilsTest {

    @Test
    public void testConvertMdToTelegramHtml() {
        // Test basic formatting
        assertEquals("<b>Bold</b>", HtmlUtils.convertMdToTelegramHtml("**Bold**"));
        assertEquals("<i>Italic</i>", HtmlUtils.convertMdToTelegramHtml("*Italic*"));
        assertEquals("<i>Italic2</i>", HtmlUtils.convertMdToTelegramHtml("_Italic2_"));
        assertEquals("<code>Code</code>", HtmlUtils.convertMdToTelegramHtml("`Code`"));

        // Test mixed formatting
        assertEquals("<b>Bold</b> and <i>Italic</i> and <code>Code</code>", 
            HtmlUtils.convertMdToTelegramHtml("**Bold** and *Italic* and `Code`"));

        // Test escaping
        assertEquals("If &lt; 5 &amp; &gt; 3", HtmlUtils.convertMdToTelegramHtml("If < 5 & > 3"));
        assertEquals("<b>If &lt; 5</b>", HtmlUtils.convertMdToTelegramHtml("**If < 5**"));

        // Test code blocks
        String mdCodeBlock = "```\nSystem.out.println(\"Hello\");\n```";
        String expectedHtmlCodeBlock = "<pre>System.out.println(\"Hello\");</pre>";
        assertEquals(expectedHtmlCodeBlock, HtmlUtils.convertMdToTelegramHtml(mdCodeBlock));

        // Test nested or complex scenarios (basic support)
        assertEquals("<b><i>Bold and Italic</i></b>", HtmlUtils.convertMdToTelegramHtml("***Bold and Italic***"));
    }
}
