package com.DhrubaStudio.E_commercePlatform.user;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserService {

    private final UserRepository userRepository;

    @Autowired
    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Transactional
    public User registerCustomer(User user) {

        if(userRepository.existsByEmail(user.getEmail())) {
            throw new IllegalArgumentException("A user with this email already exists.");
        }

        if(userRepository.existsByPhoneNumber(user.getPhoneNumber())) {
            throw new IllegalArgumentException("A user with this contact number already exists.");
        }

        user.setRole(User.Role.CUSTOMER);
        CustomerProfile newProfile = new CustomerProfile();
        newProfile.setUser(user);
        user.setCustomerProfile(newProfile);

        return userRepository.save(user);
    }



}
