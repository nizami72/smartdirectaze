package az.nizami.smartdirectaze.telegram.masterbot.service;

import az.nizami.smartdirectaze.telegram.masterbot.repo.AdminSessionRepository;
import az.nizami.smartdirectaze.telegram.masterbot.entity.AdminSessionEntity;
import az.nizami.smartdirectaze.telegram.dto.AdminState;
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
