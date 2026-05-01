package com.promo.otp;

import com.promo.otp.config.ConfigLoader;
import com.promo.otp.controller.AdminController;
import com.promo.otp.controller.AuthController;
import com.promo.otp.controller.UserController;
import com.promo.otp.service.ExpiredCodesCleaner;
import com.sun.net.httpserver.HttpServer;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import java.net.URL;
import java.util.Scanner;
import java.util.concurrent.Executors;

public class Application {

    private static HttpServer server;
    private static String currentToken = null;
    private static String currentUser = null;
    private static final Scanner scanner = new Scanner(System.in);

    public static void main(String[] args) throws Exception {
        // Параметр запуска: server или menu
        String mode = args.length > 0 ? args[0] : "server";

        if (mode.equals("menu")) {
            // Запуск в режиме консольного меню
            runConsoleMenu();
        } else {
            // Запуск в режиме сервера
            runServer();
            // После запуска сервера показываем меню
            runConsoleMenu();
        }
    }

    private static void runServer() throws Exception {
        int port = ConfigLoader.getServerPort();

        System.out.println("═══════════════════════════════════════════════════════");
        System.out.println("🔧 OTP SERVICE CONFIGURATION");
        System.out.println("═══════════════════════════════════════════════════════");
        System.out.println("📡 Server port: " + port);
        System.out.println("🗄️  Database: " + ConfigLoader.getDbUrl());
        System.out.println("🤖 Telegram: " + (ConfigLoader.getTelegramBotToken().isEmpty() ? "❌ Not configured" : "✅ Configured"));
        System.out.println("═══════════════════════════════════════════════════════");

        server = HttpServer.create(new InetSocketAddress(port), 0);

        AuthController authController = new AuthController();
        AdminController adminController = new AdminController();
        UserController userController = new UserController();

        server.createContext("/api/register", authController);
        server.createContext("/api/login", authController);
        server.createContext("/api/admin", adminController);
        server.createContext("/api/user", userController);

        ExpiredCodesCleaner cleaner = new ExpiredCodesCleaner();
        cleaner.start();

        server.setExecutor(Executors.newFixedThreadPool(10));
        server.start();

        System.out.println("\n✅ OTP Service started successfully!");
        System.out.println("📍 http://localhost:" + port);
        System.out.println("═══════════════════════════════════════════════════════\n");
    }

    private static void runConsoleMenu() {
        System.out.println("═══════════════════════════════════════════════════════");
        System.out.println("              🤖 OTP SERVICE - КОНСОЛЬНОЕ МЕНЮ");
        System.out.println("═══════════════════════════════════════════════════════");

        while (true) {
            showMenu();
            int choice = getIntInput("👉 Выберите действие: ");

            switch (choice) {
                case 1: registerUser(); break;
                case 2: registerAdmin(); break;
                case 3: login(); break;
                case 4: generateCode(); break;
                case 5: validateCode(); break;
                case 6: listUsers(); break;
                case 7: updateConfig(); break;
                case 8: deleteUser(); break;
                case 9: showStatus(); break;
                case 0:
                    System.out.println("👋 До свидания!");
                    if (server != null) {
                        server.stop(0);
                    }
                    System.exit(0);
                    break;
                default: System.out.println("❌ Неверный выбор!");
            }
        }
    }

    private static void showMenu() {
        System.out.println("\n═══════════════════════════════════════════════════════");
        System.out.println("📋 ГЛАВНОЕ МЕНЮ");
        System.out.println("═══════════════════════════════════════════════════════");
        System.out.println("1️⃣  Регистрация пользователя");
        System.out.println("2️⃣  Регистрация администратора");
        System.out.println("3️⃣  Вход в систему");
        System.out.println("4️⃣  Сгенерировать OTP код");
        System.out.println("5️⃣  Проверить OTP код");
        System.out.println("6️⃣  [ADMIN] Список пользователей");
        System.out.println("7️⃣  [ADMIN] Обновить конфигурацию");
        System.out.println("8️⃣  [ADMIN] Удалить пользователя");
        System.out.println("9️⃣  Статус");
        System.out.println("0️⃣  Выход");
        System.out.println("═══════════════════════════════════════════════════════");

        if (currentToken != null) {
            System.out.println("✅ В системе: " + currentUser);
        } else {
            System.out.println("❌ Не авторизован");
        }
        System.out.println("═══════════════════════════════════════════════════════");
    }

    private static void registerUser() {
        System.out.println("\n📝 РЕГИСТРАЦИЯ ПОЛЬЗОВАТЕЛЯ");
        String login = getStringInput("Логин: ");
        String password = getStringInput("Пароль: ");

        String json = String.format("{\"login\":\"%s\",\"password\":\"%s\",\"role\":\"USER\"}", login, password);
        String response = sendRequest("/api/register", "POST", json, null);
        System.out.println("📨 Ответ: " + response);
    }

