package com.project.leavemanagement.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.project.leavemanagement.dto.ApiResponse;
import com.project.leavemanagement.dto.AuthRequest;
import com.project.leavemanagement.dto.AuthResponse;
import com.project.leavemanagement.service.AuthService;

import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

	@Autowired
	private AuthService authService;

    private static final Logger log = LoggerFactory.getLogger(AuthController.class);

	@Operation(summary = "User Login", description = "user have to give correct credentials. ")
    @PostMapping("/login")
    public ResponseEntity<?> authenticateUser(@Valid @RequestBody AuthRequest loginRequest) {
        log.info("Login attempt for user {}", loginRequest.getEmail());
        AuthResponse response = authService.login(loginRequest);
        log.info("User {} logged in successfully", loginRequest.getEmail());
        return ResponseEntity.ok(new ApiResponse<>(true,response));
    }
	@Operation(summary = "Re-Generate Access Token", description = "using valid request token user can generate access token. ")
    @PostMapping("/refresh")
    public ResponseEntity<?> getAccessToken(@RequestParam String refreshToken) {
        log.info("Request for re-generating Access Token");
    	return ResponseEntity.ok(new ApiResponse<>(true,authService.getAccessToken(refreshToken)));
    }
	@Operation(summary = "Logout User", description = "logout. ")
    @PostMapping("/logout")
    public ResponseEntity<?> logOutUser() {
        log.info("Logout requested");
    	authService.loggedOut();
        log.info(" User Loged-out successfully.");
    	return ResponseEntity.ok(new ApiResponse<>(true,"✅ Logged-Out"));
    }
}
