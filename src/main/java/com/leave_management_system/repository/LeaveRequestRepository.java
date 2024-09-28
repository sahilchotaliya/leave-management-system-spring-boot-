package com.leave_management_system.repository;

import com.leave_management_system.model.LeaveRequest;
import com.leave_management_system.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface LeaveRequestRepository extends JpaRepository<LeaveRequest, Long> {
    List<LeaveRequest> findByUser(User user);
    List<LeaveRequest> findByStatus(String status);
    List<LeaveRequest> findByUserAndStatus(User user,String status);


}