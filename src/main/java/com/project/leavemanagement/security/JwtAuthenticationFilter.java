package com.project.leavemanagement.security;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.project.leavemanagement.dto.ApiResponse;
import com.project.leavemanagement.entity.AuthToken;
import com.project.leavemanagement.repository.AuthTokenRepo;

import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.MalformedJwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    @Autowired
    private JwtUtil jwtService;

    @Autowired
    private CustomUserDetailsService userDetailsService;

    @Autowired
    private AuthTokenRepo authTokenRepo;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        final String authHeader = request.getHeader("Authorization");

        try {
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                filterChain.doFilter(request, response);
                return;
            }

            String jwt = authHeader.substring(7);
            String username = jwtService.extractUsername(jwt);

            if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {

                Optional<AuthToken> tokenEntityOpt = authTokenRepo.findByAccessToken(jwt);
                if (tokenEntityOpt.isEmpty()) {
                    writeResponse(response, new ResponseEntity<>(
                            new ApiResponse(false, "Access token not recognized"), HttpStatus.UNAUTHORIZED));
                    return;
                }

                AuthToken tokenEntity = tokenEntityOpt.get();

                if (tokenEntity.isAccessTokenRevoked()) {
                    writeResponse(response, new ResponseEntity<>(
                            new ApiResponse(false, "Access token revoked"), HttpStatus.UNAUTHORIZED));
                    return;
                }
                if (tokenEntity.getAccessTokenExpiry().isBefore(LocalDateTime.now())) {
                    writeResponse(response, new ResponseEntity<>(
                            new ApiResponse(false, "Access token expired"), HttpStatus.UNAUTHORIZED));
                    return;
                }

                if (jwtService.validateToken(jwt)) {
                    UserDetails userDetails = userDetailsService.loadUserByUsername(username);
                    UsernamePasswordAuthenticationToken authToken =
                            new UsernamePasswordAuthenticationToken(
                                    userDetails,
                                    null,
                                    userDetails.getAuthorities());

                    authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(authToken);
                }
            }
            filterChain.doFilter(request, response);

        } catch (ExpiredJwtException e) {
            writeResponse(response, new ResponseEntity<>(
                    new ApiResponse(false, "JWT token expired"), HttpStatus.UNAUTHORIZED));
        } catch (MalformedJwtException e) {
            writeResponse(response, new ResponseEntity<>(
                    new ApiResponse(false, "Invalid JWT token"), HttpStatus.UNAUTHORIZED));
        } catch (IllegalArgumentException e) {
            writeResponse(response, new ResponseEntity<>(
                    new ApiResponse(false, "JWT claims string is empty"), HttpStatus.BAD_REQUEST));
        }
    }

    private void writeResponse(HttpServletResponse response, ResponseEntity<ApiResponse> entity) throws IOException {
        response.setStatus(entity.getStatusCodeValue());
        response.setContentType("application/json");

        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        response.getWriter().write(mapper.writeValueAsString(entity.getBody()));
    }
}
