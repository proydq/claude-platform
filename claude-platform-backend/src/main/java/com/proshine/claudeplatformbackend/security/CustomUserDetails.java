package com.proshine.claudeplatformbackend.security;

import com.proshine.claudeplatformbackend.entity.User;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.Collections;

public class CustomUserDetails implements UserDetails {
    
    private String id;
    private String username;
    private String password;
    private String userRole;
    private String userStatus;
    private Collection<? extends GrantedAuthority> authorities;
    
    public CustomUserDetails(User user) {
        this.id = user.getId();
        this.username = user.getUsername();
        this.password = user.getUserPassword();
        this.userRole = user.getUserRole();
        this.userStatus = user.getUserStatus();
        this.authorities = Collections.singletonList(new SimpleGrantedAuthority(user.getUserRole()));
    }
    
    public String getId() {
        return id;
    }
    
    public String getUserRole() {
        return userRole;
    }
    
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }
    
    @Override
    public String getPassword() {
        return password;
    }
    
    @Override
    public String getUsername() {
        return username;
    }
    
    @Override
    public boolean isAccountNonExpired() {
        return true;
    }
    
    @Override
    public boolean isAccountNonLocked() {
        return "ACTIVE".equals(userStatus);
    }
    
    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }
    
    @Override
    public boolean isEnabled() {
        return "ACTIVE".equals(userStatus);
    }
}