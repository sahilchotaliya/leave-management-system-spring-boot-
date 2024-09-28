package com.leave_management_system.service;

import com.leave_management_system.model.LeaveRequest;
import com.leave_management_system.model.User;
import com.leave_management_system.repository.LeaveRequestRepository;
import com.leave_management_system.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class LeaveRequestServiceImpl implements LeaveRequestService {
    @Autowired
    private LeaveRequestRepository leaveRequestRepository;
    @Autowired
    private UserService userService;
    @Autowired
    private UserRepository userRepository;

    @Override
    public void createLeaveRequest(LeaveRequest leaveRequest) {
        User user = leaveRequest.getUser();
        String leaveType = leaveRequest.getLeaveType();
        if (canRequestLeave(user, leaveType)) {
            int requestedDays = (int) ChronoUnit.DAYS.between(leaveRequest.getStartDate(), leaveRequest.getEndDate()) + 1;
            Map<String, Integer> remainingDays = getRemainingLeaveDays(user);
            if (requestedDays <= remainingDays.get(leaveType)) {
                leaveRequest.setStatus("PENDING");
                leaveRequestRepository.save(leaveRequest);
            } else {
                throw new RuntimeException("Requested leave days exceed remaining leave days for " + leaveType);
            }
        } else {
            throw new RuntimeException("Cannot create leave request. No remaining " + leaveType + " days.");
        }
    }

    @Override
    public List<LeaveRequest> getLeaveRequestsByUser(User user) {
        return leaveRequestRepository.findByUser(user);
    }

    @Override
    public List<LeaveRequest> getPendingLeaveRequests() {
        return leaveRequestRepository.findByStatus("PENDING");
    }

    @Override
    public LeaveRequest approveLeaveRequest(Long id) {
        LeaveRequest request = leaveRequestRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Leave request not found"));
        request.setStatus("APPROVED");

        // Calculate the number of days
        int days = (int) ChronoUnit.DAYS.between(request.getStartDate(), request.getEndDate()) + 1;

        // Deduct the leave days
        deductLeavedays(request.getUser(), request.getLeaveType(), days);

        return leaveRequestRepository.save(request);
    }


    @Override
    public LeaveRequest rejectLeaveRequest(Long id) {
        LeaveRequest request = leaveRequestRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Leave request not found"));
        request.setStatus("REJECTED");
        return leaveRequestRepository.save(request);
    }

    public Map<String, Integer> getRemainingLeaveDays(User user) {
        Map<String, Integer> remainingDays = new HashMap<>();
        remainingDays.put("Annual", user.getAnnualLeaveDays());
        remainingDays.put("Sick", user.getSickLeaveDays());
        remainingDays.put("Personal", user.getPersonalLeaveDays());
        return remainingDays;
    }

    @Override
    public List<LeaveRequest> getApprovedLeaveRequests() {
        return leaveRequestRepository.findByStatus("APPROVED");
    }

    @Override
    public List<LeaveRequest> getRejectedLeaveRequests() {
        return leaveRequestRepository.findByStatus("REJECTED");
    }

    @Override
    public List<LeaveRequest> getAllLeaveRequests() {
        return leaveRequestRepository.findAll();
    }

    @Override
    public Map<String, Integer> getTotalLeaveUsed(User user) {
        Map<String, Integer> usedDays = new HashMap<>();
        usedDays.put("Annual", 0);
        usedDays.put("Sick", 0);
        usedDays.put("Personal", 0);

        List<LeaveRequest> approvedRequests = leaveRequestRepository.findByUserAndStatus(user, "APPROVED");
        for (LeaveRequest request : approvedRequests) {
            int days = (int) ChronoUnit.DAYS.between(request.getStartDate(), request.getEndDate()) + 1;
            usedDays.put(request.getLeaveType(), usedDays.get(request.getLeaveType()) + days);
        }

        return usedDays;
    }

    @Override
    public boolean canRequestLeave(User user, String leaveType) {
        Map<String, Integer> remainingDays = getRemainingLeaveDays(user);
        return remainingDays.get(leaveType) > 0;
    }

    public void addExtraLeaveForUser(User user, String leaveType, int days) {
        switch (leaveType) {
            case "Annual":
                user.setAnnualLeaveDays(user.getAnnualLeaveDays() + days);
                break;
            case "Sick":
                user.setSickLeaveDays(user.getSickLeaveDays() + days);
                break;
            case "Personal":
                user.setPersonalLeaveDays(user.getPersonalLeaveDays() + days);
                break;
            default:
                throw new IllegalArgumentException("Invalid leave type");
        }
        userService.updateUser(user);
    }

    @Override
    public void deductLeavedays(User user, String leaveType, int days) {
        switch (leaveType) {
            case "Annual":
                user.setAnnualLeaveDays(Math.max(0, user.getAnnualLeaveDays() - days));
                break;
            case "Sick":
                user.setSickLeaveDays(Math.max(0, user.getSickLeaveDays() - days));
                break;
            case "Personal":
                user.setPersonalLeaveDays(Math.max(0, user.getPersonalLeaveDays() - days));
                break;
            default:
                throw new IllegalArgumentException("Invalid leave type");
        }
        userService.updateUser(user);
    }



}