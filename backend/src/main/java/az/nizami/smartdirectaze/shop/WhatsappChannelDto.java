package az.nizami.smartdirectaze.shop;

import java.util.Set;

/**
 * WhatsApp (Green API) channel of a shop: which shop answers, with which credentials and to whom.
 *
 * @param testPhones digits only, e.g. "994551112233"
 */
public record WhatsappChannelDto(Long shopId, String instanceId, String apiToken, AiMode aiMode, Set<String> testPhones) {

    /**
     * @param chatId WhatsApp chat id, e.g. "994551112233@c.us"
     */
    public boolean aiAnswers(String chatId) {
        return switch (aiMode) {
            case ON -> true;
            case OFF -> false;
            case TEST -> testPhones.contains(PhoneUtils.digits(chatId));
        };
    }
}
