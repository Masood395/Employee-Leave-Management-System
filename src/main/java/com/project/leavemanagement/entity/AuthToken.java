package com.project.leavemanagement.entity;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AuthToken {

	@Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @Column(nullable = false, unique = true, length = 512)
    private String accessToken;

    private LocalDateTime accessTokenExpiry;

    private boolean accessTokenRevoked = false;

    @Column(nullable = false, unique = true, length = 512)
    private String refreshToken;

    private LocalDateTime refreshTokenExpiry;

    private boolean refreshTokenRevoked = false;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
}
