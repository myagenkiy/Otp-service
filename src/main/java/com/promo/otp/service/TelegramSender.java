package com.promo.otp.service;

import com.promo.otp.config.ConfigLoader;
import okhttp3.*;

public class TelegramSender implements ChannelSender {

    private static final String BOT_TOKEN = ConfigLoader.getTelegramBotToken();
    private final OkHttpClient client = new OkHttpClient();

    @Override
    public void send(String chatId, String code) {
        if (BOT_TOKEN == null || BOT_TOKEN.isEmpty()) {
            System.out.println("⚠️ Telegram not configured");
            return;
        }

        String url = "https://api.telegram.org/bot" + BOT_TOKEN + "/sendMessage";
        String json = String.format("{\"chat_id\":\"%s\",\"text\":\"🔐 Your OTP code: %s\"}", chatId, code);

        RequestBody body = RequestBody.create(json, MediaType.parse("application/json"));
        Request request = new Request.Builder().url(url).post(body).build();

        try (Response response = client.newCall(request).execute()) {
            if (response.isSuccessful()) {
                System.out.println("✅ Telegram sent to: " + chatId);
            }
        } catch (Exception e) {
            System.err.println("❌ Telegram error: " + e.getMessage());
        }
    }
}