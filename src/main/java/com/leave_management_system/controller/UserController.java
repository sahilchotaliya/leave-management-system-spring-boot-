package com.leave_management_system.controller;

import com.leave_management_system.model.LeaveRequest;
import com.leave_management_system.model.User;
import com.leave_management_system.service.DepartmentService;
import com.leave_management_system.service.LeaveRequestService;
import com.leave_management_system.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

@Controller
public class UserController {
    @Autowired
    private UserService userService;
    @Autowired
    private DepartmentService departmentService;
    @Autowired
    private LeaveRequestService leaveRequestService;

    @GetMapping("/register")
    public String showRegistrationForm(Model model) {
        model.addAttribute("user", new User());
        model.addAttribute("departments", departmentService.getAllDepartments());
        return "register";
    }

    @PostMapping("/register")
    public String registerUser(@ModelAttribute("user") User user) {
        userService.registerUser(user);
        return "redirect:/login";
    }

    @GetMapping("/login")
    public String showLoginForm() {
        return "login";
    }

    @GetMapping("/user/dashboard")
    public String dashboard(Model model, Authentication authentication) {
        User user = userService.findByUsername(authentication.getName());
        model.addAttribute("leaveRequests", leaveRequestService.getLeaveRequestsByUser(user));
        model.addAttribute("remainingLeaveDays", leaveRequestService.getRemainingLeaveDays(user));
        return "user_dashboard";
    }

    @PostMapping("/user/leave-request")
    public String submitLeaveRequest(@RequestParam String leaveType,
                                     @RequestParam String startDate,
                                     @RequestParam String endDate,
                                     @RequestParam String reason,
                                     Authentication authentication) {
        User user = userService.findByUsername(authentication.getName());
        LeaveRequest leaveRequest = new LeaveRequest();
        leaveRequest.setUser(user);
        leaveRequest.setLeaveType(leaveType);
        leaveRequest.setStartDate(LocalDate.parse(startDate));
        leaveRequest.setEndDate(LocalDate.parse(endDate));
        leaveRequest.setReason(reason);
        leaveRequestService.createLeaveRequest(leaveRequest);
        return "redirect:/user/dashboard";
    }
    @GetMapping("/user/remaining-leave")
    @ResponseBody
    public Map<String, Integer> getRemainingLeave(Authentication authentication) {
        User user = userService.findByUsername(authentication.getName());
        return leaveRequestService.getRemainingLeaveDays(user);
    }




}