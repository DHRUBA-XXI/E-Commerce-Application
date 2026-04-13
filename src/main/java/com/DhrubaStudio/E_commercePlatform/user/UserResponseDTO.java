package com.DhrubaStudio.E_commercePlatform.user;

public class UserResponseDTO {
    private Long id;
    private String email;
    private String phoneNumber;
    private String role;

    public UserResponseDTO() {}

    public UserResponseDTO(Long id, String email, String phoneNumber, String role) {
        this.id = id;
        this.email = email;
        this.phoneNumber = phoneNumber;
        this.role = role;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }
}
