
package com.taskflow.taskflowpro.security;

import com.taskflow.taskflowpro.entity.User;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import java.util.Collection;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import java.util.List;

public class CustomUserDetails implements UserDetails {

    private final User user;

    public CustomUserDetails(User user) {
        this.user = user;
    }

    public User getUser() {
        return user;
    }


    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {

        String role = user.getRole();

        // Support old TaskFlow accounts
        // MANAGER -> FACULTY
        // EMPLOYEE -> STUDENT
        if ("MANAGER".equalsIgnoreCase(role)) {
            role = "FACULTY";
        }

        if ("EMPLOYEE".equalsIgnoreCase(role)) {
            role = "STUDENT";
        }

        return List.of(
                new SimpleGrantedAuthority("ROLE_" + role.toUpperCase())
        );
    }
    @Override
    public String getPassword() {
        return user.getPassword();
    }

    @Override
    public String getUsername() {
        return user.getEmail();
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }
}

