package fi.bizhop.jassu.security;

import fi.bizhop.jassu.Application;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

public class JWTAuth {
    private static final Logger LOG = LogManager.getLogger(JWTAuth.class);
    private static final long EXPIRATION_TIME = 14 * 24 * 60 * 60 * 1000; //14 days
    private static final SecretKey JWT_KEY;
    public static final String JWT_TOKEN_PREFIX = "JWT ";

    static {
        var jwtSecret = System.getenv("JASSU_JWT_SECRET");
        if(jwtSecret == null) {
            LOG.error("Env JASSU_JWT_SECRET must be set!");
            Application.exit();
        }
        JWT_KEY = Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8));
    }

    public static String getJwt(String email) {
        var claims = Jwts.claims();
        claims.put("email", email);

        var jwt = Jwts.builder()
                .setSubject(email)
                .setClaims(claims)
                .setExpiration(new Date(System.currentTimeMillis() + EXPIRATION_TIME))
                .signWith(JWT_KEY)
                .compact();

        LOG.debug("Generated jwt for " + email);
        return JWT_TOKEN_PREFIX + jwt;
    }

    public static String getUserEmail(String token) {
        var claims = getClaims(token);

        if(claims == null || claims.get("email") == null) {
            return null;
        }

        return (String)claims.get("email");
    }

    private static Claims getClaims(String token) {
        if(token == null) {
            return null;
        }

        try {
            var jwt = token.replace(JWT_TOKEN_PREFIX,"");
            return Jwts.parserBuilder()
                    .setSigningKey(JWT_KEY)
                    .build()
                    .parseClaimsJws(jwt)
                    .getBody();
        } catch (RuntimeException e) {
            return null;
        }
    }
}
