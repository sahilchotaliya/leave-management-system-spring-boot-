package com.leave_management_system.service;

import com.leave_management_system.model.LeaveRequest;
import com.leave_management_system.model.User;

import java.util.List;
import java.util.Map;

public interface LeaveRequestService {
    void createLeaveRequest(LeaveRequest leaveRequest);
    List<LeaveRequest> getLeaveRequestsByUser(User user);
    List<LeaveRequest> getPendingLeaveRequests();
    LeaveRequest approveLeaveRequest(Long id);
    LeaveRequest rejectLeaveRequest(Long id);
    Map<String, Integer> getRemainingLeaveDays(User user);
    List<LeaveRequest> getApprovedLeaveRequests();
    List<LeaveRequest> getRejectedLeaveRequests();
    List<LeaveRequest> getAllLeaveRequests();
    Map<String, Integer> getTotalLeaveUsed(User user);
    boolean canRequestLeave(User user, String leaveType);
    void addExtraLeaveForUser(User user, String leaveType, int days);
    void deductLeavedays(User user, String leaveType, int days);

}