package com.DhrubaStudio.E_commercePlatform.user.dto;

public class ProfileUpdateRequestDTO {

    private String firstName;
    private String lastName;
    private String address;

    ProfileUpdateRequestDTO(String firstName, String lastName, String address) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.address = address;
    }

    ProfileUpdateRequestDTO() {}

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

}
