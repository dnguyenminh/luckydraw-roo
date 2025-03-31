package vn.com.fecredit.app.service;

import vn.com.fecredit.app.entity.Event;
import vn.com.fecredit.app.service.base.AbstractService;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface EventService extends AbstractService<Event> {
    Optional<Event> findByCode(String code);
    List<Event> findCurrentEvents(LocalDateTime now);
    List<Event> findUpcomingEvents(LocalDateTime now);
    List<Event> findPastEvents(LocalDateTime now);
    boolean isEventActive(Long eventId);

    List<Event> findActiveEvents();
}
