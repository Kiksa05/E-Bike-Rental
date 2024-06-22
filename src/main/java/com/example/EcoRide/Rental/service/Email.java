package com.example.EcoRide.Rental.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.InputStreamSource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayInputStream;
import java.io.*;


@Service
public class Email {

    private static final Logger logger = LoggerFactory.getLogger(Email.class);

    @Autowired
    private JavaMailSender mailSender;

    @Autowired
    private PDFGenerator pdfGeneratorService;

    public void sendRegistrationEmail(String to, String name) throws MessagingException, IOException {
        logger.info("Preparing registration email for {}", to);
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true);

        helper.setTo(to);
        helper.setSubject("Welcome to EcoRide Rentals");
        helper.setText("Dear " + name + ",\n\nThank you for registering on EcoRide Rentals.\nPlease make sure to read the Terms and Conditions.\n\nBest regards,\nEcoRide Rentals Team");

        // Generate PDF
        ByteArrayInputStream pdf = pdfGeneratorService.generateTermsAndConditionsPdf();

        // Add PDF attachment
        InputStreamSource pdfSource = new ByteArrayResource(pdf.readAllBytes());
        helper.addAttachment("TermsAndConditions.pdf", pdfSource);


        mailSender.send(message);
        logger.info("Email sent to {}", to);
    }
}
