package fi.bizhop.jassu.service;

import fi.bizhop.jassu.models.User;
import fi.bizhop.jassu.security.GoogleAuth;
import fi.bizhop.jassu.security.JWTAuth;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;

@Service
public class AuthService {
    private static final Map<String, User> users = new HashMap<>();

    private static final String HEADER_STRING = "Authorization";
    
    public User login(HttpServletRequest request) throws Exception {
        String userEmail = getEmail(request);
        if(userEmail == null) {
            return null;
        }
        else {
            if(users.get(userEmail) == null) {
                String jwt = JWTAuth.getJwt(userEmail);
                User user = new User(userEmail, jwt);
                users.put(userEmail, user);
                return user;
            }
            else {
                return users.get(userEmail);
            }
        }
    }

    private String getEmail(HttpServletRequest request) throws Exception {
        String token = request.getHeader(HEADER_STRING);
        String userEmail = null;
        if(token.startsWith(JWTAuth.JWT_TOKEN_PREFIX)) {
            userEmail = JWTAuth.getUserEmail(token);
        }
        if(userEmail == null) {
            userEmail = GoogleAuth.getUserEmail(token);
        }
        return userEmail;
    }
}
