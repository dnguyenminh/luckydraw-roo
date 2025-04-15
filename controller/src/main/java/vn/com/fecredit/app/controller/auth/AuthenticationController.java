package vn.com.fecredit.app.controller.auth;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import vn.com.fecredit.app.controller.auth.dto.AuthRequest;
import vn.com.fecredit.app.controller.auth.dto.AuthResponse;
import vn.com.fecredit.app.service.UserService;
import vn.com.fecredit.app.security.JwtTokenProvider;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/auth")
public class AuthenticationController {

    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider jwtTokenProvider;
    private final UserService userService;

    @Autowired
    public AuthenticationController(AuthenticationManager authenticationManager,
                                   JwtTokenProvider jwtTokenProvider,
                                   UserService userService) {
        this.authenticationManager = authenticationManager;
        this.jwtTokenProvider = jwtTokenProvider;
        this.userService = userService;
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody AuthRequest authRequest) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        authRequest.getUsername(),
                        authRequest.getPassword()
                )
        );

        SecurityContextHolder.getContext().setAuthentication(authentication);
        String jwt = jwtTokenProvider.generateToken(authentication.getName());

        return ResponseEntity.ok(new AuthResponse(jwt));
    }

//    @PostMapping("/register")
//    public ResponseEntity<String> register(@Valid @RequestBody RegisterRequest registerRequest) {
//        vn.com.fecredit.app.dto.RegisterRequest serviceRegisterRequest = new vn.com.fecredit.app.dto.RegisterRequest();
//        serviceRegisterRequest.setUsername(registerRequest.getUsername());
//        serviceRegisterRequest.setEmail(registerRequest.getEmail());
//        serviceRegisterRequest.setPassword(registerRequest.getPassword());
//
//        if (userService.existsByUsername(serviceRegisterRequest.getUsername())) {
//            return ResponseEntity.badRequest().body("Username is already taken");
//        }
//
//        if (userService.existsByEmail(serviceRegisterRequest.getEmail())) {
//            return ResponseEntity.badRequest().body("Email is already in use");
//        }
//
//        userService.registerUser(serviceRegisterRequest);
//        return ResponseEntity.ok("User registered successfully");
//    }
}
