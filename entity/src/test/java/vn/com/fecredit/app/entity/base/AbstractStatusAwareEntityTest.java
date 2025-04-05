package vn.com.fecredit.app.entity.base;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

import vn.com.fecredit.app.entity.Event;
import vn.com.fecredit.app.entity.Participant;
import vn.com.fecredit.app.entity.Role;
import vn.com.fecredit.app.entity.User;
import vn.com.fecredit.app.entity.enums.CommonStatus;

class AbstractStatusAwareEntityTest extends BaseEntityTest {

    @Test
    void status_ShouldBeHandledByAllEntities() {
        // Test Event
        Event event = new Event();
        event.setStatus(CommonStatus.ACTIVE);
        assertEquals(CommonStatus.ACTIVE, event.getStatus());

        // Test Role
        Role role = new Role();
        role.setStatus(CommonStatus.ACTIVE);
        assertEquals(CommonStatus.ACTIVE, role.getStatus());

        // Test User
        User user = new User();
        user.setStatus(CommonStatus.ACTIVE);
        assertEquals(CommonStatus.ACTIVE, user.getStatus());

        // Test Participant
        Participant participant = new Participant();
        participant.setStatus(CommonStatus.ACTIVE);
        assertEquals(CommonStatus.ACTIVE, participant.getStatus());
    }

    @Test
    void status_ShouldHandleActiveState() {
        Event event = new Event();
        event.setStatus(CommonStatus.ACTIVE);
        assertEquals(CommonStatus.ACTIVE, event.getStatus());
    }

    @Test
    void status_ShouldHandleInactiveState() {
        Event event = new Event();
        event.setStatus(CommonStatus.INACTIVE);
        assertEquals(CommonStatus.INACTIVE, event.getStatus());
    }

    @Test
    void status_ShouldBeIndependentBetweenEntities() {
        Event event1 = new Event();
        Event event2 = new Event();

        event1.setStatus(CommonStatus.ACTIVE);
        event2.setStatus(CommonStatus.INACTIVE);

        assertEquals(CommonStatus.ACTIVE, event1.getStatus());
        assertEquals(CommonStatus.INACTIVE, event2.getStatus());
    }
}
