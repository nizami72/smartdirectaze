package az.nizami.smartdirectaze.telegram.masterbot.handler;

import az.nizami.smartdirectaze.catalog.AdminState;

public interface AdminStateHandler {
    AdminState getHandledState();
    void handle(Long chatId, String text);
}