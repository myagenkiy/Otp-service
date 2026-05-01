package com.promo.otp.service;

public interface ChannelSender {
    void send(String destination, String code);
}