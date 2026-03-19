package az.nizami.smartdirectaze.telegram;

import az.nizami.smartdirectaze.telegram.dto.ChannelType;

public interface MessageSender {
    void sendMessage(String recipientId, String text);
    boolean supports(ChannelType type); // Чтобы понимать, какой бин вызывать
}