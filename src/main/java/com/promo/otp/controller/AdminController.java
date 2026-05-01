package com.promo.otp.controller;

import com.promo.otp.dao.ConfigDao;
import com.promo.otp.dao.OtpDao;
import com.promo.otp.dao.UserDao;
import com.promo.otp.model.Config;
import com.promo.otp.model.User;
import com.promo.otp.security.AuthFilter;
import com.promo.otp.utils.JsonUtil;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.IOException;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AdminController implements HttpHandler {
    private final UserDao userDao = new UserDao();
    private final ConfigDao configDao = new ConfigDao();
    private final OtpDao otpDao = new OtpDao();
    private final AuthFilter authFilter = new AuthFilter();

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        // Проверяем права доступа (только ADMIN)
        String token = exchange.getRequestHeaders().getFirst("Authorization");
        if (token == null || !authFilter.isAdmin(token)) {
            sendResponse(exchange, 403, "Forbidden: Admin access required");
            return;
        }

        String path = exchange.getRequestURI().getPath();
        String method = exchange.getRequestMethod();

        try {
            if (path.equals("/api/admin/config") && method.equals("PUT")) {
                handleUpdateConfig(exchange);
            } else if (path.equals("/api/admin/users") && method.equals("GET")) {
                handleGetUsers(exchange);
            } else if (path.equals("/api/admin/users/delete") && method.equals("DELETE")) {
                handleDeleteUser(exchange);
            } else {
                sendResponse(exchange, 404, "Not found");
            }
        } catch (Exception e) {
            Map<String, String> error = Map.of("error", e.getMessage());
            sendResponse(exchange, 500, JsonUtil.toJson(error));
        }
    }

    private void handleUpdateConfig(HttpExchange exchange) throws IOException {
        String body = new String(exchange.getRequestBody().readAllBytes());
        Map<String, Object> request = JsonUtil.fromJson(body, Map.class);

        try {
            int lifetime = ((Number) request.get("lifetimeSeconds")).intValue();
            int length = ((Number) request.get("codeLength")).intValue();

            if (lifetime < 30 || lifetime > 3600) {
                sendResponse(exchange, 400, "Lifetime must be between 30 and 3600 seconds");
                return;
            }

            if (length < 4 || length > 10) {
                sendResponse(exchange, 400, "Code length must be between 4 and 10");
                return;
            }

            Config newConfig = configDao.updateConfig(lifetime, length);

            Map<String, Object> response = new HashMap<>();
            response.put("message", "Config updated successfully");
            response.put("lifetimeSeconds", newConfig.getCodeLifetimeSeconds());
            response.put("codeLength", newConfig.getCodeLength());

            sendResponse(exchange, 200, JsonUtil.toJson(response));
        } catch (Exception e) {
            Map<String, String> error = Map.of("error", e.getMessage());
            sendResponse(exchange, 400, JsonUtil.toJson(error));
        }
    }

    private void handleGetUsers(HttpExchange exchange) throws IOException {
        try {
            List<User> users = userDao.findAllNonAdmins();

            // Маскируем пароли
            List<Map<String, Object>> safeUsers = users.stream()
                    .map(u -> {
                        Map<String, Object> safe = new HashMap<>();
                        safe.put("id", u.getId());
                        safe.put("login", u.getLogin());
                        safe.put("role", u.getRole());
                        return safe;
                    })
                    .toList();

            Map<String, Object> response = new HashMap<>();
            response.put("users", safeUsers);
            response.put("count", safeUsers.size());

            sendResponse(exchange, 200, JsonUtil.toJson(response));
        } catch (Exception e) {
            Map<String, String> error = Map.of("error", e.getMessage());
            sendResponse(exchange, 500, JsonUtil.toJson(error));
        }
    }

    private void handleDeleteUser(HttpExchange exchange) throws IOException {
        String body = new String(exchange.getRequestBody().readAllBytes());
        Map<String, Object> request = JsonUtil.fromJson(body, Map.class);

        try {
            int userId = ((Number) request.get("userId")).intValue();

            // Проверяем, не админ ли это
            User user = userDao.findById(userId);
            if (user == null) {
                sendResponse(exchange, 404, "User not found");
                return;
            }

            if (user.getRole().equals("ADMIN")) {
                sendResponse(exchange, 403, "Cannot delete admin user");
                return;
            }

            // Удаляем все OTP коды пользователя
            otpDao.deleteByUserId(userId);

            // Удаляем пользователя
            userDao.deleteById(userId);

            Map<String, String> response = Map.of("message", "User deleted successfully");
            sendResponse(exchange, 200, JsonUtil.toJson(response));
        } catch (Exception e) {
            Map<String, String> error = Map.of("error", e.getMessage());
            sendResponse(exchange, 500, JsonUtil.toJson(error));
        }
    }

    private void sendResponse(HttpExchange exchange, int statusCode, String response) throws IOException {
        exchange.getResponseHeaders().set("Content-Type", "application/json");
        exchange.sendResponseHeaders(statusCode, response.getBytes().length);
        OutputStream os = exchange.getResponseBody();
        os.write(response.getBytes());
        os.close();
    }
}