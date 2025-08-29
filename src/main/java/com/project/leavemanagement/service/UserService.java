package com.project.leavemanagement.service;

import org.springframework.data.domain.Page;

import com.project.leavemanagement.dto.PagedResponse;
import com.project.leavemanagement.dto.UserRequest;
import com.project.leavemanagement.dto.UserResponse;
import com.project.leavemanagement.enums.Role;

public interface UserService {

	UserResponse createUser(UserRequest user);
	UserResponse updateUser(int id ,UserRequest user);
	PagedResponse<UserResponse> getAllUsers(int pageNo, int pageSize, String field, String dir);
	UserResponse getUserById(int id);
	void deleteUser(int id);
	PagedResponse<?> searchUsers(String username, String email, Role role, int pageNo, int pageSize, String sortBy,
			String sortDir);
}
