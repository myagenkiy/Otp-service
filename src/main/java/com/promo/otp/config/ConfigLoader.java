package com.promo.otp.config;

import io.github.cdimascio.dotenv.Dotenv;

public class ConfigLoader {
    private static final Dotenv dotenv = Dotenv.configure()
            .ignoreIfMissing()
            .load();

    public static String getDbUrl() {
        return String.format("jdbc:postgresql://%s:%s/%s",
                get("DB_HOST", "localhost"),
                get("DB_PORT", "5432"),
                get("DB_NAME", "otp_db"));
    }

    public static String getDbUsername() {
        return get("DB_USERNAME", "postgres");
    }

    public static String getDbPassword() {
        return get("DB_PASSWORD", "");
    }

    public static String getJwtSecret() {
        return get("JWT_SECRET", "mySuperSecretKeyForJWT2024");
    }

    public static int getJwtExpirationHours() {
        return Integer.parseInt(get("JWT_EXPIRATION_HOURS", "1"));
    }

    public static String getTelegramBotToken() {
        return get("TELEGRAM_BOT_TOKEN", "");
    }

    public static String getTelegramChatId() {
        return get("TELEGRAM_CHAT_ID", "");
    }

    public static String getEmailUsername() {
        return get("EMAIL_USERNAME", "");
    }

    public static String getEmailPassword() {
        return get("EMAIL_PASSWORD", "");
    }

    public static int getOtpCodeLength() {
        return Integer.parseInt(get("OTP_CODE_LENGTH", "6"));
    }

    public static int getOtpLifetimeSeconds() {
        return Integer.parseInt(get("OTP_LIFETIME_SECONDS", "300"));
    }

    public static int getServerPort() {
        return Integer.parseInt(get("SERVER_PORT", "8080"));
    }

    private static String get(String key, String defaultValue) {
        String value = dotenv.get(key);
        return (value != null && !value.isEmpty()) ? value : defaultValue;
    }
}