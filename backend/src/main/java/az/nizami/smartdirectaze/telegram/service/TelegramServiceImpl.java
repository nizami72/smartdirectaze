package az.nizami.smartdirectaze.telegram.service;

import az.nizami.smartdirectaze.telegram.masterbot.TelegramApiClient;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class TelegramServiceImpl implements TelegramService {

    private final TelegramApiClient telegramApiClient;

    @Override
    public String getBotUsername(String token) {
        return telegramApiClient.getBotUsername(token);
    }
}
