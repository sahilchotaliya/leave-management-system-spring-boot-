package com.leave_management_system.controller;

import com.leave_management_system.model.User;
import com.leave_management_system.model.Department;
import com.leave_management_system.model.LeaveRequest;
import com.leave_management_system.service.DepartmentService;
import com.leave_management_system.service.EmailService;
import com.leave_management_system.service.LeaveRequestService;
import com.leave_management_system.service.UserService;
import jakarta.mail.MessagingException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/admin")
public class AdminController {

    @Autowired
    private UserService userService;
    @Autowired
    private DepartmentService departmentService;
    @Autowired
    private LeaveRequestService leaveRequestService;
    @Autowired
    private EmailService emailService;

    @GetMapping("/dashboard")
    public String dashboard(Model model, Authentication authentication, @RequestParam(required = false) String department) {
        List<LeaveRequest> pendingRequests = leaveRequestService.getPendingLeaveRequests();
        List<LeaveRequest> approvedRequests = leaveRequestService.getApprovedLeaveRequests();
        List<LeaveRequest> rejectedRequests = leaveRequestService.getRejectedLeaveRequests();
        List<Department> allDepartments = departmentService.getAllDepartments();

        List<User> filteredUsers;
        if (department != null && !department.isEmpty()) {
            filteredUsers = userService.getUsersByDepartment(department);
        } else {
            filteredUsers = userService.getAllUsers();
        }


        Map<Long, Integer> remainingLeaveDays = filteredUsers.stream()
                .collect(Collectors.toMap(
                        User::getId,
                        user -> leaveRequestService.getRemainingLeaveDays(user).size()
                ));

        model.addAttribute("pendingRequests", pendingRequests);
        model.addAttribute("approvedRequests", approvedRequests);
        model.addAttribute("rejectedRequests", rejectedRequests);
        model.addAttribute("pendingCount", pendingRequests.size());
        model.addAttribute("approvedCount", approvedRequests.size());
        model.addAttribute("rejectedCount", rejectedRequests.size());
        model.addAttribute("users", filteredUsers);
        model.addAttribute("departments", allDepartments);
        model.addAttribute("totalUsers", filteredUsers.size());
        model.addAttribute("remainingLeaveDays", remainingLeaveDays);
        model.addAttribute("selectedDepartment", department);

        return "admin_dashboard";
    }

    @PostMapping("/approve/{id}")
    public String approveLeaveRequest(@PathVariable Long id) {
        LeaveRequest leaveRequest = leaveRequestService.approveLeaveRequest(id);
        User user = leaveRequest.getUser();
        Map<String, Integer> remainingLeaveDays = leaveRequestService.getRemainingLeaveDays(user);

        try {
            emailService.sendLeaveRequestApprovalNotification(leaveRequest, remainingLeaveDays);
        } catch (MessagingException e) {

            System.err.println("Failed to send approval email: " + e.getMessage());

        }

        return "redirect:/admin/dashboard";
    }

    @PostMapping("/reject/{id}")
    public String rejectLeaveRequest(@PathVariable Long id) {
        LeaveRequest leaveRequest = leaveRequestService.rejectLeaveRequest(id);

        try {
            emailService.sendLeaveRequestRejectionNotification(leaveRequest);
        } catch (MessagingException e) {
            System.err.println("Failed to send rejection email: " + e.getMessage());
        }

        return "redirect:/admin/dashboard";
    }



    @GetMapping("/users")
    public String listUsers(Model model) {
        List<User> users = userService.getAllUsers();
        List<Department> departments = departmentService.getAllDepartments();
        model.addAttribute("users", users);
        model.addAttribute("departments", departments);
        model.addAttribute("totalUsers", users.size());
        return "admin/users";
    }

    @GetMapping("/departments")
    public String listDepartments(Model model) {
        List<Department> departments = departmentService.getAllDepartments();
        model.addAttribute("departments", departments);
        return "admin/dashboard";
    }

    @PostMapping("/departments/create")
    @ResponseBody
    public ResponseEntity<?> createDepartment(@RequestParam String name) {
        try {
            Department department = departmentService.createDepartment(name);
            return ResponseEntity.ok(department);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Failed to create department: " + e.getMessage());
        }
    }

    @PostMapping("/users/{id}/update")
    public String updateUser(@PathVariable Long id, @RequestParam String department, @RequestParam String role) {
        User user = userService.findById(id);
        user.setDepartment(department);
        user.setRole(role);
        userService.updateUser(user);
        return "redirect:/admin/users";
    }
    @GetMapping("/users-by-department")
    public String listUsersByDepartment(Model model) {
        Map<String, List<User>> usersByDepartment = userService.getUsersByDepartment();
        model.addAttribute("usersByDepartment", usersByDepartment);
        model.addAttribute("departments", departmentService.getAllDepartments());
        return "admin/users_by_department";
    }

    @PostMapping("/users/{id}/change-department")
    public String changeUserDepartment(@PathVariable Long id, @RequestParam String department) {
        userService.changeUserDepartment(id, department);
        return "redirect:/admin/users-by-department";
    }

    @GetMapping("/all-leave-requests")
    public String listAllLeaveRequests(Model model) {
        List<LeaveRequest> allLeaveRequests = leaveRequestService.getAllLeaveRequests();
        model.addAttribute("leaveRequests", allLeaveRequests);
        return "admin/all_leave_requests";
    }
    @PostMapping("/users/{id}/add-leave")
    public String addLeaveForUser(@PathVariable Long id, @RequestParam String leaveType, @RequestParam int days) {
        User user = userService.findById(id);
        leaveRequestService.addExtraLeaveForUser(user, leaveType, days);
        return "redirect:/admin/dashboard";
    }
    @PostMapping("/users/{id}/delete")
    public String deleteUser(@PathVariable Long id) {
        userService.deleteUserById(id);
        return "redirect:/admin/dashboard";
    }


    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @DeleteMapping("/departments/{id}/delete")
    @ResponseBody
    public ResponseEntity<?> deleteDepartment(@PathVariable Long id) {
        try {
            departmentService.deleteDepartmentById(id);
            return ResponseEntity.ok().body("Department deleted successfully");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Failed to delete department: " + e.getMessage());
        }
    }



    @GetMapping("/users/{id}/remaining-leave")
    @ResponseBody
    public Map<String, Integer> getRemainingLeave(@PathVariable Long id) {
        User user = userService.findById(id);
        return leaveRequestService.getRemainingLeaveDays(user);
    }
}