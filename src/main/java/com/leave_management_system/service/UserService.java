package com.leave_management_system.service;

import com.leave_management_system.exception.ResourceNotFoundException;
import com.leave_management_system.model.Department;
import com.leave_management_system.model.User;
import com.leave_management_system.repository.UserRepository;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class UserService implements UserDetailsService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @PostConstruct
    public void init() {
        try {
            if (userRepository.findByUsername("admin") == null) {
                String adminUsername = System.getenv("ADMIN_USERNAME");
                String adminPassword = System.getenv("ADMIN_PASSWORD");
                String adminEmail = System.getenv("ADMIN_EMAIL");

                if (adminUsername == null || adminPassword == null || adminEmail == null) {
                    throw new IllegalStateException("Admin credentials not properly configured. Please set ADMIN_USERNAME, ADMIN_PASSWORD, and ADMIN_EMAIL environment variables.");
                }

                User adminUser = new User();
                adminUser.setUsername(adminUsername);
                adminUser.setPassword(passwordEncoder.encode(adminPassword));
                adminUser.setRole("ROLE_ADMIN");
                adminUser.setEmail(adminEmail);
                adminUser.setDepartment("ADMIN");

                userRepository.save(adminUser);
                System.out.println("Admin user created successfully.");
            }
        } catch (Exception e) {
            System.err.println("Error during admin user creation: " + e.getMessage());
            e.printStackTrace();
        }
    }


    public User registerUser(User user) {
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        user.setRole("ROLE_USER");
        return userRepository.save(user);
    }

    public User findByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepository.findByUsername(username);
        if (user == null) {
            throw new UsernameNotFoundException("User not found with username: " + username);
        }
        return new org.springframework.security.core.userdetails.User(
                user.getUsername(),
                user.getPassword(),
                Collections.singletonList(new SimpleGrantedAuthority(user.getRole()))
        );
    }

    public User updateUser(User user) {
        return userRepository.save(user);
    }

    public List<User> getAllUsers() {
        return userRepository.findAll();
    }
    public User findById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));
    }
    public Map<String, List<User>> getUsersByDepartment() {
        List<User> allUsers = userRepository.findAll();
        return allUsers.stream()
                .collect(Collectors.groupingBy(User::getDepartment));
    }
    public List<User> getUsersByDepartment(String department) {
        return userRepository.findByDepartment(department);
    }

    public void changeUserDepartment(Long userId, String newDepartment) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));
        user.setDepartment(newDepartment);
        userRepository.save(user);
    }
    public void deleteUserById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));
        userRepository.deleteById(id);
    }
    public User getUserById(Long id) {
        return userRepository.findById(id).orElseThrow(() -> new RuntimeException("User not found"));
    }





}