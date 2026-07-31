package com.DhrubaStudio.E_commercePlatform.user;
import com.DhrubaStudio.E_commercePlatform.user.dto.ProfileUpdateRequestDTO;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Autowired
    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public User registerCustomer(User user) {
        log.info("Attempting to register new customer with email: {}", user.getEmail());

        if(userRepository.existsByEmail(user.getEmail())) {
            log.warn("Registration failed. Email {} is already in use.", user.getEmail());
            throw new IllegalArgumentException("A user with this email already exists.");
        }
        if(userRepository.existsByPhoneNumber(user.getPhoneNumber())) {
            log.warn("Registration failed. Phone number {} is already in use.", user.getPhoneNumber());
            throw new IllegalArgumentException("A user with this contact number already exists.");
        }

        String encodedPassword = passwordEncoder.encode(user.getPassword());
        user.setPassword(encodedPassword);
        user.setRole(User.Role.CUSTOMER);

        CustomerProfile newProfile = new CustomerProfile();
        newProfile.setUser(user);
        user.setCustomerProfile(newProfile);

        User savedUser = userRepository.save(user);
        log.info("Successfully registered new customer with ID: {}", savedUser.getId());

        return savedUser;
    }

    @Transactional
    public User updateCustomerProfile(String email, ProfileUpdateRequestDTO request) {
        log.info("Updating profile details for user: {}", email);

        User user = userRepository.findByEmail(email).orElse(null);
        if(user == null) {
            log.error("Profile update failed. User {} not found in database.", email);
            throw new IllegalArgumentException("User: "+ email +" not found.");
        }

        CustomerProfile profile = user.getCustomerProfile();
        profile.setFirstName(request.getFirstName());
        profile.setLastName(request.getLastName());
        profile.setShippingAddress(request.getAddress());

        User updatedUser = userRepository.save(user);
        log.info("Successfully updated profile for user: {}", email);

        return updatedUser;
    }
}