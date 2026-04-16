package com.DhrubaStudio.E_commercePlatform.user;

import com.DhrubaStudio.E_commercePlatform.user.dto.ProfileUpdateRequestDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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

        if(userRepository.existsByEmail(user.getEmail())) {
            throw new IllegalArgumentException("A user with this email already exists.");
        }

        if(userRepository.existsByPhoneNumber(user.getPhoneNumber())) {
            throw new IllegalArgumentException("A user with this contact number already exists.");
        }

        String encodedPassword = passwordEncoder.encode(user.getPassword());
        user.setPassword(encodedPassword);
        user.setRole(User.Role.CUSTOMER);

        CustomerProfile newProfile = new CustomerProfile();
        newProfile.setUser(user);
        user.setCustomerProfile(newProfile);

        return userRepository.save(user);
    }

    @Transactional
    public User updateCustomerProfile(String email,ProfileUpdateRequestDTO request) {

        User user = userRepository.findByEmail(email).orElse(null);
        if(user == null) {
            throw new IllegalArgumentException("User: "+ email +"not found.");
        }

        CustomerProfile profile = user.getCustomerProfile();

        profile.setFirstName(request.getFirstName());
        profile.setLastName(request.getLastName());
        profile.setShippingAddress(request.getAddress());

        return userRepository.save(user);
    }

}
