package com.leave_management_system.repository;

import com.leave_management_system.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface UserRepository extends JpaRepository<User, Long> {
    User findByUsername(String username);
    void deleteById(Long id);
    List<User> findByDepartment(String department);
}