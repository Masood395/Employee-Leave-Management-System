package com.project.leavemanagement.config;

import com.project.leavemanagement.entity.User;
import com.project.leavemanagement.enums.Role;
import com.project.leavemanagement.repository.UserRepo;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class DataInitializer {

    @Bean
    CommandLineRunner initAdmin(UserRepo userRepo, PasswordEncoder passwordEncoder) {
        return args -> {
            if (userRepo.findByEmail("admin@gmail.com").orElse(null) == null) {
                User admin = new User();
                admin.setUserName("Admin");
                admin.setEmail("admin@gmail.com");
                admin.setPassword(passwordEncoder.encode("admin@123")); // encode password!
                admin.setRole(Role.ADMIN);
                userRepo.save(admin);
                System.out.println("✅ Default admin created: admin@example.com / Admin@123");
            }
        };
    }
}
