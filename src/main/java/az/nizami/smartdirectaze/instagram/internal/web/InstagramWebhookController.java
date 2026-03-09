package az.nizami.smartdirectaze.instagram.internal.web;

import az.nizami.smartdirectaze.instagram.internal.client.InstagramClient;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/webhooks/instagram")
@Log4j2
public class InstagramWebhookController {

    private final String VERIFY_TOKEN;
    private final InstagramClient instagramClient;

    public InstagramWebhookController(@Value("${instagram.webhook.verify-token}") String verifyToken, InstagramClient instagramClient) {
        VERIFY_TOKEN = verifyToken;
        this.instagramClient = instagramClient;
    }

    @GetMapping(value = "/alive")
    public ResponseEntity<String> alive() {
        log.debug("Alive called");
        return ResponseEntity.status(HttpStatus.OK).body("<h3>Smart Direct is Here</h3>");
    }

    @GetMapping
    public ResponseEntity<String> verifyWebhook(
            @RequestParam("hub.mode") String mode,
            @RequestParam("hub.verify_token") String token,
            @RequestParam("hub.challenge") String challenge) {

        log.debug("Webhook");
        if ("subscribe".equals(mode) && VERIFY_TOKEN.equals(token)) {
            return ResponseEntity.ok(challenge);
        } else {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
    }

    @PostMapping
    public ResponseEntity<Void> handleIncomingMessage(@RequestBody String payload) {
        // На этапе разработки лучше просто логгировать сырой JSON
        System.out.println("Incoming Webhook: " + payload);
        
        // Важно: Всегда возвращаем 200 OK максимально быстро, 
        // а обработку через ИИ выносим в отдельный поток/сервис
//        instagramClient.sendMessageToClient("17841405376618492", "Salam");
        return ResponseEntity.ok().build();
    }
}