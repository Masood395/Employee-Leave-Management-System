package com.project.leavemanagement.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;

import com.project.leavemanagement.entity.User;
import com.project.leavemanagement.enums.Status;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@Getter
@NoArgsConstructor
//@AllArgsConstructor
public class AdminLeaveResponse {

	private int userId;
	private LocalDate start_date;
	private LocalDate end_date;
	private String reason;
	private Status status;
	private LocalDateTime appliedAt;
	private LocalDateTime updatedAt;
	
	public AdminLeaveResponse(User userId, LocalDate start_date, LocalDate end_date, String reason, Status status,
			LocalDateTime appliedAt, LocalDateTime updatedAt) {
		super();
		this.userId = userId.getUserId();
		this.start_date = start_date;
		this.end_date = end_date;
		this.reason = reason;
		this.status = status;
		this.appliedAt = appliedAt;
		this.updatedAt = updatedAt;
	}
	
	
}
