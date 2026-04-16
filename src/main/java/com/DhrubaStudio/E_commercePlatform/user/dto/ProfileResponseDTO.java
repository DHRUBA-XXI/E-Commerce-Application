package com.DhrubaStudio.E_commercePlatform.user.dto;

import java.time.LocalDateTime;

public class ProfileResponseDTO {

    private String email;
    private String firstName;
    private String lastName;
    private String address;
    private LocalDateTime timestamp;

    public ProfileResponseDTO(String email, String firstName, String lastName, String address, LocalDateTime timestamp) {
        this.email = email;
        this.firstName = firstName;
        this.lastName = lastName;
        this.address = address;
        this.timestamp = LocalDateTime.now();
    }

    public ProfileResponseDTO() {}

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }

}
