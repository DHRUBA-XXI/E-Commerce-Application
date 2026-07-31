package com.DhrubaStudio.E_commercePlatform.security;

import com.DhrubaStudio.E_commercePlatform.user.User;
import com.DhrubaStudio.E_commercePlatform.user.UserRepository;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    @Autowired
    public CustomUserDetailsService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        log.debug("Spring Security is attempting to load user details for email: {}", email);

        User user = userRepository.findByEmail(email).orElse(null);
        if (user == null) {
            log.warn("Authentication failed. User not found in database with email: {}", email);
            throw new UsernameNotFoundException("User not found with email: " + email);
        }

        log.debug("Successfully loaded user details for email: {}. Role: {}", email, user.getRole());
        return org.springframework.security.core.userdetails.User.builder()
                .username(user.getEmail())
                .password(user.getPassword())
                .roles(user.getRole().name())
                .build();
    }
}