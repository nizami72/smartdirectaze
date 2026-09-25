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
}
