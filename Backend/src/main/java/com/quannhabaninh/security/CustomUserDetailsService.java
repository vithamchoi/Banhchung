package com.quannhabaninh.security;

import com.quannhabaninh.entity.User;
import com.quannhabaninh.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.stream.Collectors;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    @Autowired
    private UserRepository userRepository;

    @Override
    @Transactional
    public UserDetails loadUserByUsername(String emailOrPhone) throws UsernameNotFoundException {
        User user;

        // Determine if input is email or phone number
        if (emailOrPhone.contains("@")) {
            // Input is email
            user = userRepository.findByEmail(emailOrPhone)
                    .orElseThrow(() -> new UsernameNotFoundException("User not found with email: " + emailOrPhone));
        } else {
            // Input is phone number
            user = userRepository.findByPhoneNumber(emailOrPhone)
                    .orElseThrow(
                            () -> new UsernameNotFoundException("User not found with phone number: " + emailOrPhone));
        }

        return buildUserDetails(user);
    }

    /**
     * Load user by actual username field (used for JWT token validation)
     */
    @Transactional
    public UserDetails loadUserByUsernameOnly(String username) throws UsernameNotFoundException {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with username: " + username));
        return buildUserDetails(user);
    }

    private UserDetails buildUserDetails(User user) {
        return org.springframework.security.core.userdetails.User.builder()
                .username(user.getUsername())
                .password(user.getPassword())
                .authorities(getAuthorities(user))
                .accountExpired(false)
                .accountLocked(!user.getEnabled())
                .credentialsExpired(false)
                .disabled(!user.getEnabled())
                .build();
    }

    private Collection<? extends GrantedAuthority> getAuthorities(User user) {
        return user.getRoles().stream()
                .map(role -> new SimpleGrantedAuthority(role.name()))
                .collect(Collectors.toList());
    }
}
