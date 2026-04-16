package com.DhrubaStudio.E_commercePlatform.user;

import com.DhrubaStudio.E_commercePlatform.security.JwtUtilities;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/users")
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final JwtUtilities jwtUtilities;

    @Autowired
    public AuthController(AuthenticationManager authenticationManager, JwtUtilities jwtUtilities) {
        this.authenticationManager = authenticationManager;
        this.jwtUtilities = jwtUtilities;
    }

    public static class LoginRequest {
        public String email;
        public String password;
    }

    @PostMapping("/login")
    public ResponseEntity<String> login(@RequestBody LoginRequest loginRequest) {
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(loginRequest.email, loginRequest.password));
            SecurityContextHolder.getContext().setAuthentication(authentication);

            String jwt = jwtUtilities.generateToken(loginRequest.email);
            return new ResponseEntity<>(jwt, HttpStatus.OK);

        } catch(AuthenticationException e) {
            return new ResponseEntity<>("Invalid email or password.", HttpStatus.UNAUTHORIZED);
        }
    }

}
