package loris.parfume.Configurations.JWT;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class AuthorizationMethods {

    public static String secretKeyString;
    public static Long USER_ID;
    public static String ROLE;

    @Value("${jwt.token.secret}")
    public void setSecretKeyString(String secretKeyString) {

        AuthorizationMethods.secretKeyString = secretKeyString;
    }

    public static SecretKey getSecretKey() {

        byte[] secretBytes = secretKeyString.getBytes(StandardCharsets.UTF_8);

        return Keys.hmacShaKeyFor(secretBytes);
    }

    public static Map<String, Object> parseToken(HttpServletRequest request) throws Exception {

        String authorizationHeader = request.getHeader(HttpHeaders.AUTHORIZATION);

        if (authorizationHeader != null && authorizationHeader.toLowerCase().startsWith("bearer")) {

            String token = authorizationHeader.substring("Bearer".length()).trim();

            Claims claims = Jwts.parserBuilder()
                    .setSigningKey(getSecretKey())
                    .build()
                    .parseClaimsJws(token)
                    .getBody();

            Date expiration = claims.getExpiration();

            if (expiration != null && expiration.before(new Date())) {

                throw new Exception("Token is expired");
            }

            Map<String, Object> result = new HashMap<>();

            result.put("id", claims.get("id"));
            result.put("role", claims.get("role"));

            USER_ID = ((Number) claims.get("id")).longValue();
            ROLE = (String) claims.get("role");

            return result;
        }

        return null;
    }

    public static boolean isAuthorized(HttpServletRequest request, List<String> requiredRoles) throws Exception {

        Map<String, Object> tokenData = parseToken(request);

        if (tokenData != null) {

            String role = (String) tokenData.get("role");

            return !requiredRoles.contains(role);
        }

        return true;
    }

    public static boolean isJwtValid(String authorizationHeader) {

        if (authorizationHeader != null && authorizationHeader.toLowerCase().startsWith("bearer ")) {

            String token = authorizationHeader.substring("Bearer ".length()).trim();

            try {

                Claims claims = Jwts.parserBuilder()
                        .setSigningKey(getSecretKey())
                        .build()
                        .parseClaimsJws(token)
                        .getBody();

                Date expiration = claims.getExpiration();

                USER_ID = ((Number) claims.get("id")).longValue();
                ROLE = claims.get("role").toString();

                return expiration == null || !expiration.before(new Date()); // Token is expired
            }
            catch (Exception e) {

                return false; // Invalid token
            }
        }

        return false;
    }
}