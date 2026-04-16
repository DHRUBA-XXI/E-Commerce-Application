package com.DhrubaStudio.E_commercePlatform.security;

import com.DhrubaStudio.E_commercePlatform.exception.ErrorResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.LocalDateTime;

@Component
public class JwtFilter extends OncePerRequestFilter {

    private final JwtUtilities jwtUtilities;
    private final CustomUserDetailsService userDetailsService;
    private final ObjectMapper objectMapper = new ObjectMapper().findAndRegisterModules();

    @Autowired
    public JwtFilter(JwtUtilities jwtUtilities, CustomUserDetailsService userDetailsService) {
        this.jwtUtilities = jwtUtilities;
        this.userDetailsService = userDetailsService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        String authorization = request.getHeader("Authorization");
        String email = null;
        String jwt = null;

        // 1. Check if the header exists and starts with "Bearer "
        if (authorization != null && authorization.startsWith("Bearer ")) {
            jwt = authorization.substring(7);
            try {
                email = jwtUtilities.extractUsername(jwt);
            } catch (ExpiredJwtException e) {
                sendJsonError(response, HttpStatus.UNAUTHORIZED, "Token Expired", "Your session has expired, please log in again.");
                return; // Stop the request!
            } catch (JwtException e) {
                sendJsonError(response, HttpStatus.UNAUTHORIZED, "Invalid Token", "Authentication failed, please log in again.");
                return; // Stop the request!
            }
        }

        // 2. If token is good but the user isn't logged into the Spring Context yet
        if (email != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            UserDetails userDetails = userDetailsService.loadUserByUsername(email);

            // Note: In a real app, you would also call jwtUtilities.validateToken() here just to be 100% sure!

            UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
                    userDetails,
                    null,
                    userDetails.getAuthorities());
            auth.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

            // Set the VIP wristband!
            SecurityContextHolder.getContext().setAuthentication(auth);
        }

        // 3. Move to the next filter
        filterChain.doFilter(request, response);
    }

    // HELPER METHOD: Converts our Java ErrorResponse into an HTTP JSON response
    private void sendJsonError(HttpServletResponse response, HttpStatus status, String errorTitle, String message) throws IOException {
        response.setStatus(status.value());
        response.setContentType("application/json");

        ErrorResponse errorResponse = new ErrorResponse(
                LocalDateTime.now(),
                status.value(),
                errorTitle,
                message
        );

        // Convert the object to a JSON string and write it to the response body
        response.getWriter().write(objectMapper.writeValueAsString(errorResponse));
    }
}