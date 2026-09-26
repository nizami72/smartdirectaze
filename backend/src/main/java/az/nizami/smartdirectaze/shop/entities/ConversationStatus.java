package az.nizami.smartdirectaze.shop.entities;

public enum ConversationStatus {
    /** The AI seller answers */
    AI,
    /** Handed over to the seller: the AI is silent until pausedUntil */
    HUMAN
}
