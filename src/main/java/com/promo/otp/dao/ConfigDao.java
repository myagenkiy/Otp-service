package com.promo.otp.dao;

import com.promo.otp.config.DatabaseConfig;
import com.promo.otp.model.Config;

import java.sql.*;

public class ConfigDao {

    /**
     * Получает текущую конфигурацию (всегда одна запись с id=1)
     */
    public Config getConfig() throws SQLException {
        String sql = "SELECT * FROM config WHERE id = 1";

        try (Connection conn = DatabaseConfig.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            if (rs.next()) {
                return mapRow(rs);
            } else {
                // Если конфигурации нет, создаем дефолтную
                Config defaultConfig = new Config();
                saveConfig(defaultConfig);
                return defaultConfig;
            }
        }
    }

    /**
     * Обновляет конфигурацию
     */
    public Config updateConfig(int codeLifetimeSeconds, int codeLength) throws SQLException {
        String sql = "UPDATE config SET code_lifetime_seconds = ?, code_length = ? WHERE id = 1";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, codeLifetimeSeconds);
            ps.setInt(2, codeLength);
            ps.executeUpdate();
        }

        // Возвращаем обновленную конфигурацию
        return getConfig();
    }

    /**
     * Сохраняет новую конфигурацию (если таблица пуста)
     */
    private void saveConfig(Config config) throws SQLException {
        String sql = "INSERT INTO config (id, code_lifetime_seconds, code_length) VALUES (1, ?, ?)";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, config.getCodeLifetimeSeconds());
            ps.setInt(2, config.getCodeLength());
            ps.executeUpdate();
        }
    }

    private Config mapRow(ResultSet rs) throws SQLException {
        Config config = new Config();
        config.setId(rs.getInt("id"));
        config.setCodeLifetimeSeconds(rs.getInt("code_lifetime_seconds"));
        config.setCodeLength(rs.getInt("code_length"));
        return config;
    }
}