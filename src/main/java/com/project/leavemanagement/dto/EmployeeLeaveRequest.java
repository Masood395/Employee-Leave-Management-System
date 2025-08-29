package com.project.leavemanagement.dto;

import java.time.LocalDate;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class EmployeeLeaveRequest {

//	private int userId;
	@NotNull(message = "Leave start date cannot be null")
	@Future(message = "Leave start date must be in the future")
	private LocalDate start_date;
	@NotNull(message = "Leave end date cannot be null")
    @Future(message = "Leave end date must be in the future")
	private LocalDate end_date;
	@NotBlank(message = "Give a Proper Reason!!")
	@Size(min=10,message = "Reason must be min 5 character!!")
	private String reason;
}
