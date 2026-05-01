package com.promo.otp.controller;

import com.promo.otp.service.AuthService;
import com.promo.otp.utils.JsonUtil;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.IOException;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;

public class AuthController implements HttpHandler {
    private final AuthService authService = new AuthService();

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        String path = exchange.getRequestURI().getPath();
        String method = exchange.getRequestMethod();

        try {
            if (path.equals("/api/register") && method.equals("POST")) {
                handleRegister(exchange);
            } else if (path.equals("/api/login") && method.equals("POST")) {
                handleLogin(exchange);
            } else {
                sendResponse(exchange, 404, "Not found");
            }
        } catch (Exception e) {
            Map<String, String> error = Map.of("error", e.getMessage());
            sendResponse(exchange, 500, JsonUtil.toJson(error));
        }
    }

    private void handleRegister(HttpExchange exchange) throws IOException {
        String body = new String(exchange.getRequestBody().readAllBytes());
        Map<String, String> request = JsonUtil.fromJson(body, Map.class);

        String login = request.get("login");
        String password = request.get("password");
        String role = request.getOrDefault("role", "USER");

        try {
            var user = authService.register(login, password, role);
            Map<String, Object> response = new HashMap<>();
            response.put("message", "User registered successfully");
            response.put("userId", user.getId());
            response.put("login", user.getLogin());
            response.put("role", user.getRole());

            sendResponse(exchange, 201, JsonUtil.toJson(response));
        } catch (Exception e) {
            Map<String, String> error = Map.of("error", e.getMessage());
            sendResponse(exchange, 400, JsonUtil.toJson(error));
        }
    }

    private void handleLogin(HttpExchange exchange) throws IOException {
        String body = new String(exchange.getRequestBody().readAllBytes());
        Map<String, String> request = JsonUtil.fromJson(body, Map.class);

        String login = request.get("login");
        String password = request.get("password");

        try {
            String token = authService.login(login, password);
            Map<String, String> response = Map.of("token", token);
            sendResponse(exchange, 200, JsonUtil.toJson(response));
        } catch (Exception e) {
            Map<String, String> error = Map.of("error", e.getMessage());
            sendResponse(exchange, 401, JsonUtil.toJson(error));
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