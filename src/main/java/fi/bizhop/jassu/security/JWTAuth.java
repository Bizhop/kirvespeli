package fi.bizhop.jassu.security;

import fi.bizhop.jassu.Application;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.apache.logging.log4j.*;

import java.util.Date;

public class JWTAuth {
    private static final Logger LOG = LogManager.getLogger(JWTAuth.class);
    private static final long EXPIRATION_TIME = 14 * 24 * 60 * 60 * 1000; //14 days
    private static final String JWT_SECRET;
    public static final String JWT_TOKEN_PREFIX = "JWT ";

    static {
        JWT_SECRET = System.getenv("JASSU_JWT_SECRET");
        if(JWT_SECRET == null) {
            LOG.error("Env JASSU_JWT_SECRET must be set!");
            Application.exit();
        }
    }

    public static String getJwt(String email) {
        Claims claims = Jwts.claims();
        claims.put("email", email);

        String jwt = Jwts.builder()
                .setSubject(email)
                .setClaims(claims)
                .setExpiration(new Date(System.currentTimeMillis() + EXPIRATION_TIME))
                .signWith(SignatureAlgorithm.HS512, JWT_SECRET)
                .compact();

        LOG.debug("Generated jwt for " + email);
        return JWT_TOKEN_PREFIX + jwt;
    }

    public static String getUserEmail(String token) {
        Claims claims = getClaims(token);

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
            return Jwts.parser()
                    .setSigningKey(JWT_SECRET)
                    .parseClaimsJws(token.replace(JWT_TOKEN_PREFIX,""))
                    .getBody();
        } catch (RuntimeException e) {
            return null;
        }
    }
}
