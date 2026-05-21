package com.cognizant.agriserve.authservice.security;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.User;
import java.util.Collection;


public class CustomUserDetails extends User {
    private final Long userId;
    private final String name;
    private final String contactInfo;

    public CustomUserDetails(String username, String password, Collection<? extends GrantedAuthority> authorities, Long userId, String name, String contactInfo) {
        super(username, password, authorities);
        this.userId = userId;
        this.name = name;
        this.contactInfo = contactInfo;
    }

    public Long getUserId() {
        return userId;
    }

    public String getName() {
        return name;
    }

    public String getContactInfo() {
        return contactInfo;
    }
}