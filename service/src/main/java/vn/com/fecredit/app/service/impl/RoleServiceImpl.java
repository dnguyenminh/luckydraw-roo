package vn.com.fecredit.app.service.impl;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.com.fecredit.app.entity.Role;
import vn.com.fecredit.app.entity.CommonStatus;
import vn.com.fecredit.app.entity.enums.RoleName;
import vn.com.fecredit.app.repository.RoleRepository;
import vn.com.fecredit.app.service.RoleService;
import vn.com.fecredit.app.service.base.AbstractServiceImpl;

import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@Transactional
public class RoleServiceImpl extends AbstractServiceImpl<Role> implements RoleService {

    private final RoleRepository roleRepository;

    public RoleServiceImpl(RoleRepository roleRepository) {
        super(roleRepository);
        this.roleRepository = roleRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public List<Role> findByStatus(CommonStatus status) {
        return roleRepository.findByStatus(status);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Role> findByRoleName(RoleName name) {
        return roleRepository.findByRoleName(name);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Role> findByDisplayOrderGreaterThan(Integer minOrder) {
        return roleRepository.findByDisplayOrderGreaterThan(minOrder);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Role> findByUserEmail(String userEmail) {
        return roleRepository.findByUsersEmail(userEmail);
    }
}
