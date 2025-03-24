package com.system.academicCourseRegistration.service;

import java.util.Collections;

import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.system.academicCourseRegistration.model.UserItem;
import com.system.academicCourseRegistration.repository.UsersRepository;

public class CustomUserDetailsService implements UserDetailsService {
	
	private UsersRepository userRepository;
	private PasswordEncoder passwordEncoder;

    public CustomUserDetailsService(UsersRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public UserDetails loadUserByUsername(String userName) throws UsernameNotFoundException {
        UserItem userItem = userRepository.findByUsername(userName);
        System.out.println("Fetching user details");
        if (userItem == null) {
        	System.out.println("Cannot find user details");
            throw new UsernameNotFoundException("User not found: " + userName);
        }
        System.out.println("Found user details");
        return new User(
                userItem.getUserName(),
                userItem.getEncryptedPwd(),
                Collections.singletonList(new SimpleGrantedAuthority(userItem.getRole()))
        );
    }
    
    public boolean authenticate(String username, String rawPassword) {
        UserItem userItem = userRepository.findByUsername(username);
        if (userItem == null) {
            return false;
        }
        return passwordEncoder.matches(rawPassword, userItem.getEncryptedPwd()); // Match plain with hashed
    }

}
