package az.nizami.smartdirectaze.integration;

import az.nizami.smartdirectaze.integration.dto.ChannelType;

public interface MessageSender {
    void sendMessage(String recipientId, String text);
    boolean supports(ChannelType type); // Чтобы понимать, какой бин вызывать
}