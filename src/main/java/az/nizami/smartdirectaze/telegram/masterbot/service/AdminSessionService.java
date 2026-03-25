package az.nizami.smartdirectaze.telegram.masterbot.service;

import az.nizami.smartdirectaze.telegram.dto.AdminState;

public interface AdminSessionService {

    AdminState getState(Long chatId);
    void updateState(Long chatId, AdminState state);
    void reset(Long chatId);
}
