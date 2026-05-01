package com.promo.otp.service;

import com.promo.otp.dao.OtpDao;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class ExpiredCodesCleaner {
    private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
    private final OtpDao otpDao = new OtpDao();

    public void start() {
        scheduler.scheduleAtFixedRate(() -> {
            try {
                int updated = otpDao.expireOldCodes();
                if (updated > 0) {
                    System.out.println("🕐 Marked " + updated + " codes as EXPIRED");
                }
            } catch (Exception e) {
                System.err.println("❌ Error expiring codes: " + e.getMessage());
            }
        }, 0, 1, TimeUnit.MINUTES); // Проверяем каждую минуту
    }

    public void stop() {
        scheduler.shutdown();
    }
}