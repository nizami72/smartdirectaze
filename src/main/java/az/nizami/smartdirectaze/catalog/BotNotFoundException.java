package az.nizami.smartdirectaze.catalog;

public class BotNotFoundException extends RuntimeException {
    public BotNotFoundException(String message) {
        super(message);
    }
}
