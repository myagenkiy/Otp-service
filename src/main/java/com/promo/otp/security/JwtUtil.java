package com.promo.otp.security;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.promo.otp.config.ConfigLoader;
import java.util.Date;

public class JwtUtil {
    private static final String SECRET = ConfigLoader.getJwtSecret();
    private static final Algorithm ALGORITHM = Algorithm.HMAC256(SECRET);

    public static String generate(String login, String role) {
        int hours = ConfigLoader.getJwtExpirationHours();
        return JWT.create()
                .withSubject(login)
                .withClaim("role", role)
                .withIssuedAt(new Date())
                .withExpiresAt(new Date(System.currentTimeMillis() + hours * 3600000L))
                .sign(ALGORITHM);
    }

    public static DecodedJWT verify(String token) {
        return JWT.require(ALGORITHM).build().verify(token);
    }

    public static String getLoginFromToken(String token) {
        return verify(token).getSubject();
    }

    public static String getRoleFromToken(String token) {
        return verify(token).getClaim("role").asString();
    }
}