package com.promo.otp.service;

import com.promo.otp.config.ConfigLoader;
import com.promo.otp.dao.ConfigDao;
import com.promo.otp.dao.OtpDao;
import com.promo.otp.dao.UserDao;
import com.promo.otp.model.Config;
import com.promo.otp.model.OtpCode;
import com.promo.otp.model.User;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class OtpService {
    private final OtpDao otpDao = new OtpDao();
    private final ConfigDao configDao = new ConfigDao();
    private final UserDao userDao = new UserDao();
    private final SecureRandom random = new SecureRandom();
    private final List<ChannelSender> senders = new ArrayList<>();

    public OtpService() {
        senders.add(new SmsSenderMock());
        senders.add(new EmailSender());
        senders.add(new TelegramSender());
        senders.add(new FileSender());
    }

    public String generateCode(int userId, String operationId) throws Exception {
        User user = userDao.findById(userId);
        if (user == null) throw new Exception("User not found");

        Config config = configDao.getConfig();
        String code = generateRandomCode(config.getCodeLength());
        LocalDateTime expiresAt = LocalDateTime.now().plusSeconds(config.getCodeLifetimeSeconds());

        OtpCode otpCode = new OtpCode(userId, operationId, code, expiresAt);
        otpDao.save(otpCode);

        String destination = user.getLogin();
        String telegramChatId = ConfigLoader.getTelegramChatId();

        for (ChannelSender sender : senders) {
            try {
                if (sender instanceof TelegramSender) {
                    if (telegramChatId != null && !telegramChatId.isEmpty()) {
                        ((TelegramSender) sender).send(telegramChatId, code);
                    }
                } else {
                    sender.send(destination, code);
                }
            } catch (Exception e) {
                System.err.println("Failed: " + e.getMessage());
            }
        }
        return code;
    }

    public boolean validateCode(String operationId, String code) throws Exception {
        if (!otpDao.isCodeValid(operationId, code)) return false;
        otpDao.updateStatusByOperationIdAndCode(operationId, code, "USED");
        return true;
    }

    private String generateRandomCode(int length) {
        StringBuilder code = new StringBuilder();
        for (int i = 0; i < length; i++) code.append(random.nextInt(10));
        return code.toString();
    }
}