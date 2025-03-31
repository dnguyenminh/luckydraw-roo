package vn.com.fecredit.app.service;

import org.springframework.stereotype.Service;
import vn.com.fecredit.app.dto.RegisterRequest;
import vn.com.fecredit.app.entity.User;

@Service
public interface UserService {
    
    boolean existsByUsername(String username);
    
    boolean existsByEmail(String email);
    
    User registerUser(RegisterRequest registerRequest);
}
