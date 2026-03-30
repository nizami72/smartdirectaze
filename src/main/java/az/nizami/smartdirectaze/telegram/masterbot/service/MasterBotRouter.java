package az.nizami.smartdirectaze.telegram.masterbot.service;

import az.nizami.smartdirectaze.catalog.ProductService;
import az.nizami.smartdirectaze.fakedata.FakeDataService;
import az.nizami.smartdirectaze.shared.Util;
import az.nizami.smartdirectaze.telegram.dto.AdminState;
import az.nizami.smartdirectaze.telegram.masterbot.handler.AdminStateHandler;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static az.nizami.smartdirectaze.telegram.masterbot.service.Command.*;

@Service
@Log4j2
public class MasterBotRouter {

    //<editor-fold desc="Fields">
    private final Map<AdminState, AdminStateHandler> handlers;
    private final AdminSessionService sessionService;
    private final FakeDataService fakeDataService;
    private final ProductService productService;
    //</editor-fold>

    //<editor-fold desc="Constructor">
    public MasterBotRouter(List<AdminStateHandler> handlerList, AdminSessionService sessionService, FakeDataService fakeDataService, ProductService productService) {
        this.handlers = handlerList.stream()
                .collect(Collectors.toMap(AdminStateHandler::getHandledState, h -> h));
        this.sessionService = sessionService;
        this.fakeDataService = fakeDataService;
        this.productService = productService;
    }
    //</editor-fold>

    public String processUpdate(Update update) {
        Long ownerId = update.getMessage().getFrom().getId();
        String text = update.getMessage().getText();

        if (REGISTER_NEW_SHOP.getCommand().equals(text)) {
            sessionService.reset(ownerId);
        } else if (text.startsWith(LOAD_.getCommand())) {
            log.debug("Chat ID [{}]", ownerId);
            if (!Util.checkIfAdmin(ownerId)) return "You are not admin";
            Long id = Long.parseLong(text.split("_")[1]);
            boolean ifShopExist = productService.isShopExist(id);
            if (ifShopExist) {
                try {
                    fakeDataService.generateFakeData(Long.parseLong(text.split("_")[1]));
                    return "Fake data generated";
                } catch (Exception e) {
                    log.error("Failed to load fake data for chat [{}]", ownerId, e);
                    return "Failed to load fake data";
                }
            } else return "Shop not found";

        } else if (text.startsWith(START.getCommand())) {
            sessionService.reset(ownerId);
            log.debug("Chat id [{}] and text [{}]", ownerId, text);
        }

        AdminState currentState = sessionService.getState(ownerId);
        AdminStateHandler handler = handlers.get(currentState);
        if (handler != null) {
            handler.handle(ownerId, text);
        }
        return "Done";
    }

}

