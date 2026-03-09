package lk.ijse.etecmanagementsystem.emailservice;

import javafx.scene.control.Alert;
import lk.ijse.etecmanagementsystem.util.ETecAlerts;

import java.util.Properties;
import javax.mail.*;
import javax.mail.internet.*;

public class EmailService {

    // Your admin email credentials
    private static final String FROM_EMAIL = "eteccomputers38@gmail.com";
    private static final String PASSWORD = "lwip jusn uxdm rajv"; // Use App Password, not login password


    public static boolean sendUserPasswordToEmail(String recipientEmail, String password) {
        // 1. Setup Mail Server Properties
        Properties props = new Properties();
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.host", "smtp.gmail.com");
        props.put("mail.smtp.port", "587");

        // 2. Create Session
        Session session = Session.getInstance(props, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(FROM_EMAIL, PASSWORD);
            }
        });

        // 3. Draft the Message
        try {
            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(FROM_EMAIL));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(recipientEmail));
            message.setSubject("ETec Management System - Password Request");

            String htmlContent = "<h3>Password Request</h3>"
                    + "<p>Use the Following Password for Login to System :</p>"
                    + "<h2>" + password + "</h2>"
                    + "<p>If you did not request this, please ignore this email.</p>";

            message.setContent(htmlContent, "text/html");

            // 4. Send Email
            Transport.send(message);
            System.out.println("Email sent successfully!");
            return true;

        } catch (MessagingException e) {
            ETecAlerts.showAlert(Alert.AlertType.ERROR, "Email Error", "Failed to send email to " + recipientEmail);
            e.printStackTrace();
            return false;
        }
    }
}