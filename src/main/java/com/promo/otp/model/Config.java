package com.promo.otp.model;

public class Config {
    private int id;
    private int codeLifetimeSeconds;
    private int codeLength;

    public Config() {
        this.codeLifetimeSeconds = 300; // 5 минут по умолчанию
        this.codeLength = 6; // 6 цифр по умолчанию
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getCodeLifetimeSeconds() {
        return codeLifetimeSeconds;
    }

    public void setCodeLifetimeSeconds(int codeLifetimeSeconds) {
        this.codeLifetimeSeconds = codeLifetimeSeconds;
    }

    public int getCodeLength() {
        return codeLength;
    }

    public void setCodeLength(int codeLength) {
        this.codeLength = codeLength;
    }
}