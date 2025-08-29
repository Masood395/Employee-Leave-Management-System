package com.project.leavemanagement.dto;

import java.time.LocalDateTime;

import com.project.leavemanagement.enums.Role;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class UserResponse {

	private int  id;
    private String username;
    private String email;
    private Role role;
    private int  managerId;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
