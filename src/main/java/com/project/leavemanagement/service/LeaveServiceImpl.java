package com.project.leavemanagement.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import org.modelmapper.ModelMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import com.project.leavemanagement.dto.Action;
import com.project.leavemanagement.dto.AdminLeaveResponse;
import com.project.leavemanagement.dto.EmployeeLeaveRequest;
import com.project.leavemanagement.dto.EmployeeLeaveResponse;
import com.project.leavemanagement.dto.ManagerLeaveResponse;
import com.project.leavemanagement.dto.PagedResponse;
import com.project.leavemanagement.entity.LeaveRequest;
import com.project.leavemanagement.entity.User;
import com.project.leavemanagement.enums.Role;
import com.project.leavemanagement.enums.Status;
import com.project.leavemanagement.exception.ResourceNotFoundException;
import com.project.leavemanagement.repository.LeaveRequestRepo;
import com.project.leavemanagement.repository.UserRepo;
import org.springframework.security.access.AccessDeniedException;

@Service
public class LeaveServiceImpl implements LeaveService {

	@Autowired
	private LeaveRequestRepo lr;
	
	@Autowired
	private UserRepo ur;
	
	@Autowired
	private EmailService emailService;
	
	@Autowired
	private ModelMapper modelMapper;
	
    private static final Logger log = LoggerFactory.getLogger(LeaveServiceImpl.class);

	private User getLoggedInUser() {
		String email = SecurityContextHolder.getContext().getAuthentication().getName();
	    return ur.findByEmail(email)
	                 .orElseThrow(() -> new RuntimeException("User not found"));
	}
	
	@Override
	public EmployeeLeaveResponse applyLeave(EmployeeLeaveRequest leave) {
	    User user = getLoggedInUser();
        log.debug("Applying leave for {} with payload: start={}, end={}", user.getUserName(), leave.getStart_date(), leave.getEnd_date());
	    if (!user.getRole().equals(Role.EMPLOYEE)) {
	        throw new IllegalArgumentException("You are not an Employee!!");
	    }

	    if (leave.getStart_date().isAfter(leave.getEnd_date())) {
            log.warn("Invalid date range for user {}: {} - {}", user.getUserName(), leave.getStart_date(), leave.getEnd_date());
	        throw new IllegalArgumentException("End-Date can't be before Start-Date!!");
	    }

	    if (leave.getStart_date().isBefore(LocalDateTime.now().toLocalDate())) {
	        throw new IllegalArgumentException("Cannot apply for leave in the past!!");
	    }

	    List<LeaveRequest> existingLeaves = lr.findByUserUserId(user.getUserId());

	    for (LeaveRequest existing : existingLeaves) {
	        if (existing.getStatus().equals(Status.PENDING) || existing.getStatus().equals(Status.APPROVED)) {
	            boolean overlaps = !leave.getEnd_date().isBefore(existing.getStart_date()) &&
	                               !leave.getStart_date().isAfter(existing.getEnd_date());

	            if (overlaps) {
	                throw new IllegalArgumentException(
	                        "Leave request overlaps with an existing leave (" +
	                        existing.getStart_date() + " to " + existing.getEnd_date() + ")");
	            }
	        }
	    }

	    LeaveRequest leaveRequest = modelMapper.map(leave, LeaveRequest.class);
	    leaveRequest.setAppliedAt(LocalDateTime.now());
	    leaveRequest.setUser(user);
	    LeaveRequest saved = lr.save(leaveRequest);
        log.info("Leave saved id={} by user={}", saved.getId(), user.getUserName());
	    return modelMapper.map(saved, EmployeeLeaveResponse.class);
	}


	@Override
	public PagedResponse<EmployeeLeaveResponse> getAplliedLeave(int pageNo, int pageSize, String field, String dir) {
		User user = getLoggedInUser();
		Sort sort = (dir.equalsIgnoreCase("asc") ? Sort.by(Direction.ASC, field) : Sort.by(Direction.DESC, field));
		Pageable pageable = PageRequest.of(pageNo, pageSize, sort);
		Page<LeaveRequest> page = lr.findByUser(user,pageable);
		List<LeaveRequest> leaves = page.toList();
		if(leaves.isEmpty()) {throw new IllegalArgumentException("You didnt applied for Leave");}
		List<EmployeeLeaveResponse> dtoList = leaves.stream().map(l-> modelMapper.map(l, EmployeeLeaveResponse.class)).toList();
		return new PagedResponse<>(dtoList, page.getNumber(), page.getSize(), page.getTotalElements(),
					page.getTotalPages(), page.isLast());
	}

	@Override
	public PagedResponse<AdminLeaveResponse> getAllLeaves(int pageNo, int pageSize, String field, String dir) {
		Sort sort = (dir.equalsIgnoreCase("asc") ? Sort.by(Direction.ASC, field) : Sort.by(Direction.DESC, field));
		Pageable pageable = PageRequest.of(pageNo, pageSize, sort);
		Page<LeaveRequest> page = lr.findAll(pageable);
		List<LeaveRequest> all = page.toList();
		if(all.isEmpty()) {throw new IllegalArgumentException("NO Leave Request is Present..!!");}
		List<AdminLeaveResponse> dtoList = all.stream().map(l->modelMapper.map(l, AdminLeaveResponse.class)).toList();
		return new PagedResponse<>(dtoList, page.getNumber(), page.getSize(), page.getTotalElements(),
				page.getTotalPages(), page.isLast());
	}

