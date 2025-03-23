package vn.com.fecredit.app.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import vn.com.fecredit.app.entity.Role;
import vn.com.fecredit.app.entity.CommonStatus;
import vn.com.fecredit.app.entity.enums.RoleName;

import java.util.List;
import java.util.Optional;

@Repository
public interface RoleRepository extends JpaRepository<Role, Long> {
    Optional<Role> findByRoleName(RoleName roleName);
    boolean existsByRoleName(RoleName roleName);
    List<Role> findByStatus(CommonStatus status);
    List<Role> findByStatusOrderByDisplayOrderAsc(CommonStatus status);
    List<Role> findByDisplayOrderGreaterThan(Integer minOrder);
    List<Role> findByUsersEmail(String email);
}