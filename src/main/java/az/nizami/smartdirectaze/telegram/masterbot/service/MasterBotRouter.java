package az.nizami.smartdirectaze.telegram.masterbot.service;

import az.nizami.smartdirectaze.fakedata.FakeDataService;
import az.nizami.smartdirectaze.telegram.dto.AdminState;
import az.nizami.smartdirectaze.telegram.masterbot.handler.AdminStateHandler;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Log4j2
public class MasterBotRouter {

    private final Map<AdminState, AdminStateHandler> handlers;
    private final AdminSessionService sessionService;
    private final FakeDataService fakeDataService;

    public MasterBotRouter(List<AdminStateHandler> handlerList, AdminSessionService sessionService, FakeDataService fakeDataService) {
        this.handlers = handlerList.stream()
                .collect(Collectors.toMap(AdminStateHandler::getHandledState, h -> h));
        this.sessionService = sessionService;
        this.fakeDataService = fakeDataService;
    }

    public void processUpdate(Long chatId, String text) {
        if ("/start".equals(text)) {
            sessionService.reset(chatId);
        } else if (text.startsWith("/load_")) {
            log.debug("Chat ID [{}]", chatId);
            try {
                fakeDataService.generateFakeData(Long.parseLong(text.split("_")[1]));
            } catch (Exception e) {
                log.error("Failed to load fake data for chat [{}]", chatId, e);
            }
        }
        AdminState currentState = sessionService.getState(chatId);
        AdminStateHandler handler = handlers.get(currentState);
        if (handler != null) {
            handler.handle(chatId, text);
        }
    }
}

