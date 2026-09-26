package az.nizami.smartdirectaze.whatsapp;

/**
 * Green API "stateInstanceChanged": the shop's number got connected, disconnected, blocked...
 *
 * @param state authorized, notAuthorized, blocked, yellowCard, sleepMode, starting
 */
public record WhatsappStateChangedEvent(String instanceId, String state) {
}
