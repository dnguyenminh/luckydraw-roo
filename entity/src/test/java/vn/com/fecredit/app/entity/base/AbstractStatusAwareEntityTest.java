package vn.com.fecredit.app.entity.base;

import org.junit.jupiter.api.Test;
import vn.com.fecredit.app.entity.Event;
import vn.com.fecredit.app.entity.Role;
import vn.com.fecredit.app.entity.User;
import vn.com.fecredit.app.entity.Participant;

import static org.junit.jupiter.api.Assertions.*;
import static vn.com.fecredit.app.entity.CommonStatus.ACTIVE;
import static vn.com.fecredit.app.entity.CommonStatus.INACTIVE;

class AbstractStatusAwareEntityTest extends BaseEntityTest {

    @Test
    void status_ShouldBeHandledByAllEntities() {
        // Test Event
        Event event = new Event();
        event.setStatus(ACTIVE);
        assertEquals(ACTIVE, event.getStatus());

        // Test Role
        Role role = new Role();
        role.setStatus(ACTIVE);
        assertEquals(ACTIVE, role.getStatus());

        // Test User
        User user = new User();
        user.setStatus(ACTIVE);
        assertEquals(ACTIVE, user.getStatus());

        // Test Participant
        Participant participant = new Participant();
        participant.setStatus(ACTIVE);
        assertEquals(ACTIVE, participant.getStatus());
    }

    @Test
    void status_ShouldHandleActiveState() {
        Event event = new Event();
        event.setStatus(ACTIVE);
        assertEquals(ACTIVE, event.getStatus());
    }

    @Test
    void status_ShouldHandleInactiveState() {
        Event event = new Event();
        event.setStatus(INACTIVE);
        assertEquals(INACTIVE, event.getStatus());
    }

    @Test
    void status_ShouldBeIndependentBetweenEntities() {
        Event event1 = new Event();
        Event event2 = new Event();

        event1.setStatus(ACTIVE);
        event2.setStatus(INACTIVE);

        assertEquals(ACTIVE, event1.getStatus());
        assertEquals(INACTIVE, event2.getStatus());
    }
}
