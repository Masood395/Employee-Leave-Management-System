package com.project.leavemanagement.service;

import java.time.LocalDate;
import java.util.List;

import com.project.leavemanagement.dto.Action;
import com.project.leavemanagement.dto.AdminLeaveResponse;
import com.project.leavemanagement.dto.EmployeeLeaveRequest;
import com.project.leavemanagement.dto.EmployeeLeaveResponse;
import com.project.leavemanagement.dto.ManagerLeaveResponse;
import com.project.leavemanagement.dto.PagedResponse;
import com.project.leavemanagement.enums.Status;

public interface LeaveService {

	EmployeeLeaveResponse applyLeave(EmployeeLeaveRequest leave);
	PagedResponse<EmployeeLeaveResponse> getAplliedLeave(int pageNo, int pageSize, String sortBy, String sortDir);
	PagedResponse<AdminLeaveResponse> getAllLeaves(int pageNo, int pageSize, String sortBy, String sortDir);
	PagedResponse<ManagerLeaveResponse> getPendings( String status,int pageNo, int pageSize, String sortBy, String sortDir);
	ManagerLeaveResponse managerAction(int id, Action action);
	PagedResponse<?> searchAplliedLeave(Integer id, Status status, LocalDate startDate,
			LocalDate endDate, String reason, int pageNo,int pageSize, 
			String sortBy, String sortDir);
	
}
