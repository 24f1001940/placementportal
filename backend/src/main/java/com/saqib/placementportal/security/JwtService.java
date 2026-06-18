package com.saqib.placementportal.security;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.saqib.placementportal.entity.UserAccount;
import com.saqib.placementportal.exception.ApiException;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Base64;
import java.util.List;
import java.util.Map;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

@Service
public class JwtService {
    private static final String HMAC_ALGORITHM = "HmacSHA256";
    private static final Base64.Encoder URL_ENCODER = Base64.getUrlEncoder().withoutPadding();
    private static final Base64.Decoder URL_DECODER = Base64.getUrlDecoder();

    private final ObjectMapper objectMapper;
    private final byte[] secret;
    private final long expirationMinutes;

    public JwtService(
            ObjectMapper objectMapper,
            @Value("${app.jwt.secret}") String secret,
            @Value("${app.jwt.expiration-minutes}") long expirationMinutes
    ) {
        if (secret.length() < 32) {
            throw new IllegalArgumentException("app.jwt.secret must be at least 32 characters");
        }
        this.objectMapper = objectMapper;
        this.secret = secret.getBytes(StandardCharsets.UTF_8);
        this.expirationMinutes = expirationMinutes;
    }

    public String generate(UserAccount user) {
        try {
            Instant now = Instant.now();
            Map<String, Object> header = Map.of("alg", "HS256", "typ", "JWT");
            Map<String, Object> payload = Map.of(
                    "sub", user.getEmail(),
                    "userId", user.getId(),
                    "role", user.primaryRole().name(),
                    "iat", now.getEpochSecond(),
                    "exp", now.plusSeconds(expirationMinutes * 60).getEpochSecond()
            );
            String headerPart = encodeJson(header);
            String payloadPart = encodeJson(payload);
            String signingInput = headerPart + "." + payloadPart;
            return signingInput + "." + sign(signingInput);
        } catch (Exception ex) {
            throw new ApiException(HttpStatus.INTERNAL_SERVER_ERROR, "Could not create JWT");
        }
    }

    public String subject(String token) {
        Map<String, Object> payload = payload(token);
        return (String) payload.get("sub");
    }

    public boolean isValid(String token, UserAccount user) {
        Map<String, Object> payload = payload(token);
        Number exp = (Number) payload.get("exp");
        return user.getEmail().equals(payload.get("sub")) && Instant.now().isBefore(Instant.ofEpochSecond(exp.longValue()));
    }

    private Map<String, Object> payload(String token) {
        try {
            String[] parts = token.split("\\.");
            if (parts.length != 3) {
                throw new ApiException(HttpStatus.UNAUTHORIZED, "Invalid token");
            }
            String expected = sign(parts[0] + "." + parts[1]);
            if (!MessageDigestSafe.equals(expected, parts[2])) {
                throw new ApiException(HttpStatus.UNAUTHORIZED, "Invalid token signature");
            }
            Map<String, Object> payload = objectMapper.readValue(URL_DECODER.decode(parts[1]), new TypeReference<>() {
            });
            Number exp = (Number) payload.get("exp");
            if (exp == null || Instant.now().isAfter(Instant.ofEpochSecond(exp.longValue()))) {
                throw new ApiException(HttpStatus.UNAUTHORIZED, "Token expired");
            }
            return payload;
        } catch (ApiException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new ApiException(HttpStatus.UNAUTHORIZED, "Invalid token");
        }
    }

    private String encodeJson(Map<String, Object> json) throws Exception {
        return URL_ENCODER.encodeToString(objectMapper.writeValueAsBytes(json));
    }

    private String sign(String value) throws Exception {
        Mac mac = Mac.getInstance(HMAC_ALGORITHM);
        mac.init(new SecretKeySpec(secret, HMAC_ALGORITHM));
        return URL_ENCODER.encodeToString(mac.doFinal(value.getBytes(StandardCharsets.UTF_8)));
    }

    private static final class MessageDigestSafe {
        private MessageDigestSafe() {
        }

        static boolean equals(String left, String right) {
            return java.security.MessageDigest.isEqual(
                    left.getBytes(StandardCharsets.UTF_8),
                    right.getBytes(StandardCharsets.UTF_8)
            );
        }
    }
}
