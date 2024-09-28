package com.leave_management_system.config;

import com.leave_management_system.model.Department;
import com.leave_management_system.repository.DepartmentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;

@Component
public class DatabaseInitializer implements CommandLineRunner {

    @Autowired
    private DepartmentRepository departmentRepository;

    @Override
    public void run(String... args) throws Exception {
        initializeDepartments();
    }

    private void initializeDepartments() {
        if (departmentRepository.count() == 0) {
            List<Department> departments = Arrays.asList(
                    new Department("IT"),
                    new Department("HR"),
                    new Department("Finance"),
                    new Department("Marketing"),
                    new Department("Operations")
            );
            departmentRepository.saveAll(departments);
        }
    }
}