package vn.com.fecredit.app.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;

import vn.com.fecredit.app.entity.EventLocation;
import vn.com.fecredit.app.entity.Participant;
import vn.com.fecredit.app.entity.ParticipantEvent;
import vn.com.fecredit.app.entity.Region;
import vn.com.fecredit.app.entity.Reward;
import vn.com.fecredit.app.entity.SpinHistory;
import vn.com.fecredit.app.entity.enums.CommonStatus;
import vn.com.fecredit.app.repository.RewardRepository;
import vn.com.fecredit.app.repository.SpinHistoryRepository;

import java.util.HashSet;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class SpinServiceTest {

    @Mock
    private RewardRepository rewardRepository;
    
    @Mock
    private SpinHistoryRepository spinHistoryRepository;
    
    @InjectMocks
    @Spy
    private SpinService spinService;
    
    private Reward reward;
    private EventLocation eventLocation;
    private Region region;
    private Participant participant;
    private ParticipantEvent participantEvent;
    
    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        
        region = Region.builder()
                .id(1L)
                .name("Test Region")
                .code("REG1")
                .status(CommonStatus.ACTIVE)
                .eventLocations(new HashSet<>())
                .build();
        
        eventLocation = EventLocation.builder()
                .id(1L)
                .name("Test Location")
                .code("LOC1")
                .quantity(10)
                .winProbability(0.5)
                .maxSpin(100)
                .region(region)
                .status(CommonStatus.ACTIVE)
                .rewards(new HashSet<>())
                .participantEvents(new HashSet<>())
                .build();
        
        region.getEventLocations().add(eventLocation);
        
        reward = Reward.builder()
                .id(1L)
                .name("Test Reward")
                .code("RWD1")
                .eventLocation(eventLocation)
                .status(CommonStatus.ACTIVE)
                .spinHistories(new HashSet<>())
                .build();
        
        eventLocation.getRewards().add(reward);
        
        participant = Participant.builder()
                .id(1L)
                .name("Test Participant")
                .code("PART1")
                .status(CommonStatus.ACTIVE)
                .build();
        
        participantEvent = ParticipantEvent.builder()
                .id(1L)
                .participant(participant)
                .eventLocation(eventLocation)
                .spinsRemaining(5)
                .status(CommonStatus.ACTIVE)
                .build();
        
        eventLocation.getParticipantEvents().add(participantEvent);
    }
    
    @Test
    void testProcessSpin() {
        // Setup
        eventLocation.setQuantity(5);
        when(rewardRepository.findById(1L)).thenReturn(Optional.of(reward));
        when(spinHistoryRepository.save(any(SpinHistory.class))).thenAnswer(invocation -> {
            SpinHistory history = invocation.getArgument(0);
            history.setId(1L);
            return history;
        });
        
        // Mock the random factor to ensure win
        doReturn(true).when(spinService).calculateWinChance(1L);
        
        SpinHistory spinHistory = spinService.processSpin(1L, participantEvent);
        
        assertNotNull(spinHistory);
        assertEquals(1L, spinHistory.getId());
        assertEquals(reward, spinHistory.getReward());
        assertEquals(participantEvent, spinHistory.getParticipantEvent());
        assertTrue(spinHistory.isWin());
        
        verify(rewardRepository).findById(1L);
        verify(spinHistoryRepository).save(any(SpinHistory.class));
    }
    
    @Test
    void testProcessSpinWithZeroQuantity() {
        // Setup: zero quantity in location means no rewards available
        eventLocation.setQuantity(0);
        when(rewardRepository.findById(1L)).thenReturn(Optional.of(reward));
        when(spinHistoryRepository.save(any(SpinHistory.class))).thenAnswer(invocation -> {
            SpinHistory history = invocation.getArgument(0);
            history.setId(1L);
            return history;
        });
        
        // Even if calculation would result in a win, zero quantity should override
        doReturn(true).when(spinService).calculateWinChance(1L);
        
        SpinHistory spinHistory = spinService.processSpin(1L, participantEvent);
        
        assertNotNull(spinHistory);
        assertFalse(spinHistory.isWin());
        
        verify(rewardRepository).findById(1L);
        verify(spinHistoryRepository).save(any(SpinHistory.class));
    }
    
    @Test
    void testCalculateWinChance() {
        eventLocation.setWinProbability(0.3);
        when(rewardRepository.findById(1L)).thenReturn(Optional.of(reward));
        
        // Mock the random generation to test both win and lose scenarios
        doReturn(0.2).when(spinService).generateRandomProbability();
        boolean didWin = spinService.calculateWinChance(1L);
        assertTrue(didWin);
        
        doReturn(0.4).when(spinService).generateRandomProbability();
        boolean didLose = spinService.calculateWinChance(1L);
        assertFalse(didLose);
        
        verify(rewardRepository, times(2)).findById(1L);
    }
}
