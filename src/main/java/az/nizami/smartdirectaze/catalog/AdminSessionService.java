package az.nizami.smartdirectaze.catalog;

public interface AdminSessionService {

    AdminState getState(Long chatId);
    void updateState(Long chatId, AdminState state);
    void reset(Long chatId);
}
