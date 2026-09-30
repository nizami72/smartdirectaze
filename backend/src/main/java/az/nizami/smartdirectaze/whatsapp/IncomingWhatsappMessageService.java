package az.nizami.smartdirectaze.whatsapp;

import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
@RequiredArgsConstructor
public class IncomingWhatsappMessageService {
    private final IncomingWhatsappMessageRepository repository;

    // Commit receipts in acceptance order on this single backend. No transaction spans AI calls.
    public synchronized void accept(String messageId, WhatsappMessageReceivedEvent event) {
        if (repository.existsByInstanceIdAndMessageId(event.instanceId(), messageId)) return;
        try {
            repository.saveAndFlush(new IncomingWhatsappMessage(messageId, event));
        } catch (DataIntegrityViolationException e) {
            // Constraint is the authority, not the preliminary lookup. Repository transaction has ended.
            if (!repository.existsByInstanceIdAndMessageId(event.instanceId(), messageId)) throw e;
        }
    }

    public synchronized List<IncomingWhatsappMessage> ready(int limit) {
        return repository.findReady(PageRequest.of(0, limit));
    }
}
