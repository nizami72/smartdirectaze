package az.nizami.smartdirectaze.whatsapp;

import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest(showSql = false)
@ActiveProfiles("test")
@Import(IncomingWhatsappMessageService.class)
@Transactional(propagation = Propagation.NOT_SUPPORTED)
class WhatsappInboxTest {
    @Autowired IncomingWhatsappMessageRepository repository;
    @Autowired IncomingWhatsappMessageService inbox;
    WhatsappMessageDispatcher dispatcher;

    WhatsappMessageReceivedEvent message(String instance, String chat, String text) {
        return new WhatsappMessageReceivedEvent(instance, chat, "Name", text, "textMessage");
    }

    @AfterEach void cleanup() {
        if (dispatcher != null) dispatcher.close();
        repository.deleteAll();
    }

    @Test void duplicateIncludingAfterRecreatingServiceIsProcessedOnlyOnce() {
        var event = message("1", "chat", "confirm");
        inbox.accept("id", event);
        inbox.accept("id", event);
        new IncomingWhatsappMessageService(repository).accept("id", event);
        assertEquals(1, repository.count());
        inbox.accept("id", message("2", "chat", "confirm"));
        assertEquals(2, repository.count());
    }

    @Test void databaseConstraintProtectsConcurrentWriters() throws Exception {
        var start = new CountDownLatch(1);
        try (var pool = Executors.newFixedThreadPool(2)) {
            Callable<Boolean> insert = () -> {
                start.await();
                try {
                    repository.saveAndFlush(new IncomingWhatsappMessage("same", message("1", "chat", "hello")));
                    return true;
                } catch (org.springframework.dao.DataIntegrityViolationException expected) {
                    return false;
                }
            };
            var a = pool.submit(insert); var b = pool.submit(insert); start.countDown();
            assertNotEquals(a.get(5, TimeUnit.SECONDS), b.get(5, TimeUnit.SECONDS));
            assertEquals(1, repository.count());
        }
    }

    @Test void sameChatOrderedOtherChatParallelAndDuplicateDoesNotInvokeConsumer() throws Exception {
        inbox.accept("a", message("1", "chat", "first"));
        inbox.accept("b", message("1", "chat", "second"));
        inbox.accept("c", message("1", "other", "parallel"));
        var entered = new CountDownLatch(2);
        var release = new CountDownLatch(1);
        var second = new CountDownLatch(1);
        var firstFinished = new CountDownLatch(1);
        var firstCount = new AtomicInteger();
        var violation = new java.util.concurrent.atomic.AtomicBoolean();
        dispatcher = new WhatsappMessageDispatcher(inbox, repository, event -> {
            var m = (WhatsappMessageReceivedEvent) event;
            if (m.text().equals("second")) {
                if (firstFinished.getCount() != 0) violation.set(true);
                second.countDown();
            } else {
                if (m.text().equals("first")) firstCount.incrementAndGet();
                entered.countDown();
                try { if (!release.await(5, TimeUnit.SECONDS)) throw new IllegalStateException("test timeout"); }
                catch (InterruptedException e) { Thread.currentThread().interrupt(); throw new RuntimeException(e); }
                if (m.text().equals("first")) firstFinished.countDown();
            }
        });
        dispatcher.dispatch();
        assertTrue(entered.await(5, TimeUnit.SECONDS));
        inbox.accept("a", message("1", "chat", "first"));
        dispatcher.dispatch();
        assertEquals(1, second.getCount());
        release.countDown();
        // Wait on committed completion, then dispatch the next head. Bounded without sleep.
        long deadline = System.nanoTime() + TimeUnit.SECONDS.toNanos(5);
        while (second.getCount() != 0 && System.nanoTime() < deadline) {
            dispatcher.dispatch();
            Thread.yield();
        }
        assertTrue(second.await(1, TimeUnit.SECONDS));
        assertFalse(violation.get());
        assertEquals(1, firstCount.get());
    }

    @Test void processingFailureBlocksChatWithoutReplayingSideEffects() throws Exception {
        inbox.accept("a", message("1", "chat", "first"));
        inbox.accept("b", message("1", "chat", "second"));
        var calls = new AtomicInteger();
        var failed = new CountDownLatch(1);
        dispatcher = new WhatsappMessageDispatcher(inbox, repository, event -> {
            calls.incrementAndGet(); failed.countDown(); throw new IllegalStateException("uncertain send");
        });
        dispatcher.dispatch();
        assertTrue(failed.await(5, TimeUnit.SECONDS));
        dispatcher.close();
        assertEquals(1, calls.get());
        assertTrue(inbox.ready(4).isEmpty());
        assertEquals("FAILED", repository.findAll().stream().filter(m -> m.getMessageId().equals("a"))
                .findFirst().orElseThrow().getStatus());
        inbox.accept("a", message("1", "chat", "first"));
        assertEquals(2, repository.count());
    }

    @Test void interruptedMessageBlocksOnlyItsChatAndIsNotReplayed() {
        inbox.accept("1", message("1", "chat", "first"));
        inbox.accept("2", message("1", "chat", "second"));
        inbox.accept("3", message("1", "other", "other"));
        var first = repository.findAll().stream().filter(m -> m.getMessageId().equals("1")).findFirst().orElseThrow();
        repository.setStatus(first.getId(), "PROCESSING");
        dispatcher = new WhatsappMessageDispatcher(inbox, repository, event -> fail("No replay"));
        dispatcher.recover();
        assertEquals("FAILED", repository.findById(first.getId()).orElseThrow().getStatus());
        assertEquals(List.of("other"), inbox.ready(4).stream().map(IncomingWhatsappMessage::getChatId).toList());
    }
}
