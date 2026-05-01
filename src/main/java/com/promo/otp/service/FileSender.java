package com.promo.otp.service;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class FileSender implements ChannelSender {
    private static final String FILE_PATH = "otp_codes.log";

    @Override
    public void send(String destination, String code) {
        try (PrintWriter writer = new PrintWriter(new FileWriter(FILE_PATH, true))) {
            String timestamp = LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
            writer.printf("[%s] User: %s, Code: %s%n", timestamp, destination, code);
            System.out.println("✅ Code saved to " + FILE_PATH);
        } catch (IOException e) {
            System.err.println("❌ Failed to save code to file: " + e.getMessage());
        }
    }
}