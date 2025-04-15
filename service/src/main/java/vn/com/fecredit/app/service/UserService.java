package vn.com.fecredit.app.service;

import org.springframework.stereotype.Service;

@Service
public interface UserService {

    boolean existsByUsername(String username);

    boolean existsByEmail(String email);

}
