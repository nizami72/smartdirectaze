package az.nizami.smartdirectaze.shop.dto.channel;

import az.nizami.smartdirectaze.shop.entities.ChannelStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WhatsAppQrResponse {
    private String qrCode;
    private ChannelStatus status;
    /** Digits of the number that scanned the QR; set once connected */
    private String connectedPhone;
    /** false = show the risk warning first; the QR is not given until the merchant confirms it */
    private Boolean riskAccepted;
}
