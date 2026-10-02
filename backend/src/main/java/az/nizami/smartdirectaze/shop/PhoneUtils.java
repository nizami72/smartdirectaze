package az.nizami.smartdirectaze.shop;

public class PhoneUtils {

    /**
     * Keeps only the phone digits: "+994 55 111-22-33" -> "994551112233", "994551112233@c.us" -> "994551112233".
     */
    public static String digits(String phoneOrChatId) {
        if (phoneOrChatId == null) {
            return "";
        }
        int at = phoneOrChatId.indexOf('@');
        String phone = at >= 0 ? phoneOrChatId.substring(0, at) : phoneOrChatId;
        return phone.replaceAll("\\D", "");
    }

    /**
     * Full international digits of a phone typed by a merchant, or null when it cannot be a real number.
     * Azerbaijan: "050 467 99 33", "50 467 99 33", "+994 50 467 99 33" -> "994504679933"; must be 994 + 9 digits.
     * Other countries: 11-15 digits with the country code.
     */
    public static String normalize(String phone) {
        String d = digits(phone);
        if (d.startsWith("00")) {
            d = d.substring(2);
        }
        if (d.length() == 10 && d.startsWith("0")) {
            d = "994" + d.substring(1);
        } else if (d.length() == 9 && !d.startsWith("994")) {
            d = "994" + d;
        }
        if (d.startsWith("994")) {
            return d.length() == 12 ? d : null;
        }
        return d.length() >= 11 && d.length() <= 15 ? d : null;
    }

    /**
     * Readable number for messages: "994504679933" -> "+994 50 467 99 33". Other countries: "+" and the digits.
     */
    public static String pretty(String phoneOrChatId) {
        String d = digits(phoneOrChatId);
        if (d.length() == 12 && d.startsWith("994")) {
            return String.format("+994 %s %s %s %s", d.substring(3, 5), d.substring(5, 8), d.substring(8, 10), d.substring(10));
        }
        return d.isEmpty() ? "—" : "+" + d;
    }
}
