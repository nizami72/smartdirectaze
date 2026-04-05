package az.nizami.smartdirectaze.util;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class HtmlUtils {

    /**
     * Converts common Markdown formatting to Telegram-supported HTML.
     * Escapes HTML special characters before applying Markdown-like transformations.
     *
     * @param markdown The input string in Markdown format.
     * @return The converted string in HTML format.
     */
    public static String convertMdToTelegramHtml(String markdown) {
        if (markdown == null || markdown.isBlank()) {
            return markdown;
        }

        // 1. Escape HTML special characters
        String html = markdown
                .replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;");

        List<String> placeholders = new ArrayList<>();
        
        // 2. Placeholder for Triple Backticks (Code Block): ```text``` -> <pre>text</pre>
        Pattern prePattern = Pattern.compile("```(.*?)```", Pattern.DOTALL);
        Matcher preMatcher = prePattern.matcher(html);
        StringBuilder sbPre = new StringBuilder();
        while (preMatcher.find()) {
            String content = preMatcher.group(1).trim();
            String placeholder = "PXPREPX" + placeholders.size() + "X";
            placeholders.add("<pre>" + content + "</pre>");
            preMatcher.appendReplacement(sbPre, placeholder);
        }
        preMatcher.appendTail(sbPre);
        html = sbPre.toString();

        // 3. Placeholder for Monospace: `text` -> <code>text</code>
        Pattern codePattern = Pattern.compile("`(.*?)`", Pattern.DOTALL);
        Matcher codeMatcher = codePattern.matcher(html);
        StringBuilder sbCode = new StringBuilder();
        while (codeMatcher.find()) {
            String content = codeMatcher.group(1);
            String placeholder = "PXCODEPX" + placeholders.size() + "X";
            placeholders.add("<code>" + content + "</code>");
            codeMatcher.appendReplacement(sbCode, placeholder);
        }
        codeMatcher.appendTail(sbCode);
        html = sbCode.toString();

        // 4. Convert Bold/Italic
        // We use simple non-greedy matching. To handle nested stars, 
        // we replace *** first, then ** then *.
        html = html.replaceAll("\\*\\*\\*(.*?)\\*\\*\\*", "<b><i>$1</i></b>");
        html = html.replaceAll("\\*\\*(.*?)\\*\\*", "<b>$1</b>");
        html = html.replaceAll("(?<!\\*)\\*(?!\\*)(.*?)(?<!\\*)\\*(?!\\*)", "<i>$1</i>");
        
        html = html.replaceAll("__(.*?)__", "<b>$1</b>");
        html = html.replaceAll("(?<!_)_(?!_)(.*?)(?<!_)_(?!_)", "<i>$1</i>");

        // 5. Restore Placeholders
        for (int i = 0; i < placeholders.size(); i++) {
            String prePlaceholder = "PXPREPX" + i + "X";
            html = html.replace(prePlaceholder, placeholders.get(i));
            String codePlaceholder = "PXCODEPX" + i + "X";
            html = html.replace(codePlaceholder, placeholders.get(i));
        }

        return html;
    }
}
