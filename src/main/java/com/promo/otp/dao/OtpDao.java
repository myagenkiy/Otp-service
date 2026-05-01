package com.promo.otp.dao;

import com.promo.otp.config.DatabaseConfig;
import com.promo.otp.model.OtpCode;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class OtpDao {

    /**
     * Сохраняет новый OTP код в базу данных
     */
    public OtpCode save(OtpCode otpCode) throws SQLException {
        String sql = "INSERT INTO otp_codes (user_id, operation_id, code, status, expires_at) " +
                "VALUES (?, ?, ?, ?, ?) RETURNING id, created_at";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, otpCode.getUserId());
            ps.setString(2, otpCode.getOperationId());
            ps.setString(3, otpCode.getCode());
            ps.setString(4, otpCode.getStatus());
            ps.setTimestamp(5, Timestamp.valueOf(otpCode.getExpiresAt()));

            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                otpCode.setId(rs.getInt("id"));
                otpCode.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
            }
        }
        return otpCode;
    }

    /**
     * Находит OTP код по ID операции и коду
     */
    public Optional<OtpCode> findByOperationIdAndCode(String operationId, String code) throws SQLException {
        String sql = "SELECT * FROM otp_codes WHERE operation_id = ? AND code = ?";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, operationId);
            ps.setString(2, code);

            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return Optional.of(mapRow(rs));
            }
        }
        return Optional.empty();
    }

    /**
     * Находит все активные OTP коды для пользователя
     */
    public List<OtpCode> findActiveByUserId(int userId) throws SQLException {
        List<OtpCode> codes = new ArrayList<>();
        String sql = "SELECT * FROM otp_codes WHERE user_id = ? AND status = 'ACTIVE' AND expires_at > NOW()";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, userId);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                codes.add(mapRow(rs));
            }
        }
        return codes;
    }

    /**
     * Находит OTP код по ID операции
     */
    public Optional<OtpCode> findByOperationId(String operationId) throws SQLException {
        String sql = "SELECT * FROM otp_codes WHERE operation_id = ?";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, operationId);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                return Optional.of(mapRow(rs));
            }
        }
        return Optional.empty();
    }

    /**
     * Обновляет статус OTP кода
     */
    public boolean updateStatus(int id, String status) throws SQLException {
        String sql = "UPDATE otp_codes SET status = ? WHERE id = ?";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, status);
            ps.setInt(2, id);

            return ps.executeUpdate() > 0;
        }
    }

    /**
     * Обновляет статус по ID операции и коду
     */
    public boolean updateStatusByOperationIdAndCode(String operationId, String code, String status) throws SQLException {
        String sql = "UPDATE otp_codes SET status = ? WHERE operation_id = ? AND code = ?";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, status);
            ps.setString(2, operationId);
            ps.setString(3, code);

            return ps.executeUpdate() > 0;
        }
    }

    /**
     * Помечает все просроченные коды как EXPIRED
     * @return количество обновленных кодов
     */
    public int expireOldCodes() throws SQLException {
        String sql = "UPDATE otp_codes SET status = 'EXPIRED' " +
                "WHERE status = 'ACTIVE' AND expires_at < NOW()";

        try (Connection conn = DatabaseConfig.getConnection();
             Statement stmt = conn.createStatement()) {

            return stmt.executeUpdate(sql);
        }
    }

    /**
     * Удаляет все коды пользователя
     */
    public int deleteByUserId(int userId) throws SQLException {
        String sql = "DELETE FROM otp_codes WHERE user_id = ?";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, userId);
            return ps.executeUpdate();
        }
    }

    /**
     * Удаляет код по ID операции
     */
    public boolean deleteByOperationId(String operationId) throws SQLException {
        String sql = "DELETE FROM otp_codes WHERE operation_id = ?";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, operationId);
            return ps.executeUpdate() > 0;
        }
    }

    /**
     * Проверяет, активен ли код
     */
    public boolean isCodeValid(String operationId, String code) throws SQLException {
        String sql = "SELECT COUNT(*) FROM otp_codes " +
                "WHERE operation_id = ? AND code = ? " +
                "AND status = 'ACTIVE' AND expires_at > NOW()";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, operationId);
            ps.setString(2, code);

            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return rs.getInt(1) > 0;
            }
        }
        return false;
    }

    /**
     * Получает количество активных кодов для пользователя
     */
    public int countActiveCodesByUserId(int userId) throws SQLException {
        String sql = "SELECT COUNT(*) FROM otp_codes " +
                "WHERE user_id = ? AND status = 'ACTIVE' AND expires_at > NOW()";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, userId);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                return rs.getInt(1);
            }
        }
        return 0;
    }

    /**
     * Получает все коды пользователя (для аудита)
     */
    public List<OtpCode> findAllByUserId(int userId) throws SQLException {
        List<OtpCode> codes = new ArrayList<>();
        String sql = "SELECT * FROM otp_codes WHERE user_id = ? ORDER BY created_at DESC";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, userId);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                codes.add(mapRow(rs));
            }
        }
        return codes;
    }

    /**
     * Очищает все просроченные коды (физическое удаление)
     * @return количество удаленных кодов
     */
    public int deleteExpiredCodes() throws SQLException {
        String sql = "DELETE FROM otp_codes WHERE expires_at < NOW()";

        try (Connection conn = DatabaseConfig.getConnection();
             Statement stmt = conn.createStatement()) {

            return stmt.executeUpdate(sql);
        }
    }

    /**
     * Маппинг ResultSet в объект OtpCode
     */
    private OtpCode mapRow(ResultSet rs) throws SQLException {
        OtpCode otpCode = new OtpCode();
        otpCode.setId(rs.getInt("id"));
        otpCode.setUserId(rs.getInt("user_id"));
        otpCode.setOperationId(rs.getString("operation_id"));
        otpCode.setCode(rs.getString("code"));
        otpCode.setStatus(rs.getString("status"));

        Timestamp createdAt = rs.getTimestamp("created_at");
        if (createdAt != null) {
            otpCode.setCreatedAt(createdAt.toLocalDateTime());
        }

        Timestamp expiresAt = rs.getTimestamp("expires_at");
        if (expiresAt != null) {
            otpCode.setExpiresAt(expiresAt.toLocalDateTime());
        }

        return otpCode;
    }
}