package vn.com.fecredit.app.service;

import org.springframework.stereotype.Service;

import vn.com.fecredit.app.entity.User;
import vn.com.fecredit.app.service.dto.RegisterRequest;

@Service
public interface UserService {
    
    boolean existsByUsername(String username);
    
    boolean existsByEmail(String email);
    
    User registerUser(RegisterRequest registerRequest);
}
