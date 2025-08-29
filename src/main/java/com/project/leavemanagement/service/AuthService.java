package com.project.leavemanagement.service;

import com.project.leavemanagement.dto.AuthRequest;
import com.project.leavemanagement.dto.AuthResponse;

public interface AuthService {
    AuthResponse login(AuthRequest loginDTO);
    AuthResponse getAccessToken(String refreshToken);
    void loggedOut();
}
