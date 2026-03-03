package az.nizami.smartdirectaze.web;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/webhook")
public class InstagramStubController {

    private final String VERIFY_TOKEN = "my_secret_token_2026"; // Ваш токен
    private final InstagramService instagramService;

    public InstagramStubController(InstagramService instagramService) {
        this.instagramService = instagramService;
    }

    // Метод для верификации Webhook со стороны Meta
    @GetMapping
    public ResponseEntity<String> verifyWebhook(
            @RequestParam("hub.mode") String mode,
            @RequestParam("hub.verify_token") String token,
            @RequestParam("hub.challenge") String challenge) {

        if ("subscribe".equals(mode) && VERIFY_TOKEN.equals(token)) {
            return ResponseEntity.ok(challenge);
        }
        return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
    }

    // Прием сообщений
    @PostMapping
    public ResponseEntity<Void> handleIncomingMessage(@RequestBody String payload) {
        // На этапе стаба просто печатаем в консоль, что пришло
        System.out.println("Incoming payload: " + payload);
        
        // В реальности здесь нужно распарсить JSON и вытащить sender_id
        // Для стаба просто вызываем метод ответа
        instagramService.sendStubResponse(payload);
        
        return ResponseEntity.ok().build();
    }
}