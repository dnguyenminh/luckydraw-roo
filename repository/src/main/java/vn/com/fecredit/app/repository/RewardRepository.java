package vn.com.fecredit.app.repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import jakarta.validation.constraints.NotNull;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import vn.com.fecredit.app.entity.Reward;
import vn.com.fecredit.app.entity.enums.CommonStatus;

@Repository
public interface RewardRepository extends SimpleObjectRepository<Reward> {
    Optional<Reward> findByCode(String code);

    boolean existsByCode(String code);

    List<Reward> findByStatus(CommonStatus status);

    Page<Reward> findByStatus(@NotNull CommonStatus status, Pageable pageable);
}
