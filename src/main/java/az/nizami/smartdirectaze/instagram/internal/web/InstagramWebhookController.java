package az.nizami.smartdirectaze.instagram.internal.web;

import az.nizami.smartdirectaze.catalog.ProductService;
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
@RequestMapping("/webhooks/instagram1")
@Log4j2
public class InstagramWebhookController {

    private final String VERIFY_TOKEN;
    private final InstagramClient instagramClient;
    private final ProductService productService;

    public InstagramWebhookController(@Value("${instagram.webhook.verify-token}") String verifyToken, InstagramClient instagramClient, ProductService productService) {
        VERIFY_TOKEN = verifyToken;
        this.instagramClient = instagramClient;
        this.productService = productService;
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
        productService.synchroniseProducts();
        return ResponseEntity.ok().build();
    }
}