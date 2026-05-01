package com.promo.otp.service;

import com.promo.otp.dao.UserDao;
import com.promo.otp.model.User;
import com.promo.otp.security.JwtUtil;
import com.promo.otp.security.PasswordUtil;

import java.sql.SQLException;

public class AuthService {
    private final UserDao userDao = new UserDao();

    public User register(String login, String password, String role) throws Exception {
        // Валидация
        if (login == null || login.trim().isEmpty()) {
            throw new Exception("Login cannot be empty");
        }

        if (password == null || password.length() < 4) {
            throw new Exception("Password must be at least 4 characters");
        }

        if (!role.equals("ADMIN") && !role.equals("USER")) {
            throw new Exception("Role must be ADMIN or USER");
        }

        // Проверка, существует ли пользователь
        User existing = userDao.findByLogin(login);
        if (existing != null) {
            throw new Exception("User with this login already exists");
        }

        // Если пытаются создать админа, проверяем, нет ли уже админа
        if (role.equals("ADMIN")) {
            if (userDao.existsAdmin()) {
                throw new Exception("Admin already exists. Cannot create second admin.");
            }
        }

        // Хэшируем пароль и создаем пользователя
        String hashedPassword = PasswordUtil.hash(password);
        User newUser = new User(login, hashedPassword, role);

        return userDao.create(newUser);
    }

    public String login(String login, String password) throws Exception {
        // Ищем пользователя
        User user = userDao.findByLogin(login);
        if (user == null) {
            throw new Exception("Invalid login or password");
        }

        // Проверяем пароль
        if (!PasswordUtil.verify(password, user.getPasswordHash())) {
            throw new Exception("Invalid login or password");
        }

        // Генерируем JWT токен
        return JwtUtil.generate(user.getLogin(), user.getRole());
    }
}