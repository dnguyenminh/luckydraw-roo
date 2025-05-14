package vn.com.fecredit.app.repository;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;

import lombok.extern.slf4j.Slf4j;
import vn.com.fecredit.app.entity.Participant;

/**
 * Simple test for ParticipantRepository functionality
 * This test uses JPA with Spring Boot test support
 */
@Slf4j
@DataJpaTest
@ActiveProfiles("test")
@Sql(scripts = {"classpath:schema-h2.sql", "classpath:data-h2.sql"})
public class SimpleParticipantRepositoryTest {

    @Autowired
    private ParticipantRepository participantRepository;
    
    @Test
    void testFindByProvinceId() {
        // Find all participants with province ID = 1 (Hanoi)
        List<Participant> participants = participantRepository.findByProvinceId(1L);
        
        // Verify results
        assertThat(participants).isNotEmpty();
        assertThat(participants).hasSize(1);
        log.info("Found {} participants with province_id = 1", participants.size());
        
        // Verify first participant's properties
        Participant participant = participants.get(0);
        assertThat(participant.getName()).isEqualTo("John Doe");
        assertThat(participant.getCode()).isEqualTo("JOHN001");
    }
    
    @Test
    void testFindByCode() {
        // Find participant by code "JOHN001" (which exists in the test data)
        Optional<Participant> participantOpt = participantRepository.findByCode("JOHN001");
        
        // Verify results
        assertThat(participantOpt).isPresent();
        log.info("Found participant with code 'JOHN001'");
        
        // Verify participant's properties
        Participant participant = participantOpt.get();
        assertThat(participant.getName()).isEqualTo("John Doe");
        assertThat(participant.getPhone()).isEqualTo("1234567890");
    }
}
