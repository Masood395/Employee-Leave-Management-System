package com.project.leavemanagement.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import com.project.leavemanagement.dto.EmployeeLeaveRequest;
import com.project.leavemanagement.dto.EmployeeLeaveResponse;
import com.project.leavemanagement.entity.LeaveRequest;
import com.project.leavemanagement.entity.User;
import com.project.leavemanagement.enums.Role;
import com.project.leavemanagement.enums.Status;
import com.project.leavemanagement.repository.LeaveRequestRepo;
import com.project.leavemanagement.repository.UserRepo;
import com.project.leavemanagement.serviceimpl.LeaveServiceImpl;

@ExtendWith(MockitoExtension.class)
class LeaveServiceImplTest {

	@Mock
	private LeaveRequestRepo leaveRepo; 
	@Mock
	private UserRepo userRepo;
	@Mock
	private ModelMapper modelMapper;
	@InjectMocks
	private LeaveServiceImpl leaveService;

	@BeforeEach
	void setUp() {
        MockitoAnnotations.openMocks(this);
    }
	
	@Test
	 void applyLeave_ShouldSaveLeaveRequest_WhenUserExists() {
	        // Arrange
	        int userId = 3;
	        User user = new User();
	        user.setUserId(userId);
	        user.setEmail("test@example.com");
	        user.setRole(Role.EMPLOYEE);
	        

	        EmployeeLeaveRequest request = new EmployeeLeaveRequest();
	        request.setReason("Vacation");
	        request.setStart_date(LocalDate.now().plusDays(1));
	        request.setEnd_date(LocalDate.now().plusDays(10));

	        // Mock SecurityContext
	        Authentication authentication =
	                new UsernamePasswordAuthenticationToken("test@example.com", null, List.of());
	        SecurityContext context = SecurityContextHolder.createEmptyContext();
	        context.setAuthentication(authentication);
	        SecurityContextHolder.setContext(context);
	        
	        when(userRepo.findByEmail("test@example.com")).thenReturn(Optional.of(user));
	        when(leaveRepo.save(any(LeaveRequest.class))).thenAnswer(i -> i.getArguments()[0]);

	        when(modelMapper.map(any(EmployeeLeaveRequest.class), eq(LeaveRequest.class)))
	        .thenAnswer(invocation -> {
	            EmployeeLeaveRequest req = invocation.getArgument(0);
	            LeaveRequest entity = new LeaveRequest();
	            entity.setReason(req.getReason());
	            entity.setStart_date(req.getStart_date());
	            entity.setEnd_date(req.getEnd_date());
	            return entity;
	        });

	    when(modelMapper.map(any(LeaveRequest.class), eq(EmployeeLeaveResponse.class)))
	        .thenAnswer(invocation -> {
	            LeaveRequest entity = invocation.getArgument(0);
	            EmployeeLeaveResponse dto = new EmployeeLeaveResponse();
	            dto.setReason(entity.getReason());
	            dto.setStatus(entity.getStatus());
	            return dto;
	        });



	        // Act
	         EmployeeLeaveResponse result = leaveService.applyLeave(request);

	        // Assert
	        assertNotNull(result);
	        assertEquals(Status.PENDING, result.getStatus());
	        assertEquals("Vacation", result.getReason());
	        verify(leaveRepo, times(1)).save(any(LeaveRequest.class));
	    }
	
	
}
