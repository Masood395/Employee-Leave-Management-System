package com.project.leavemanagement.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.project.leavemanagement.entity.AuthToken;

public interface AuthTokenRepo extends JpaRepository<AuthToken, Integer> {

	Optional<AuthToken> findByAccessToken(String accessToken);
    Optional<AuthToken> findByRefreshToken(String refreshToken);
}
