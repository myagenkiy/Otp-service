package com.promo.otp.service;

public class SmsSenderMock implements ChannelSender {
    @Override
    public void send(String phone, String code) {
        System.out.println("💬 [SMS] To: " + phone + " Code: " + code);
    }
}