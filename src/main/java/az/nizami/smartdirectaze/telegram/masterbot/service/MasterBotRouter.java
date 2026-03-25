package az.nizami.smartdirectaze.telegram.masterbot.service;

import az.nizami.smartdirectaze.catalog.AdminSessionService;
import az.nizami.smartdirectaze.catalog.AdminState;
import az.nizami.smartdirectaze.telegram.masterbot.handler.AdminStateHandler;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class MasterBotRouter {

    private final Map<AdminState, AdminStateHandler> handlers;
    private final AdminSessionService sessionService;

    public MasterBotRouter(List<AdminStateHandler> handlerList, AdminSessionService sessionService) {
        this.handlers = handlerList.stream()
                .collect(Collectors.toMap(AdminStateHandler::getHandledState, h -> h));
        this.sessionService = sessionService;
    }

    public void processUpdate(Long chatId, String text) {
        if ("/start".equals(text)) {
            sessionService.reset(chatId);
        }
        AdminState currentState = sessionService.getState(chatId);
        AdminStateHandler handler = handlers.get(currentState);
        if (handler != null) {
            handler.handle(chatId, text);
        }
    }
}

