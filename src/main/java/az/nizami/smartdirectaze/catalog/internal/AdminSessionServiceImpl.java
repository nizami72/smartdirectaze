package az.nizami.smartdirectaze.catalog.internal;

import az.nizami.smartdirectaze.catalog.AdminSessionService;
import az.nizami.smartdirectaze.catalog.entities.AdminSessionEntity;
import az.nizami.smartdirectaze.catalog.repo.AdminSessionRepository;
import az.nizami.smartdirectaze.catalog.AdminState;
import org.springframework.stereotype.Service;

@Service
public class AdminSessionServiceImpl implements AdminSessionService {

    private final AdminSessionRepository repository;

    public AdminSessionServiceImpl(AdminSessionRepository repository) {
        this.repository = repository;
    }

    public AdminState getState(Long chatId) {
        return repository.findById(chatId)
                .map(AdminSessionEntity::getState)
                .orElse(AdminState.START);
    }

    public void updateState(Long chatId, AdminState state) {
        AdminSessionEntity session = repository.findById(chatId).orElse(new AdminSessionEntity());
        session.setChatId(chatId);
        session.setState(state);
        repository.save(session);
    }

    public void reset(Long chatId) {
        repository.findById(chatId).ifPresent(session -> {
            session.setState(AdminState.START);
            repository.save(session);
        });
    }
}
