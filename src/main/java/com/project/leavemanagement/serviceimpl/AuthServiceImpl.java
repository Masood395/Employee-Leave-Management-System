package com.project.leavemanagement.serviceimpl;

import java.time.LocalDateTime;

import org.modelmapper.ModelMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.project.leavemanagement.dto.AuthRequest;
import com.project.leavemanagement.dto.AuthResponse;
import com.project.leavemanagement.entity.AuthToken;
import com.project.leavemanagement.entity.User;
import com.project.leavemanagement.repository.AuthTokenRepo;
import com.project.leavemanagement.repository.UserRepo;
import com.project.leavemanagement.security.JwtUtil;
import com.project.leavemanagement.service.AuthService;


@Service
public class AuthServiceImpl implements AuthService {

	@Autowired
    private  UserRepo ur;
	@Autowired
	private AuthTokenRepo atr;
	@Autowired
    private  JwtUtil jwtUtil;
	@Autowired
    private   AuthenticationManager authenticationManager;

    private static final Logger log = LoggerFactory.getLogger(AuthServiceImpl.class);


    @Override
    public AuthResponse login(AuthRequest loginDTO) {
    	log.info("Checking Login credential....");
    	authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(loginDTO.getEmail(),loginDTO.getPassword()));
        User user = ur.findByEmail(loginDTO.getEmail())
                .orElseThrow(() -> new RuntimeException("Email not found"));
        log.info("User {} is Verified..",user.getUserName());

//        if (!passwordEncoder.matches(loginDTO.getPassword(), user.getPassword())) {
//            throw new RuntimeException("Invalid password");
//        }
        
        atr.findAll().stream()
        .filter(t -> t.getUser().getUserId()==(user.getUserId()) &&
                     (!t.isAccessTokenRevoked() || !t.isRefreshTokenRevoked()))
        .forEach(t -> {
            t.setAccessTokenRevoked(true);
            t.setRefreshTokenRevoked(true);
            atr.save(t);
        });
        
        
        String accessToken = jwtUtil.generateToken(user.getEmail(), user.getRole().name(),true);
        log.info("Access Token Generated ");
        String refreshToken = jwtUtil.generateToken(user.getUserName(), user.getRole().name(),false);
        log.info("Refresh Token Generated ");

        AuthToken token = new AuthToken();
        token.setUser(user);
        token.setAccessToken(accessToken);
        token.setAccessTokenExpiry(LocalDateTime.now().plusSeconds(jwtUtil.getJwtExpirationMs() / 1000));
        token.setAccessTokenRevoked(false);
        token.setRefreshToken(refreshToken);
        token.setRefreshTokenExpiry(LocalDateTime.now().plusSeconds(jwtUtil.getJwtRefExpirationMs() / 1000));
        token.setRefreshTokenRevoked(false);

        atr.save(token);
        
        ur.save(user);
        return new AuthResponse(accessToken,refreshToken);
    }


	@Override
	public AuthResponse getAccessToken(String refreshToken) {
		var tokenEntity = atr.findByRefreshToken(refreshToken)
                .orElseThrow(() -> new RuntimeException("Invalid refresh token"));

        if (tokenEntity.isRefreshTokenRevoked()
                || tokenEntity.getRefreshTokenExpiry().isBefore(LocalDateTime.now())) {
        	log.warn("Refresh token expired or revoked");
            throw new RuntimeException("Refresh token expired or revoked");
        }

        User user = tokenEntity.getUser();
        
        atr.findAll().stream()
        .filter(t -> t.getUser().getUserId()==(user.getUserId()) && !t.isAccessTokenRevoked())
        .forEach(t -> {
            t.setAccessTokenRevoked(true);
            atr.save(t);
        });

        String newAccessToken = jwtUtil.generateToken(user.getEmail(), user.getRole().name(), true);

        tokenEntity.setAccessToken(newAccessToken);
        tokenEntity.setAccessTokenExpiry(jwtUtil.extractExpiry(newAccessToken).toInstant()
                .atZone(java.time.ZoneId.systemDefault()).toLocalDateTime());
        tokenEntity.setAccessTokenRevoked(false);

        atr.save(tokenEntity);
        log.info("Access Token Generated Successfully");

        return new AuthResponse(newAccessToken, tokenEntity.getRefreshToken());
	}


	@Override
	public void loggedOut() {
		 String currentEmail = SecurityContextHolder.getContext().getAuthentication().getName();
		    User user = ur.findByEmail(currentEmail)
		            .orElseThrow(() -> new RuntimeException("User not found"));
		    log.info("Logging Out User: {}",user.getUserName());
		    atr.findAll().stream()
	        .filter(t -> t.getUser().getUserId()==(user.getUserId()) &&
	                     (!t.isAccessTokenRevoked() || !t.isRefreshTokenRevoked()))
	        .forEach(t -> {
	            t.setAccessTokenRevoked(true);
	            t.setRefreshTokenRevoked(true);
	            atr.save(t);
	        });
		    log.debug("All Tokens are Revoked...");
	}
    
  
}

