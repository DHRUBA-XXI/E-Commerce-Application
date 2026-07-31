package com.DhrubaStudio.E_commercePlatform.notifications;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.scheduling.annotation.Async;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;

import java.math.BigDecimal;

@Slf4j
@Service
public class EmailService {

    private JavaMailSender mailSender;

    public EmailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    @Async
    public void sendOrderConfirmationMail(String toEmail, Long orderId, BigDecimal totalAmount, String shippingAddress) {
        try{
        SimpleMailMessage message = new SimpleMailMessage();

        String mailContent = "Thank you for the order! Your total amount: Rs " + totalAmount +
                "\nShipping address: " + shippingAddress +
                "\nKeep shopping with Maitra Store!";

        message.setFrom(System.getProperty("mail.smtp.host"));
        message.setTo(toEmail);
        message.setSubject("Order Confirmation for #" + orderId);
        message.setText(mailContent);
        mailSender.send(message);

        }catch(Exception e){
            log.error("Exception occurred while sending Order confirmation email for Order ID-{} : ", orderId, e);
        }
    }
}
