package az.nizami.smartdirectaze.util;

import java.util.Arrays;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class WhatsappTextUtils {

    private static final Pattern HEADER = Pattern.compile("(?m)^#{1,6}\\s*(.+?)\\s*$");
    private static final Pattern BOLD = Pattern.compile("\\*\\*(.+?)\\*\\*|__(.+?)__");
    private static final Pattern STRIKE = Pattern.compile("~~(.+?)~~");
    private static final Pattern LINK = Pattern.compile("\\[([^\\]]+)]\\((https?://[^)\\s]+)\\)");
    private static final Pattern BULLET = Pattern.compile("(?m)^(\\s*)[*+]\\s+");
    private static final Pattern TABLE_SEPARATOR = Pattern.compile("(?m)^\\s*\\|?(\\s*:?-{2,}:?\\s*\\|)+\\s*:?-*:?\\s*$\\R?");
    private static final Pattern TABLE_ROW = Pattern.compile("(?m)^\\s*\\|(.+)\\|\\s*$");

    /**
     * Converts common Markdown formatting to WhatsApp formatting:
     * **bold** / __bold__ / # Header -> *bold*, ~~strike~~ -> ~strike~, [text](url) -> text: url,
     * table rows | a | b | -> - a — b (WhatsApp has no tables).
     */
    public static String convertMdToWhatsapp(String markdown) {
        if (markdown == null || markdown.isBlank()) {
            return markdown;
        }
        // Bullets first, so "* item" is not mistaken for italic/bold later
        String text = BULLET.matcher(markdown).replaceAll("$1- ");
        text = TABLE_SEPARATOR.matcher(text).replaceAll("");
        text = TABLE_ROW.matcher(text).replaceAll(m -> Matcher.quoteReplacement(
                "- " + String.join(" — ", Arrays.stream(m.group(1).split("\\|")).map(String::trim).toList())));
        text = LINK.matcher(text).replaceAll("$1: $2");
        text = BOLD.matcher(text).replaceAll(m -> "*" + (m.group(1) != null ? m.group(1) : m.group(2)) + "*");
        text = HEADER.matcher(text).replaceAll("*$1*");
        text = STRIKE.matcher(text).replaceAll("~$1~");
        return text;
    }
}
