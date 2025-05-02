package vn.com.fecredit.app.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import jakarta.validation.constraints.NotNull;
import vn.com.fecredit.app.entity.Reward;
import vn.com.fecredit.app.entity.enums.CommonStatus;

@Repository
public interface RewardRepository extends SimpleObjectRepository<Reward, Long> {
    Optional<Reward> findByCode(String code);

    boolean existsByCode(String code);

    List<Reward> findByStatus(CommonStatus status);

    Page<Reward> findByStatus(@NotNull CommonStatus status, Pageable pageable);
}