	@Override
	public PagedResponse<ManagerLeaveResponse> getPendings(String status,int pageNo, int pageSize, String field, String dir) {
		Status s=Status.valueOf(status.toUpperCase());
//		if(!ur.findById(mid).get().getRole().equals(Role.MANAGER)) {
//			throw new IllegalArgumentException("Access Denied!! : Your not a Manager..");
//		}
		User manager = getLoggedInUser();
		int mid=manager.getUserId();
		Sort sort = (dir.equalsIgnoreCase("asc") ? Sort.by(Direction.ASC, field) : Sort.by(Direction.DESC, field));
		Pageable pageable = PageRequest.of(pageNo, pageSize, sort);
		Page<LeaveRequest> page = lr.findByUser_Manager_UserIdAndStatus( mid,s,pageable);
		List<LeaveRequest> list = page.toList();
		if(list.isEmpty()) {throw new ResourceNotFoundException("There is No "+s+" Leave Requests....");}
		List<ManagerLeaveResponse> dtoList = list.stream().map(l->modelMapper.map(l, ManagerLeaveResponse.class)).toList();
		return new PagedResponse<>(dtoList, page.getNumber(), page.getSize(), page.getTotalElements(),
				page.getTotalPages(), page.isLast());
	}

	@Override
	public ManagerLeaveResponse managerAction(int id,Action action) {
		if(action.getAction().equals(Status.PENDING)) {throw new IllegalArgumentException ("Invalid Action !!");}
		LeaveRequest leave = lr.findById(id).orElseThrow(()->new ResourceNotFoundException("Leave Request with Id "+id+" Not Available"));
		
//		if(!ur.findById(mid).get().getRole().equals(Role.MANAGER)) {
//			throw new IllegalArgumentException("Access Denied!! : Your not a Manager..");
//		}
		User manager = getLoggedInUser();
		int mid=manager.getUserId();
		
		
		if(leave.getUser().getManager().getUserId()!=mid) {
			throw new AccessDeniedException("You Dont have a Access to this Employee's Leave Request..!!!");
		}
		
		if((leave.getStart_date().isBefore(LocalDate.now())||leave.getStart_date().isEqual(LocalDate.now()))&&!leave.getStatus().equals(Status.PENDING)) {
			throw new IllegalArgumentException("Cant Update Leave Request after it has Started!!");
		}else if(leave.getStart_date().isBefore(LocalDate.now())||leave.getStart_date().isEqual(LocalDate.now())){
			throw new IllegalArgumentException("Cant Update Leave Request after it  Start-Date is Over!!");
		}
		
		if(!leave.getStatus().equals(Status.PENDING)) {throw new RuntimeException("This Leave Request is Already "+leave.getStatus()+" !!");}
		
		leave.setStatus(action.getAction());
		leave.setUpdatedAt(LocalDateTime.now());
		LeaveRequest save=lr.save(leave);
        log.info("Leave id={} {} and audited by {}", save.getId(),save.getStatus(), manager.getUserName());
		
		//mail send
		String body = (action.getAction().equals(Status.APPROVED)) ? "Dear " + leave.getUser().getUserName() + ",\r\n"
				+ "\r\n" + "I am pleased to inform you that your leave request from " + leave.getStart_date() + " to "
				+ leave.getEnd_date()
				+ " has been approved. Please ensure all pending work is completed and hand over your responsibilities to your backup before your time off.\r\n"
				+ "\r\n"
				+ "If you have any questions or need to discuss work arrangements during your absence, feel free to reach out. We wish you a restful break and look forward to your return.\r\n"
				+ "\r\n" + "Best regards,\r\n" + "" + manager.getUserName() + "\r\n" + "" + manager.getRole() + "\r\n"
				+ "GOOGLE \r\n" + "\r\n" + ""
				: "Dear " + leave.getUser().getUserName() + ",\r\n" + "\r\n"
						+ "Thank you for submitting your leave request for the period of " + leave.getStart_date()
						+ " to " + leave.getEnd_date()
						+ ". After careful consideration, I regret to inform you that your request cannot be approved at this time due to team workload and project deadlines.\r\n"
						+ "\r\n"
						+ "If possible, please propose alternative dates for your leave, and I would be happy to discuss further options with you. Thank you for your understanding.\r\n"
						+ "\r\n" + "Kind regards,\r\n" + manager.getUserName() + "\r\n" + manager.getRole() + "\r\n"
						+ "GOOGLE";
				
				String subject = "Your Leave Request has been " + action.getAction() ;
				emailService.sendLeaveStatusEmail("clasroom999@gmail.com", subject, body);
//				emailService.sendLeaveStatusEmail(leave.getUser().getEmail(), subject, body);
		
		return modelMapper.map(save, ManagerLeaveResponse.class);
	}
	
	

	@Override
	public PagedResponse<?> searchAplliedLeave(Integer id, Status status, LocalDate startDate,
			LocalDate endDate,String reason, int pageNo, int pageSize, String sortBy,
			String sortDir) {
		if(getLoggedInUser().getRole().equals(Role.EMPLOYEE)) {id=getLoggedInUser().getUserId();}
		Sort sort = (sortDir.equalsIgnoreCase("asc") ? Sort.by(Direction.ASC, sortBy) : Sort.by(Direction.DESC, sortBy));
		Pageable pageable = PageRequest.of(pageNo, pageSize, sort);
		Page<LeaveRequest> page = lr.searchLeaveRequests(status, startDate, endDate, reason, id, pageable);
		List<LeaveRequest> leaves = page.toList();
		if(leaves.isEmpty()) {throw new IllegalArgumentException("Not Available!!");}
		List<EmployeeLeaveResponse> dtoList = leaves.stream().map(l-> modelMapper.map(l, EmployeeLeaveResponse.class)).toList();
		return new PagedResponse<>(dtoList, page.getNumber(), page.getSize(), page.getTotalElements(),
					page.getTotalPages(), page.isLast());
	}

}
