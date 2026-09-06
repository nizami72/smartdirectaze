package az.nizami.smartdirectaze.exception;

import lombok.Getter;

@Getter
public enum ErrorMessage {

    USER_NOT_FOUND("The user with id not found", "User not registered");

    final String systemMessage;
    final String userMessage;

    ErrorMessage(String systemMessage, String userMessage) {
        this.systemMessage = systemMessage;
        this.userMessage = userMessage;
    }

}
