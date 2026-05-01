package com.promo.otp.security;

import com.auth0.jwt.interfaces.DecodedJWT;
import com.promo.otp.dao.UserDao;
import com.promo.otp.model.User;

import java.sql.SQLException;

public class AuthFilter {
    private final UserDao userDao = new UserDao();

    public boolean isAuthenticated(String token) {
        if (token == null || token.isEmpty()) {
            return false;
        }

        // Убираем "Bearer " если есть
        if (token.startsWith("Bearer ")) {
            token = token.substring(7);
        }

        try {
            DecodedJWT jwt = JwtUtil.verify(token);
            return jwt.getExpiresAt().getTime() > System.currentTimeMillis();
        } catch (Exception e) {
            return false;
        }
    }

    public boolean isAdmin(String token) {
        if (!isAuthenticated(token)) {
            return false;
        }

        if (token.startsWith("Bearer ")) {
            token = token.substring(7);
        }

        try {
            String role = JwtUtil.getRoleFromToken(token);
            return "ADMIN".equals(role);
        } catch (Exception e) {
            return false;
        }
    }

    public String getLoginFromToken(String token) {
        if (token.startsWith("Bearer ")) {
            token = token.substring(7);
        }

        try {
            return JwtUtil.getLoginFromToken(token);
        } catch (Exception e) {
            return null;
        }
    }

    public int getUserIdFromToken(String token) throws SQLException {
        String login = getLoginFromToken(token);
        if (login == null) {
            throw new RuntimeException("Invalid token");
        }

        User user = userDao.findByLogin(login);
        if (user == null) {
            throw new RuntimeException("User not found");
        }

        return user.getId();
    }
}