package az.nizami.smartdirectaze.shop.entities;

public enum ChannelStatus {
    NOT_CONFIGURED,
    WAITING_QR,
    CONNECTED,
    DISCONNECTED,
    INVALID_TOKEN,
    PENDING_ACTIVATION,
    WAITING_FOR_INSTANCE // just created, no channel type, no instance assigned yet
}