package com.project.leavemanagement.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;

import com.project.leavemanagement.entity.User;
import com.project.leavemanagement.enums.Status;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class EmployeeLeaveResponse {

	private LocalDate start_date;
	private LocalDate end_date;
	private String reason;
	private Status status;
	private LocalDateTime appliedAt;
	private LocalDateTime updatedAt;
}