    private static void registerAdmin() {
        System.out.println("\n👑 РЕГИСТРАЦИЯ АДМИНИСТРАТОРА");
        String login = getStringInput("Логин: ");
        String password = getStringInput("Пароль: ");

        String json = String.format("{\"login\":\"%s\",\"password\":\"%s\",\"role\":\"ADMIN\"}", login, password);
        String response = sendRequest("/api/register", "POST", json, null);
        System.out.println("📨 Ответ: " + response);
    }

    private static void login() {
        System.out.println("\n🔐 ВХОД В СИСТЕМУ");
        String login = getStringInput("Логин: ");
        String password = getStringInput("Пароль: ");

        String json = String.format("{\"login\":\"%s\",\"password\":\"%s\"}", login, password);
        String response = sendRequest("/api/login", "POST", json, null);

        if (response.contains("token")) {
            int start = response.indexOf("\"token\":\"") + 9;
            int end = response.indexOf("\"", start);
            currentToken = response.substring(start, end);
            currentUser = login;
            System.out.println("✅ Вход выполнен успешно!");
        } else {
            System.out.println("❌ Ошибка входа: " + response);
        }
    }

    private static void generateCode() {
        if (currentToken == null) {
            System.out.println("❌ Сначала выполните вход!");
            return;
        }

        System.out.println("\n🎲 ГЕНЕРАЦИЯ OTP КОДА");
        String operationId = getStringInput("ID операции: ");

        String json = String.format("{\"operationId\":\"%s\"}", operationId);
        String response = sendRequest("/api/user/generate", "POST", json, currentToken);
        System.out.println("📨 Ответ: " + response);
    }

    private static void validateCode() {
        if (currentToken == null) {
            System.out.println("❌ Сначала выполните вход!");
            return;
        }

        System.out.println("\n🔍 ПРОВЕРКА OTP КОДА");
        String operationId = getStringInput("ID операции: ");
        String code = getStringInput("Код: ");

        String json = String.format("{\"operationId\":\"%s\",\"code\":\"%s\"}", operationId, code);
        String response = sendRequest("/api/user/validate", "POST", json, currentToken);
        System.out.println("📨 Ответ: " + response);
    }

    private static void listUsers() {
        if (currentToken == null) {
            System.out.println("❌ Сначала выполните вход!");
            return;
        }
        String response = sendRequest("/api/admin/users", "GET", null, currentToken);
        System.out.println("📨 Ответ: " + response);
    }

    private static void updateConfig() {
        if (currentToken == null) {
            System.out.println("❌ Сначала выполните вход!");
            return;
        }

        System.out.println("\n⚙️ ОБНОВЛЕНИЕ КОНФИГУРАЦИИ");
        int lifetime = getIntInput("Время жизни (сек): ");
        int length = getIntInput("Длина кода: ");

        String json = String.format("{\"lifetimeSeconds\":%d,\"codeLength\":%d}", lifetime, length);
        String response = sendRequest("/api/admin/config", "PUT", json, currentToken);
        System.out.println("📨 Ответ: " + response);
    }

    private static void deleteUser() {
        if (currentToken == null) {
            System.out.println("❌ Сначала выполните вход!");
            return;
        }

        System.out.println("\n🗑️ УДАЛЕНИЕ ПОЛЬЗОВАТЕЛЯ");
        int userId = getIntInput("ID пользователя: ");

        String json = String.format("{\"userId\":%d}", userId);
        String response = sendRequest("/api/admin/users/delete", "DELETE", json, currentToken);
        System.out.println("📨 Ответ: " + response);
    }

    private static void showStatus() {
        System.out.println("\n📊 СТАТУС");
        System.out.println("═══════════════════════════════════════");
        System.out.println("🔌 Сервер: http://localhost:" + ConfigLoader.getServerPort());
        System.out.println("🔑 Авторизация: " + (currentToken != null ? "✅ " + currentUser : "❌ Нет"));
        System.out.println("═══════════════════════════════════════");
    }

    private static String sendRequest(String endpoint, String method, String json, String token) {
        try {
            URL url = new URL("http://localhost:" + ConfigLoader.getServerPort() + endpoint);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod(method);
            conn.setRequestProperty("Content-Type", "application/json");

            if (token != null) {
                conn.setRequestProperty("Authorization", "Bearer " + token);
            }

            if (json != null) {
                conn.setDoOutput(true);
                try (OutputStream os = conn.getOutputStream()) {
                    os.write(json.getBytes());
                }
            }

            int code = conn.getResponseCode();
            BufferedReader br = new BufferedReader(new InputStreamReader(
                    code < 400 ? conn.getInputStream() : conn.getErrorStream()
            ));

            StringBuilder response = new StringBuilder();
            String line;
            while ((line = br.readLine()) != null) {
                response.append(line);
            }
            br.close();
            return response.toString();

        } catch (Exception e) {
            return "{\"error\":\"" + e.getMessage() + "\"}";
        }
    }

    private static String getStringInput(String prompt) {
        System.out.print(prompt);
        return scanner.nextLine();
    }

    private static int getIntInput(String prompt) {
        System.out.print(prompt);
        while (!scanner.hasNextInt()) {
            System.out.print("❌ Введите число: ");
            scanner.next();
        }
        int value = scanner.nextInt();
        scanner.nextLine();
        return value;
    }
}