package com.leave_management_system.repository;

import com.leave_management_system.model.Department;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DepartmentRepository extends JpaRepository<Department, Long> {
    Department findByName(String name);
    void deleteById(Long id);
}
