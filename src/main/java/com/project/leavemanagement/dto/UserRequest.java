package com.project.leavemanagement.dto;


import com.project.leavemanagement.enums.Role;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@ToString
@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class UserRequest {

	@NotBlank(message = "User Name is Required!!")
	@Size(min = 5,max=20,message = "Name should be between 5-20 character")
	private String userName;
	@Email(message = "Enter a valid Email!!")
	private String email;
	@NotBlank(message = "Password is Required!!")
	private String password;
	private Role role;
	private int managerId;
	
}
