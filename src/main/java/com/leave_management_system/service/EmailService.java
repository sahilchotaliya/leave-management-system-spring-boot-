package com.leave_management_system.service;

import com.leave_management_system.model.LeaveRequest;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;


import java.util.Map;

@Service
public class EmailService {
    @Autowired
    private JavaMailSender mailSender;

    // Send notification for a new leave request (Plain Text)
    public void sendNewLeaveRequestNotification(LeaveRequest leaveRequest) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo("proa77259@gmail.com");
        message.setSubject("New Leave Request");
        message.setText("A new leave request has been submitted by " + leaveRequest.getUser().getUsername());
        mailSender.send(message);
    }

    // Send leave request approval notification (HTML Email)
    public void sendLeaveRequestApprovalNotification(LeaveRequest leaveRequest, Map<String, Integer> remainingLeaveDays) throws MessagingException {
        MimeMessage mimeMessage = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, "UTF-8");

        helper.setTo(leaveRequest.getUser().getEmail());
        helper.setSubject("Leave Request Approved");

        String emailContent = createHtmlEmailContent(leaveRequest, remainingLeaveDays);
        helper.setText(emailContent, true);  // Set HTML content to true

        mailSender.send(mimeMessage);
    }

    // HTML content generation for leave request approval
    private String createHtmlEmailContent(LeaveRequest leaveRequest, Map<String, Integer> remainingLeaveDays) {
        StringBuilder rows = new StringBuilder();

        for (Map.Entry<String, Integer> entry : remainingLeaveDays.entrySet()) {
            rows.append("<tr>")
                    .append("<td>").append(entry.getKey()).append("</td>")
                    .append("<td>").append(entry.getValue()).append("</td>")
                    .append("</tr>");
        }

        // Return HTML content with dynamic values
        return "<!DOCTYPE html>" +
                "<html lang=\"en\">" +
                "<head>" +
                "    <meta charset=\"UTF-8\">" +
                "    <meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">" +
                "    <title>Leave Request Approved</title>" +
                "    <style>" +
                "        body { font-family: Arial, sans-serif; line-height: 1.6; color: #333; }" +
                "        .container { width: 100%; max-width: 600px; margin: 0 auto; padding: 20px; }" +
                "        .header { background-color: #4CAF50; color: white; text-align: center; padding: 10px; }" +
                "        .content { background-color: #f9f9f9; border: 1px solid #ddd; padding: 20px; margin-top: 20px; }" +
                "        table { width: 100%; border-collapse: collapse; margin-top: 20px; }" +
                "        th, td { border: 1px solid #ddd; padding: 8px; text-align: left; }" +
                "        th { background-color: #4CAF50; color: white; }" +
                "    </style>" +
                "</head>" +
                "<body>" +
                "<div class=\"container\">" +
                "    <div class=\"header\">" +
                "        <h1>Leave Request Approved</h1>" +
                "    </div>" +
                "    <div class=\"content\">" +
                "        <p>Dear <h2> " + leaveRequest.getUser().getUsername() + " <h2>,</p>" +
                "        <p>Your leave request from " + leaveRequest.getStartDate() + " to " + leaveRequest.getEndDate() + " has been approved.</p>" +
                "        <h2>Remaining Leave Days:</h2>" +
                "        <table>" +
                "            <tr>" +
                "                <th>Leave Type</th>" +
                "                <th>Remaining Days</th>" +
                "            </tr>" +
                rows +
                "        </table>" +
                "        <p>Best regards,<br>HR Department</p>" +
                "    </div>" +
                "</div>" +
                "</body>" +
                "</html>";
    }

    // Send leave request rejection notification (Plain Text)
    // Send leave request rejection notification (HTML Email)
    public void sendLeaveRequestRejectionNotification(LeaveRequest leaveRequest) throws MessagingException {
        MimeMessage mimeMessage = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, "UTF-8");

        helper.setTo(leaveRequest.getUser().getEmail());
        helper.setSubject("Leave Request Rejected");

        String emailContent = createHtmlRejectionEmailContent(leaveRequest);
        helper.setText(emailContent, true);  // Set HTML content to true

        mailSender.send(mimeMessage);
    }

    // HTML content generation for leave request rejection
    private String createHtmlRejectionEmailContent(LeaveRequest leaveRequest) {
        // Return HTML content with dynamic values
        return "<!DOCTYPE html>" +
                "<html lang=\"en\">" +
                "<head>" +
                "    <meta charset=\"UTF-8\">" +
                "    <meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">" +
                "    <title>Leave Request Rejected</title>" +
                "    <style>" +
                "        body { font-family: Arial, sans-serif; line-height: 1.6; color: #333; }" +
                "        .container { width: 100%; max-width: 600px; margin: 0 auto; padding: 20px; }" +
                "        .header { background-color: #f44336; color: white; text-align: center; padding: 10px; }" +
                "        .content { background-color: #f9f9f9; border: 1px solid #ddd; padding: 20px; margin-top: 20px; }" +
                "    </style>" +
                "</head>" +
                "<body>" +
                "<div class=\"container\">" +
                "    <div class=\"header\">" +
                "        <h1>Leave Request Rejected</h1>" +
                "    </div>" +
                "    <div class=\"content\">" +
                "        <p>Dear " + leaveRequest.getUser().getUsername() + ",</p>" +
                "        <p>We regret to inform you that your leave request from " + leaveRequest.getStartDate() + " to " + leaveRequest.getEndDate() + " has been rejected.</p>" +
                "        <p>Please feel free to reach out if you have any questions.</p>" +
                "        <p>Best regards,<br>HR Department</p>" +
                "    </div>" +
                "</div>" +
                "</body>" +
                "</html>";
    }

}
