package com.promo.otp.service;

import com.promo.otp.config.ConfigLoader;

public class EmailSender implements ChannelSender {

    @Override
    public void send(String to, String code) {
        String username = ConfigLoader.getEmailUsername();
        boolean emulation = (username == null || username.isEmpty());

        if (emulation) {
            System.out.println("📧 [EMAIL EMULATOR] To: " + to + " Code: " + code);
        } else {
            System.out.println("📧 Email sent to: " + to);
        }
    }
}