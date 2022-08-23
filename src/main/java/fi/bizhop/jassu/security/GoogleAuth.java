package fi.bizhop.jassu.security;

import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.List;

public class GoogleAuth {
    private static final Logger LOG = LogManager.getLogger(GoogleAuth.class);

    private static final GoogleIdTokenVerifier VERIFIER;

    static {
        VERIFIER = new GoogleIdTokenVerifier.Builder(new NetHttpTransport(), new GsonFactory())
                .setAudience(List.of(System.getenv("CLIENT_ID")))
                .build();
    }

    public static String getUserEmail(String token) {
        LOG.debug(String.format("Authenticating with google token: %s", token));

        var email = verifyAndGetEmail(token);
        if(email != null) {
            LOG.debug(String.format("Google user found with email: %s", email));
        }
        return email;
    }

    public static String verifyAndGetEmail(String token) {
        try {
            var gToken = VERIFIER.verify(token);
            if(gToken != null) {
                return gToken.getPayload().getEmail();
            }
            else {
                LOG.error("Invalid ID token");
                return null;
            }
        } catch (Exception e) {
            LOG.error("Error handling id token", e);
            return null;
        }
    }
}
