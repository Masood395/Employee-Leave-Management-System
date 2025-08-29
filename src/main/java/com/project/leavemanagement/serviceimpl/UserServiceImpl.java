package com.project.leavemanagement.serviceimpl;

import java.time.LocalDateTime;
import java.util.List;

import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.project.leavemanagement.dto.EmployeeLeaveResponse;
import com.project.leavemanagement.dto.PagedResponse;
import com.project.leavemanagement.dto.UserRequest;
import com.project.leavemanagement.dto.UserResponse;
import com.project.leavemanagement.entity.LeaveRequest;
import com.project.leavemanagement.entity.User;
import com.project.leavemanagement.enums.Role;
import com.project.leavemanagement.exception.ResourceNotFoundException;
import com.project.leavemanagement.repository.UserRepo;
import com.project.leavemanagement.service.UserService;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class UserServiceImpl implements UserService {
	
	@Autowired
	private UserRepo ur;
	
	@Autowired
	private ModelMapper modelMapper;
	
	@Autowired
    private  PasswordEncoder passwordEncoder;

	@Override
	public UserResponse createUser(UserRequest dto) {
		if(dto.getRole().equals(Role.ADMIN)) {log.warn("Admin Cant create a new Admin!!");;throw new RuntimeException("You can only ADD Employee and Manager ");}
		String trim = dto.getUserName().trim();
		if(trim.length()<5) {
			throw new IllegalArgumentException("Name should be between 5-20 character");
		}
		dto.setUserName(trim);
		if (ur.findByEmail(dto.getEmail()).orElse(null) != null) {
			log.warn("User with this Email : " + dto.getEmail() + " Already Exist...");
			throw new IllegalArgumentException("User with this Email : " + dto.getEmail() + " Already Exist...");
		}
		User manager=null;
		if(!dto.getRole().equals(Role.MANAGER)) {
			if(dto.getManagerId()!=0) {
				 manager = ur.findById(dto.getManagerId()).orElseThrow(()->new ResourceNotFoundException("Manager with ID "+dto.getManagerId()+" Not available"));
				 if(manager.getRole()!=Role.MANAGER) {
					 throw new IllegalArgumentException("Manager with ID "+dto.getManagerId()+" Not Available!!");
				 }
			}
			if(dto.getManagerId()==0&&dto.getRole().equals(Role.EMPLOYEE)) {
				throw new IllegalArgumentException("Add a valid Manger for Employee "+dto.getUserName());
			}
		}
		
		User user = convertToUser(dto);
		user.setPassword(passwordEncoder.encode(dto.getPassword()));
		log.info("Password is Encoded Successfully..");
		user.setCreatedAt(LocalDateTime.now());
		user.setManager(manager);
		log.info("User Created Successfully..");
		return modelMapper.map(ur.save(user), UserResponse.class);
	}

	@Override
	public UserResponse updateUser(int id, UserRequest dto) {
		User user = ur.findById(id).orElseThrow(()-> new ResourceNotFoundException("User with ID "+id+" Not Available!!"));
		if(user.getRole().equals(Role.ADMIN)) {throw new IllegalArgumentException("Can't Update!!!");}
		if(dto.getRole().equals(Role.ADMIN)) {
			throw new RuntimeException("You can only Make Role : Employee Or Manager ");}
		String trim = dto.getUserName().trim();
		if(trim.length()<5) {
			throw new IllegalArgumentException("Name should be between 5-20 character");
		}
		dto.setUserName(trim);
		User manager=null;
		if(!dto.getRole().equals(Role.MANAGER)) {
			if(dto.getManagerId()!=0) {
				 manager = ur.findById(dto.getManagerId()).orElseThrow(()->new ResourceNotFoundException("Manager with ID "+dto.getManagerId()+" Not available"));
				 if(manager.getRole()!=Role.MANAGER) {
					 throw new IllegalArgumentException("Manager with ID "+dto.getManagerId()+" Not Available!!");
				 }
			}
			if(dto.getManagerId()==0&&dto.getRole().equals(Role.EMPLOYEE)) {
				throw new IllegalArgumentException("Add a valid Manger for Employee "+dto.getUserName());
			}
		}
		//M->E
		if(user.getRole().equals(Role.MANAGER)&&dto.getRole().equals(Role.EMPLOYEE)) {
			if(!ur.findByManager(user).isEmpty()) {throw new IllegalArgumentException("Re-Assign the Employees Working under this Manager!! ");}
			
		}
		
		if(dto.getUserName()!=null) {user.setUserName(dto.getUserName());}
		if(dto.getEmail()!=null) {user.setEmail(dto.getEmail());}
		if(dto.getPassword()!=null) {user.setPassword(passwordEncoder.encode(dto.getPassword()));}
		if(dto.getRole()!=null) {user.setRole(dto.getRole());}
		if (manager.getUserId() == id) {
			if (dto.getRole().equals(Role.EMPLOYEE)) {
				throw new IllegalArgumentException("Manager with ID " + dto.getManagerId() + " Not Available!!");
			}
		}
		user.setManager(manager);
		
		user.setUpdatedAt(LocalDateTime.now());
		return modelMapper.map(ur.save(user), UserResponse.class);
	}

	@Override
	public PagedResponse<UserResponse> getAllUsers(int pageNo, int pageSize, String field, String dir) {
		Sort sort = (dir.equalsIgnoreCase("asc") ? Sort.by(Direction.ASC, field) : Sort.by(Direction.DESC, field));
		Pageable pageable = PageRequest.of(pageNo, pageSize, sort);
		Page<User> page = ur.findAll(pageable);
		List<User> userList =page.toList();
		List<UserResponse> dtoList= userList.stream().map(u->modelMapper.map(u, UserResponse.class)).toList();
		return new PagedResponse<>(dtoList, page.getNumber(), page.getSize(), page.getTotalElements(),
					page.getTotalPages(), page.isLast());
	}

	@Override
	public UserResponse getUserById(int id) {
		User user = ur.findById(id).orElseThrow(()-> new ResourceNotFoundException("User with ID "+id+" Not Available!!"));
		return modelMapper.map(user, UserResponse.class);
	}

	@Override
	public void deleteUser(int id) {
		User user = ur.findById(id).orElseThrow(
				()-> new ResourceNotFoundException("User with ID "+id+" Not Available!!"));	
		if(user.getRole().equals(Role.ADMIN)) {throw new IllegalArgumentException("Can't Delete ADMIN!!!");}
		ur.delete(user);
	}
	

	private User convertToUser(UserRequest dto) {
		 User u=new User(dto.getUserName(),dto.getEmail(),dto.getPassword(),dto.getRole());
		 System.out.println(u);
		 return u;
	}

	@Override
	public PagedResponse<?> searchUsers(String username, String email, Role role, int pageNo, int pageSize,
			String sortBy, String sortDir) {
		Sort sort = (sortDir.equalsIgnoreCase("asc") ? Sort.by(Direction.ASC, sortBy) : Sort.by(Direction.DESC, sortBy));
		Pageable pageable = PageRequest.of(pageNo, pageSize, sort);
		Page<User> page = ur.searchUsers(username, email, role, pageable);
		List<User> user = page.toList();
		if(user.isEmpty()) {throw new IllegalArgumentException("Not Available!!");}
		List<UserResponse> dtoList = user.stream().map(l-> modelMapper.map(l, UserResponse.class)).toList();
		return new PagedResponse<>(dtoList, page.getNumber(), page.getSize(), page.getTotalElements(),
					page.getTotalPages(), page.isLast());	}
}
