package com.alex.spring.security.demo.persistence.entity.utils;

public class UserCustomDetails {

    private String email;

    public UserCustomDetails(String email) {
        this.email = email;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }
}
