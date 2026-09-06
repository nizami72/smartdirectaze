package az.nizami.smartdirectaze.telegram.masterbot.service;

import lombok.Getter;

@Getter
public enum Command {
    START("/start"),
    REGISTER_NEW_BOT("/register_new_bot"),
    REGISTER_NEW_SHOP("/register_new_shop"),
    LOAD_("/load_"),
    SETUP_SHOP("/setup_shop"),


    ;

    public final String command;

    Command(String command) {
        this.command = command;
    }
}
