package com.project.leavemanagement.controller;

import java.time.LocalDate;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.project.leavemanagement.dto.Action;
import com.project.leavemanagement.dto.ApiResponse;
import com.project.leavemanagement.dto.EmployeeLeaveRequest;
import com.project.leavemanagement.entity.User;
import com.project.leavemanagement.enums.Status;
import com.project.leavemanagement.repository.UserRepo;
import com.project.leavemanagement.service.LeaveService;

import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/leave")
public class LeaveController {
	
	@Autowired
	private LeaveService ls;
	@Autowired
	private UserRepo ur;
	
    private static final Logger log = LoggerFactory.getLogger(LeaveController.class);
    
    private User getLoggedInUser() {
		String email = SecurityContextHolder.getContext().getAuthentication().getName();
	    return ur.findByEmail(email)
	                 .orElseThrow(() -> new RuntimeException("User not found"));
	}

	@Operation(summary = "Apply Leave Requests", description = "Employee can apply leave requests with proper reason. ")
	@PostMapping
	public ResponseEntity<?> applyLeave(@Valid @RequestBody EmployeeLeaveRequest leave) {
        log.info("User {} applying for leave from {} to {}", getLoggedInUser().getUserName(),leave.getStart_date(), leave.getEnd_date());
		return new ResponseEntity<>(new ApiResponse<>(true, ls.applyLeave(leave)),HttpStatus.CREATED);
	}
	@Operation(summary = "Get All Leave Requests", description = "Fetch all particular employee leave requests. ")
	@GetMapping
	public ResponseEntity<?> getMyLeaves(
			@RequestParam(defaultValue = "0",required = false) int pageNo
			,@RequestParam(defaultValue = "10",required = false) int pageSize
			,@RequestParam(defaultValue = "appliedAt",required = false) String sortBy
			,@RequestParam(defaultValue = "ASC",required = false) String sortDir
			) {
		return ResponseEntity.ok(new ApiResponse<>(true, ls.getAplliedLeave(pageNo,pageSize,sortBy,sortDir)));
	}
	@Operation(summary = "Get Leave Requests of Employee", description = "Fetch all employee leave requests working under particular manager. ")
	@GetMapping("/{status}")
	public ResponseEntity<?> getPendingLeaves(@PathVariable String status,
			@RequestParam(defaultValue = "0",required = false) int pageNo
			,@RequestParam(defaultValue = "10",required = false) int pageSize
			,@RequestParam(defaultValue = "appliedAt",required = false) String sortBy
			,@RequestParam(defaultValue = "ASC",required = false) String sortDir
			) {
		return ResponseEntity.ok(new ApiResponse<>(true, ls.getPendings(status,pageNo,pageSize,sortBy,sortDir)));
	}
	@Operation(summary = "Accept/Reject Leave Request", description = "manager can accept/reject the employees leaves working under him. ")
	@PutMapping("/action/{id}")
	public ResponseEntity<?>  managerAction(@PathVariable int id,@RequestBody Action action) {
        log.info("Manager {} {} leave id={}", getLoggedInUser().getUserName(), action, id);
		return new ResponseEntity<>(new ApiResponse<>(true, ls.managerAction(id,action)),HttpStatus.ACCEPTED);
	}
	@Operation(summary = "Search All Leave Requests", description = "Fetch and search all leave requests.")
	@GetMapping("/search")
	public ResponseEntity<?> searchLeaves(
			@RequestParam(required = false) Integer id,
			@RequestParam(required = false) Status status,
			@RequestParam(required = false) LocalDate startDate,
			@RequestParam(required = false) LocalDate endDate,
			@RequestParam(required = false) String reason,
			@RequestParam(defaultValue = "0",required = false) int pageNo
			,@RequestParam(defaultValue = "10",required = false) int pageSize
			,@RequestParam(defaultValue = "appliedAt",required = false) String sortBy
			,@RequestParam(defaultValue = "ASC",required = false) String sortDir
			) {
		return ResponseEntity.ok(new ApiResponse<>(true, ls.searchAplliedLeave(id,status,startDate,endDate,reason,pageNo,pageSize,sortBy,sortDir)));
	}
	
}
