package com.example.complaintsystemmanagement;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.http.ResponseEntity;



@Service
public class UserService {
    private static final Logger logger = LoggerFactory.getLogger(UserService.class);


    @Autowired
    private UserRepository userRepository;
    @Autowired
    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public User getUserById(Long userId) {
        return userRepository.findById(userId).orElse(null);
    }

    @Autowired
    private PasswordEncoder passwordEncoder;

    public User findByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    public void save(User user) {
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        userRepository.save(user);
    }

    public boolean authenticate(String username, String password) {
        User user = userRepository.findByUsername(username);
        if (user != null) {
            return passwordEncoder.matches(password, user.getPassword());
        }
        return false;
    }
    public long getNumberOfUsers() {
        return userRepository.countAllUsers();
    }
    public User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        return userRepository.findByUsername(username);
    }
    public static class UserNotFoundException extends RuntimeException {
        public UserNotFoundException(String message) {
            super(message);
        }
    }

    public void updateUserDetails(Long id, String username, String address, String phoneNumber, String email) {
        User currentUser = userRepository.findById(id).orElse(null);
        if (currentUser != null) {
            currentUser.setUsername(username);
            currentUser.setAddress(address);
            currentUser.setPhoneNumber(phoneNumber);
            currentUser.setEmail(email);
            userRepository.save(currentUser);
        } else {

            logger.error("User not found with ID: " + id);
            throw new UserNotFoundException("User not found with ID: " + id);
        }
    }
    public Long getCurrentUserId() {
        User currentUser = getCurrentUser();
        return (currentUser != null) ? currentUser.getId() : null;
    }
}