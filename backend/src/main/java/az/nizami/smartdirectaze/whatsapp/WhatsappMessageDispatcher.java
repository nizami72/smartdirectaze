package az.nizami.smartdirectaze.whatsapp;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.Set;
import java.util.concurrent.*;

/** Single backend only. The database holds the queue; the pool holds at most four active chats. */
@Component
@Slf4j
public class WhatsappMessageDispatcher {
    private final IncomingWhatsappMessageService inbox;
    private final IncomingWhatsappMessageRepository repository;
    private final ApplicationEventPublisher publisher;
    private final ExecutorService workers = Executors.newFixedThreadPool(4);
    private final Set<Long> active = ConcurrentHashMap.newKeySet();

    public WhatsappMessageDispatcher(IncomingWhatsappMessageService inbox,
                                     IncomingWhatsappMessageRepository repository,
                                     ApplicationEventPublisher publisher) {
        this.inbox = inbox;
        this.repository = repository;
        this.publisher = publisher;
    }

    @PostConstruct
    public void recover() {
        int count = repository.failInterrupted();
        if (count > 0) log.error("WhatsApp inbox: {} interrupted messages require manual review; affected chats blocked", count);
    }

    @Scheduled(fixedDelay = 250)
    public synchronized void dispatch() {
        int capacity = 4 - active.size();
        if (capacity <= 0) return;
        for (IncomingWhatsappMessage message : inbox.ready(capacity)) {
            if (!active.add(message.getId())) continue;
            try {
                repository.setStatus(message.getId(), "PROCESSING");
                workers.execute(() -> process(message));
            } catch (RuntimeException e) {
                active.remove(message.getId());
                log.error("WhatsApp inbox dispatch failed, receipt={}; manual review required", message.getId());
                // PROCESSING remains blocked, never blindly replay after a dispatch failure.
            }
        }
    }

    private void process(IncomingWhatsappMessage message) {
        try {
            // Synchronous listener: completes AI and sending before releasing this chat.
            publisher.publishEvent(message.event());
            repository.setStatus(message.getId(), "DONE");
        } catch (Exception e) {
            try {
                repository.setStatus(message.getId(), "FAILED");
            } catch (Exception ignored) {
                // PROCESSING also blocks the chat if the database is unavailable.
            }
            log.error("WhatsApp processing failed, receipt={}; chat blocked for manual review", message.getId());
        } finally {
            active.remove(message.getId());
        }
    }

    @PreDestroy
    public void close() {
        workers.shutdown();
        try {
            if (!workers.awaitTermination(5, TimeUnit.SECONDS)) workers.shutdownNow();
        } catch (InterruptedException e) {
            workers.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }
}
