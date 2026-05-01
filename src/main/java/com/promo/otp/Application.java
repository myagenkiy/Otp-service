package com.promo.otp;

import com.promo.otp.config.ConfigLoader;
import com.promo.otp.controller.AdminController;
import com.promo.otp.controller.AuthController;
import com.promo.otp.controller.UserController;
import com.promo.otp.service.ExpiredCodesCleaner;
import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.concurrent.Executors;

public class Application {

    public static void main(String[] args) throws IOException {
        int port = ConfigLoader.getServerPort();

        System.out.println("═══════════════════════════════════════════════════════");
        System.out.println("🔧 OTP SERVICE CONFIGURATION");
        System.out.println("═══════════════════════════════════════════════════════");
        System.out.println("📡 Server port: " + port);
        System.out.println("🗄️  Database: " + ConfigLoader.getDbUrl());
        System.out.println("🤖 Telegram: " + (ConfigLoader.getTelegramBotToken().isEmpty() ? "❌ Not configured" : "✅ Configured"));
        System.out.println("📧 Email: " + (ConfigLoader.getEmailUsername().isEmpty() ? "❌ Not configured" : "✅ Configured"));
        System.out.println("═══════════════════════════════════════════════════════");

        HttpServer server = HttpServer.create(new InetSocketAddress(port), 0);

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
}