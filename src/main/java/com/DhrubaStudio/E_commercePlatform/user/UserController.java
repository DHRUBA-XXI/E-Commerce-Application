package com.DhrubaStudio.E_commercePlatform.user;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
         try{
             User newUser = userService.registerCustomer(user);
             UserResponseDTO userResponseDTO = new UserResponseDTO(
                     newUser.getId(),
                     newUser.getEmail(),
                     newUser.getPhoneNumber(),
                     newUser.getRole().name()
             );

             return new ResponseEntity<>(userResponseDTO, HttpStatus.CREATED);
         }catch (IllegalArgumentException e){
             return new ResponseEntity<>(e.getMessage(),HttpStatus.BAD_REQUEST);
         }
     }
}
