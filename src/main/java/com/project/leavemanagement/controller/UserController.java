package com.project.leavemanagement.controller;

import java.time.LocalDate;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.project.leavemanagement.dto.ApiResponse;
import com.project.leavemanagement.dto.UserRequest;
import com.project.leavemanagement.enums.Role;
import com.project.leavemanagement.enums.Status;
import com.project.leavemanagement.service.LeaveService;
import com.project.leavemanagement.service.UserService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/admin/users")
public class UserController {
	
	@Autowired
	private UserService us;
	@Autowired
	private LeaveService ls;
	
    private static final Logger log = LoggerFactory.getLogger(UserController.class);


	@PostMapping
	public ResponseEntity<?> createUser(@Valid @RequestBody UserRequest dto) {
		log.debug("Admin Requested for creating User :{}",dto);
		log.info("Admin Requested to create user : {} with Role : {}",dto.getUserName(),dto.getRole());
		return new ResponseEntity<>(new ApiResponse<>(true, us.createUser(dto)),HttpStatus.CREATED);
	}
	@PutMapping("/{id}")
	public ResponseEntity<?> updateUser(@PathVariable int id ,@Valid @RequestBody UserRequest dto) {
		log.info("Admin Requesedt for Update user : {}",dto.getUserName());
		return new ResponseEntity<>(new ApiResponse<>(true,us.updateUser(id,dto)),HttpStatus.ACCEPTED);
	}
	@GetMapping
	public ResponseEntity<?> getUsers(
			@RequestParam(defaultValue = "0",required = false) int pageNo
			,@RequestParam(defaultValue = "10",required = false) int pageSize
			,@RequestParam(defaultValue = "userName",required = false) String sortBy
			,@RequestParam(defaultValue = "ASC",required = false) String sortDir
			) {
		return ResponseEntity.ok(new ApiResponse<>(true, us.getAllUsers(pageNo,pageSize,sortBy,sortDir)));
	}
	@GetMapping("/{id}")
	public ResponseEntity<?> getById(@PathVariable int id) {
		return ResponseEntity.ok(new ApiResponse<>(true,us.getUserById(id)));
	}
	@DeleteMapping("/{id}")
	public ResponseEntity<?> deleteById(@PathVariable int id) {
		log.info("Admin Requested for Deleting User with ID : {}",id);
		 us.deleteUser(id);
		 log.info("User with ID : {} Deleted Successfully");
		 return ResponseEntity.ok(new ApiResponse<>(true, "User Deleted Successfully!!"));
	}
	@GetMapping("/leave")
	public ResponseEntity<?> getUsersLeaves(
			@RequestParam(defaultValue = "0",required = false) int pageNo
			,@RequestParam(defaultValue = "10",required = false) int pageSize
			,@RequestParam(defaultValue = "appliedAt",required = false) String sortBy
			,@RequestParam(defaultValue = "ASC",required = false) String sortDir
			) {
		return ResponseEntity.ok(new ApiResponse<>(true, ls.getAllLeaves(pageNo,pageSize,sortBy,sortDir)));
	}
	@GetMapping("/leave/search")
	public ResponseEntity<?> searchLeaves(
			@RequestParam(required = false) Integer uid,
			@RequestParam(required = false) Status status,
			@RequestParam(required = false) LocalDate startDate,
			@RequestParam(required = false) LocalDate endDate,
			@RequestParam(required = false) String reason,
			@RequestParam(defaultValue = "0",required = false) int pageNo
			,@RequestParam(defaultValue = "10",required = false) int pageSize
			,@RequestParam(defaultValue = "appliedAt",required = false) String sortBy
			,@RequestParam(defaultValue = "ASC",required = false) String sortDir
			) {
		return ResponseEntity.ok(new ApiResponse<>(true, ls.searchAplliedLeave(uid,status,startDate,endDate,reason,pageNo,pageSize,sortBy,sortDir)));
	}
	@GetMapping("/search")
    public ResponseEntity<?> searchUsers(
            @RequestParam(required = false) String username,
            @RequestParam(required = false) String email,
            @RequestParam(required = false) Role role,
            @RequestParam(defaultValue = "0",required = false) int pageNo
			,@RequestParam(defaultValue = "10",required = false) int pageSize
			,@RequestParam(defaultValue = "userName",required = false) String sortBy
			,@RequestParam(defaultValue = "ASC",required = false) String sortDir
    ) {
		return ResponseEntity.ok(new ApiResponse<>(true, us.searchUsers(username, email, role, pageNo,pageSize,sortBy,sortDir)));

    }
}
