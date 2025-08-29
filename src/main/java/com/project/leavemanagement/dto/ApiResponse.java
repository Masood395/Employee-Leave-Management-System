package com.project.leavemanagement.dto;

import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonFormat;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@Getter
@NoArgsConstructor
public class ApiResponse<T> {

	@JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
	private LocalDateTime timestamp;
	private boolean success;
	private T data;
	
	public ApiResponse(boolean success, T data) {
		this.timestamp = LocalDateTime.now();
		this.success = success;
		this.data = data;
	}
	
	
}
