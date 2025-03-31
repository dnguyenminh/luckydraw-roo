package vn.com.fecredit.app.service.impl;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.com.fecredit.app.entity.Event;
import vn.com.fecredit.app.entity.CommonStatus;
import vn.com.fecredit.app.repository.EventRepository;
import vn.com.fecredit.app.service.EventService;
import vn.com.fecredit.app.service.base.AbstractServiceImpl;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@Transactional
public class EventServiceImpl extends AbstractServiceImpl<Event> implements EventService {

    private final EventRepository eventRepository;

    public EventServiceImpl(EventRepository eventRepository) {
        super(eventRepository);
        this.eventRepository = eventRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public List<Event> findByStatus(CommonStatus status) {
        return eventRepository.findByStatus(status);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Event> findByCode(String code) {
        return eventRepository.findByCode(code);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Event> findCurrentEvents(LocalDateTime now) {
        return eventRepository.findCurrentEvents(now);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Event> findUpcomingEvents(LocalDateTime now) {
        return eventRepository.findUpcomingEvents(now);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Event> findPastEvents(LocalDateTime now) {
        return eventRepository.findPastEvents(now);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean isEventActive(Long eventId) {
        return findById(eventId)
                .map(event -> CommonStatus.ACTIVE.equals(event.getStatus()) &&
                        event.getStartTime().isBefore(LocalDateTime.now()) &&
                        event.getEndTime().isAfter(LocalDateTime.now()))
                .orElse(false);
    }

    @Override
    public List<Event> findActiveEvents() {
        return eventRepository.findActiveEvents(LocalDateTime.now());
    }
}
