package vn.com.fecredit.app.repository;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.assertTrue;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ActiveProfiles("test")
class ParticipantEventRepositoryTest {

    @Test
    void testFindParticipantEventById() {
        // ...simulate repository usage...
        // repository.findById(...)
        // ...assert that the correct entity is returned...
        assertTrue(true);
    }
}