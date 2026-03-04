package az.nizami.smartdirectaze.instagram.internal;

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
public class InstagramWebhookController {

    private final String VERIFY_TOKEN;

    public InstagramWebhookController(@Value( "${instagram.webhook.verify-token}")String verifyToken) {
        VERIFY_TOKEN = verifyToken;
    }

    @GetMapping(value = "/alive")
    public ResponseEntity<String> alive() {
           return ResponseEntity.status(HttpStatus.OK).body("<h3>Smart Direct is Here</h3>");
    }

    @GetMapping
    public ResponseEntity<String> verifyWebhook(
            @RequestParam("hub.mode") String mode,
            @RequestParam("hub.verify_token") String token,
            @RequestParam("hub.challenge") String challenge) {

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
        return ResponseEntity.ok().build();
    }
}