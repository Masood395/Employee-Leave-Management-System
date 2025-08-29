package com.project.leavemanagement.repository;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.project.leavemanagement.entity.LeaveRequest;
import com.project.leavemanagement.entity.User;
import com.project.leavemanagement.enums.Status;

public interface LeaveRequestRepo extends JpaRepository<LeaveRequest, Integer> {

	Page<LeaveRequest> findByUser(User user, Pageable pageable);

	@Query("from LeaveRequest where start_date=?1")
	List<LeaveRequest> findByStart_Date(LocalDate start_date);

	List<LeaveRequest> findByStatus(Status pending);

	Page<LeaveRequest> findByUser_Manager_UserIdAndStatus(int managerId,Status pending, Pageable pageable);
	
	@Query("""
	        SELECT l
	        FROM LeaveRequest l
	        WHERE (:status IS NULL OR l.status = :status)
	          AND (:startDate IS NULL OR l.start_date = :startDate)
	          AND (:endDate IS NULL OR l.end_date = :endDate)
	          AND (:reason IS NULL OR :reason = '' OR LOWER(l.reason) LIKE LOWER(CONCAT('%', :reason, '%')))
	          AND (:uid IS NULL OR l.user.id = :uid)
	    """)
	    Page<LeaveRequest> searchLeaveRequests(@Param("status") Status status,
	                                           @Param("startDate") LocalDate startDate,
	                                           @Param("endDate") LocalDate endDate,
	                                           @Param("reason") String reason,
	                                           @Param("uid") Integer userId,
	                                           Pageable pageable);

	List<LeaveRequest> findByUserUserId(int userId);
	
}
