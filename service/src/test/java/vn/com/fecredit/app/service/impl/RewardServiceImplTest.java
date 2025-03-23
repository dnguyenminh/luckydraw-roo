package vn.com.fecredit.app.service.impl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import vn.com.fecredit.app.entity.CommonStatus;
import vn.com.fecredit.app.entity.Reward;
import vn.com.fecredit.app.repository.RewardRepository;
import vn.com.fecredit.app.service.exception.EntityNotFoundException;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RewardServiceImplTest {

    @Mock
    private RewardRepository rewardRepository;

    @InjectMocks
    private RewardServiceImpl rewardService;

    private Reward availableReward;
    private Reward depleteReward;
    private Reward inactiveReward;

    @BeforeEach
    void setUp() {
        availableReward = Reward.builder()
                .id(1L)
                .name("Available Reward")
                .code("AVAILABLE001")
                .quantity(10)
                .value(BigDecimal.valueOf(100))
                .status(CommonStatus.ACTIVE)
                .build();

        depleteReward = Reward.builder()
                .id(2L)
                .name("Depleted Reward")
                .code("DEPLETED001")
                .quantity(0)
                .value(BigDecimal.valueOf(100))
                .status(CommonStatus.ACTIVE)
                .build();

        inactiveReward = Reward.builder()
                .id(3L)
                .name("Inactive Reward")
                .code("INACTIVE001")
                .quantity(10)
                .value(BigDecimal.valueOf(100))
                .status(CommonStatus.INACTIVE)
                .build();
    }

    @Test
    void findByCode_ShouldReturnReward_WhenRewardExists() {
        // Given
        when(rewardRepository.findByCode("AVAILABLE001")).thenReturn(Optional.of(availableReward));

        // When
        Optional<Reward> result = rewardService.findByCode("AVAILABLE001");

        // Then
        assertTrue(result.isPresent());
        assertEquals("Available Reward", result.get().getName());
        verify(rewardRepository).findByCode("AVAILABLE001");
    }

    @Test
    void findByEventLocationId_ShouldReturnRewards() {
        // Given
        Long locationId = 1L;
        when(rewardRepository.findByEventLocationId(locationId))
                .thenReturn(Arrays.asList(availableReward, depleteReward));

        // When
        List<Reward> result = rewardService.findByEventLocationId(locationId);

        // Then
        assertEquals(2, result.size());
        verify(rewardRepository).findByEventLocationId(locationId);
    }

    @Test
    void findAvailableRewardsByLocation_ShouldReturnAvailableRewards() {
        // Given
        Long locationId = 1L;
        when(rewardRepository.findAvailableRewardsByLocation(locationId))
                .thenReturn(Arrays.asList(availableReward));

        // When
        List<Reward> result = rewardService.findAvailableRewardsByLocation(locationId);

        // Then
        assertEquals(1, result.size());
        assertEquals("Available Reward", result.get(0).getName());
        verify(rewardRepository).findAvailableRewardsByLocation(locationId);
    }

    @Test
    void isRewardAvailable_ShouldReturnTrue_WhenRewardIsActive() {
        // Given
        when(rewardRepository.findById(1L)).thenReturn(Optional.of(availableReward));

        // When
        boolean result = rewardService.isRewardAvailable(1L);

        // Then
        assertTrue(result);
        verify(rewardRepository).findById(1L);
    }

    @Test
    void isRewardAvailable_ShouldReturnFalse_WhenRewardIsDepleted() {
        // Given
        when(rewardRepository.findById(2L)).thenReturn(Optional.of(depleteReward));

        // When
        boolean result = rewardService.isRewardAvailable(2L);

        // Then
        assertFalse(result);
        verify(rewardRepository).findById(2L);
    }

    @Test
    void isRewardAvailable_ShouldReturnFalse_WhenRewardIsInactive() {
        // Given
        when(rewardRepository.findById(3L)).thenReturn(Optional.of(inactiveReward));

        // When
        boolean result = rewardService.isRewardAvailable(3L);

        // Then
        assertFalse(result);
        verify(rewardRepository).findById(3L);
    }

    @Test
    void hasAvailableQuantity_ShouldReturnTrue_WhenQuantityIsPositive() {
        // Given
        when(rewardRepository.findById(1L)).thenReturn(Optional.of(availableReward));

        // When
        boolean result = rewardService.hasAvailableQuantity(1L);

        // Then
        assertTrue(result);
        verify(rewardRepository).findById(1L);
    }

    @Test
    void hasAvailableQuantity_ShouldReturnFalse_WhenQuantityIsZero() {
        // Given
        when(rewardRepository.findById(2L)).thenReturn(Optional.of(depleteReward));

        // When
        boolean result = rewardService.hasAvailableQuantity(2L);

        // Then
        assertFalse(result);
        verify(rewardRepository).findById(2L);
    }

    @Test
    void decreaseQuantity_ShouldDecreaseQuantity_WhenEnoughQuantity() {
        // Given
        when(rewardRepository.findById(1L)).thenReturn(Optional.of(availableReward));
        when(rewardRepository.save(any(Reward.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // When
        Reward result = rewardService.decreaseQuantity(1L, 5);

        // Then
        assertEquals(5, result.getQuantity());
        verify(rewardRepository).findById(1L);
        verify(rewardRepository).save(availableReward);
    }

    @Test
    void decreaseQuantity_ShouldThrowException_WhenNotEnoughQuantity() {
        // Given
        when(rewardRepository.findById(2L)).thenReturn(Optional.of(depleteReward));

        // When & Then
        assertThrows(IllegalStateException.class, () -> rewardService.decreaseQuantity(2L, 1));
        verify(rewardRepository).findById(2L);
        verify(rewardRepository, never()).save(any(Reward.class));
    }

    @Test
    void decreaseQuantity_ShouldThrowException_WhenRewardNotFound() {
        // Given
        when(rewardRepository.findById(99L)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(EntityNotFoundException.class, () -> rewardService.decreaseQuantity(99L, 1));
        verify(rewardRepository).findById(99L);
        verify(rewardRepository, never()).save(any(Reward.class));
    }

    @Test
    void decreaseQuantity_ShouldThrowException_WhenAmountNotPositive() {
        // When & Then
        assertThrows(IllegalArgumentException.class, () -> rewardService.decreaseQuantity(1L, 0));
        assertThrows(IllegalArgumentException.class, () -> rewardService.decreaseQuantity(1L, -1));
        verify(rewardRepository, never()).findById(anyLong());
        verify(rewardRepository, never()).save(any(Reward.class));
    }

    @Test
    void getMaximumRewardValueAtLocation_ShouldReturnMaxValue() {
        // Given
        Long locationId = 1L;
        BigDecimal maxValue = BigDecimal.valueOf(100);
        when(rewardRepository.getMaximumRewardValueAtLocation(locationId)).thenReturn(maxValue);

        // When
        BigDecimal result = rewardService.getMaximumRewardValueAtLocation(locationId);

        // Then
        assertEquals(maxValue, result);
        verify(rewardRepository).getMaximumRewardValueAtLocation(locationId);
    }
}
