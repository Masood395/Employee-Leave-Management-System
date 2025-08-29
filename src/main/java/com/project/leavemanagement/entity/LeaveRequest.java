package com.project.leavemanagement.entity;

import java.time.LocalDate;
import java.time.LocalDateTime;

import com.project.leavemanagement.enums.Status;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "leave_requests")
public class LeaveRequest {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private int id;
	@ManyToOne(optional = false)
	@JoinColumn(name = "user_id")
	private User user;
//	@Column(unique = true)
	private LocalDate start_date;
	private LocalDate end_date;
	private String reason;
	@Enumerated(EnumType.STRING)
	private Status status=Status.PENDING;
	private LocalDateTime appliedAt;
	private LocalDateTime updatedAt;
	
}
