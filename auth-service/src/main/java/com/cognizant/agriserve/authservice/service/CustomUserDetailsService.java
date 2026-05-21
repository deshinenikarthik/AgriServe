package com.cognizant.agriserve.authservice.service;

import com.cognizant.agriserve.authservice.dto.UserDTO;
import com.cognizant.agriserve.authservice.client.UserClient;
import com.cognizant.agriserve.authservice.security.CustomUserDetails;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@Slf4j
public class CustomUserDetailsService implements UserDetailsService {

    @Autowired
    private UserClient userClient;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        log.info("Loading user details for: {}", username);
        UserDTO user = userClient.login(username);

        if (user == null) {
            throw new UsernameNotFoundException("User not found with username: " + username);
        }

        List<GrantedAuthority> authorities = new ArrayList<>();
        if (user.getRole() != null) {
            authorities.add(new SimpleGrantedAuthority(user.getRole()));
        }

        // 👇 UPDATED RETURN STATEMENT
        return new CustomUserDetails(
                user.getEmail(),
                user.getPassword(),
                authorities,
                user.getUserId(),
                user.getName(),         // Pass name
                user.getContactInfo()   // Pass contact info
        );
    }
}