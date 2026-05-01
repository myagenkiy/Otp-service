package com.promo.otp.controller;

import com.promo.otp.security.AuthFilter;
import com.promo.otp.service.OtpService;
import com.promo.otp.utils.JsonUtil;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.IOException;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class UserController implements HttpHandler {
    private final OtpService otpService = new OtpService();
    private final AuthFilter authFilter = new AuthFilter();

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        // Проверяем авторизацию
        String token = exchange.getRequestHeaders().getFirst("Authorization");
        if (token == null || !authFilter.isAuthenticated(token)) {
            sendResponse(exchange, 401, "Unauthorized");
            return;
        }

        // Проверяем, что пользователь не админ (хотя админ тоже может иметь доступ к своим операциям)
        if (authFilter.isAdmin(token)) {
            sendResponse(exchange, 403, "Forbidden: User API is for regular users only");
            return;
        }

        String path = exchange.getRequestURI().getPath();
        String method = exchange.getRequestMethod();

        try {
            if (path.equals("/api/user/generate") && method.equals("POST")) {
                handleGenerateCode(exchange, token);
            } else if (path.equals("/api/user/validate") && method.equals("POST")) {
                handleValidateCode(exchange);
            } else {
                sendResponse(exchange, 404, "Not found");
            }
        } catch (Exception e) {
            Map<String, String> error = Map.of("error", e.getMessage());
            sendResponse(exchange, 500, JsonUtil.toJson(error));
        }
    }

    private void handleGenerateCode(HttpExchange exchange, String token) throws IOException {
        String body = new String(exchange.getRequestBody().readAllBytes());
        Map<String, String> request = JsonUtil.fromJson(body, Map.class);

        // Если operationId не передан, генерируем сами
        String operationId = request.getOrDefault("operationId", UUID.randomUUID().toString());

        try {
            int userId = authFilter.getUserIdFromToken(token);
            String code = otpService.generateCode(userId, operationId);

            Map<String, Object> response = new HashMap<>();
            response.put("message", "Code generated and sent successfully");
            response.put("operationId", operationId);
            response.put("code", code); // В реальном приложении не возвращайте код! Только для тестирования
            response.put("note", "Code has been sent via SMS, Email, Telegram and saved to file");

            sendResponse(exchange, 200, JsonUtil.toJson(response));
        } catch (Exception e) {
            Map<String, String> error = Map.of("error", e.getMessage());
            sendResponse(exchange, 400, JsonUtil.toJson(error));
        }
    }

    private void handleValidateCode(HttpExchange exchange) throws IOException {
        String body = new String(exchange.getRequestBody().readAllBytes());
        Map<String, String> request = JsonUtil.fromJson(body, Map.class);

        String operationId = request.get("operationId");
        String code = request.get("code");

        if (operationId == null || code == null) {
            Map<String, String> error = Map.of("error", "operationId and code are required");
            sendResponse(exchange, 400, JsonUtil.toJson(error));
            return;
        }

        try {
            boolean isValid = otpService.validateCode(operationId, code);

            Map<String, Object> response = new HashMap<>();
            if (isValid) {
                response.put("success", true);
                response.put("message", "Code is valid. Operation confirmed.");
            } else {
                response.put("success", false);
                response.put("message", "Invalid or expired code");
            }

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