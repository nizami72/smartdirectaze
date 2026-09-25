package az.nizami.smartdirectaze.shop;

/**
 * Who the AI seller answers on a channel.
 */
public enum AiMode {
    /** AI is silent, the merchant answers himself */
    OFF,
    /** AI answers only the merchant's test numbers */
    TEST,
    /** AI answers every customer */
    ON
}
