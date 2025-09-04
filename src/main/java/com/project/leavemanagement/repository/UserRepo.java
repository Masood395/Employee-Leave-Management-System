package com.project.leavemanagement.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.project.leavemanagement.entity.User;
import com.project.leavemanagement.enums.Role;


public interface UserRepo extends JpaRepository<User, Integer> {

	Optional<User> findByEmail(String email);
	
	@Query("""
			SELECT u FROM User u
			WHERE (:userName IS NULL OR LOWER(u.userName) LIKE LOWER(CONCAT('%', :userName, '%')))
			AND   (:email IS NULL OR LOWER(u.email) LIKE LOWER(CONCAT('%', :email, '%')))
			AND   (:role IS NULL OR u.role = :role)
			""")
	Page<User> searchUsers(@Param("userName") String userName, @Param("email") String email, @Param("role") Role role,
			Pageable pageable);

    List<User> findByManager(User manager);
}
