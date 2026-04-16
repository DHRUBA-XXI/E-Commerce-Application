package com.DhrubaStudio.E_commercePlatform.user;

import com.DhrubaStudio.E_commercePlatform.user.dto.ProfileResponseDTO;
import com.DhrubaStudio.E_commercePlatform.user.dto.ProfileUpdateRequestDTO;
import com.DhrubaStudio.E_commercePlatform.user.dto.UserResponseDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/users")
public class UserController {
     private final UserService userService;

     @Autowired
     public UserController(UserService userService) {
        this.userService = userService;
     }

     @PostMapping("/register")
     public ResponseEntity<?> registerUser(@RequestBody User user) {

         User newUser = userService.registerCustomer(user);

         UserResponseDTO userResponseDTO = new UserResponseDTO(
                 newUser.getId(),
                 newUser.getEmail(),
                 newUser.getPhoneNumber(),
                 newUser.getRole().name());

         return new ResponseEntity<>(userResponseDTO, HttpStatus.CREATED);
     }

     @PatchMapping("/update-myProfile")
     public ResponseEntity<?> updateMyProfile(@AuthenticationPrincipal UserDetails userDetails,
                                             @RequestBody ProfileUpdateRequestDTO request) {
         String email = userDetails.getUsername();
         User updatedUser = userService.updateCustomerProfile(email, request);
         CustomerProfile profile = updatedUser.getCustomerProfile();

         ProfileResponseDTO responseDTO = new ProfileResponseDTO(
                 updatedUser.getEmail(),
                 profile.getFirstName(),
                 profile.getLastName(),
                 profile.getShippingAddress(),
                 LocalDateTime.now());

         return new ResponseEntity<>(responseDTO, HttpStatus.OK);
     }

}
