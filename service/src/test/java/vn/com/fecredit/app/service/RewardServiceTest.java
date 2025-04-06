package vn.com.fecredit.app.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import vn.com.fecredit.app.entity.EventLocation;
import vn.com.fecredit.app.entity.Reward;
import vn.com.fecredit.app.entity.enums.CommonStatus;
import vn.com.fecredit.app.repository.RewardRepository;
import vn.com.fecredit.app.service.impl.RewardServiceImpl;

@ExtendWith(MockitoExtension.class)
public class RewardServiceTest {

    @Mock
    private RewardRepository rewardRepository;

    @Mock
    private EventLocationService eventLocationService;

    @InjectMocks
    private RewardServiceImpl rewardService; // Use the implementation class instead of the interface

    private Reward reward1;
    private Reward reward2;
    private EventLocation eventLocation;

    @BeforeEach
    void setUp() {
        // Setup test data
        eventLocation = EventLocation.builder()
                .id(1L)
                .name("Test Location")
                .code("TEST_LOC")
                .status(CommonStatus.ACTIVE)
                .build();

        reward1 = Reward.builder()
                .id(1L)
                .name("Test Reward 1")
                .code("TEST_REWARD_1")
                .eventLocation(eventLocation)
                .status(CommonStatus.ACTIVE)
                .build();

        reward2 = Reward.builder()
                .id(2L)
                .name("Test Reward 2")
                .code("TEST_REWARD_2")
                .eventLocation(eventLocation)
                .status(CommonStatus.INACTIVE)
                .build();
    }

    @Test
    void findById_ShouldReturnReward_WhenExists() {
        // Arrange
        when(rewardRepository.findById(1L)).thenReturn(Optional.of(reward1));

        // Act
        Optional<Reward> result = rewardService.findById(1L);

        // Assert
        assertThat(result).isPresent();
        assertThat(result.get().getId()).isEqualTo(1L);
        assertThat(result.get().getName()).isEqualTo("Test Reward 1");
    }

    @Test
    void findByStatus_ShouldReturnFilteredRewards() {
        // Arrange
        when(rewardRepository.findByStatus(CommonStatus.ACTIVE)).thenReturn(List.of(reward1));
        when(rewardRepository.findByStatus(CommonStatus.INACTIVE)).thenReturn(List.of(reward2));

        // Act
        List<Reward> activeRewards = rewardService.findByStatus(CommonStatus.ACTIVE);
        List<Reward> inactiveRewards = rewardService.findByStatus(CommonStatus.INACTIVE);

        // Assert
        assertThat(activeRewards).hasSize(1);
        assertThat(activeRewards.get(0).getCode()).isEqualTo("TEST_REWARD_1");
        
        assertThat(inactiveRewards).hasSize(1);
        assertThat(inactiveRewards.get(0).getCode()).isEqualTo("TEST_REWARD_2");
    }

    @Test
    void findByEventLocationId_ShouldReturnLocationsRewards() {
        // Arrange
        when(rewardRepository.findByEventLocationId(1L)).thenReturn(Arrays.asList(reward1, reward2));

        // Act
        List<Reward> result = rewardService.findByEventLocationId(1L);

        // Assert
        assertThat(result).hasSize(2);
        verify(rewardRepository, times(1)).findByEventLocationId(1L);
    }

    @Test
    void create_ShouldSaveReward_WithLocationAssociation() {
        // Arrange
        when(eventLocationService.findById(anyLong())).thenReturn(Optional.of(eventLocation));
        when(rewardRepository.save(any(Reward.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Reward newReward = Reward.builder()
                .name("New Reward")
                .code("NEW_REWARD")
                .status(CommonStatus.ACTIVE)
                .build();

        // Act
        Reward result = rewardService.create(newReward, 1L);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getName()).isEqualTo("New Reward");
        assertThat(result.getEventLocation()).isEqualTo(eventLocation);
        
        verify(rewardRepository, times(1)).save(any(Reward.class));
        verify(eventLocationService, times(1)).findById(1L);
    }

    // Add other tests as needed...
}