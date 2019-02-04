package fi.bizhop.jassu.service;

import fi.bizhop.jassu.models.User;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

@Service
public class UserService {
    private static final Map<String, User> users = new HashMap<>();

    public User get(String email) {
        return users.get(email);
    }

    public void add(User user) {
        users.put(user.getEmail(), user);
    }

    /**
     * Modify users money. Use negative values to subtract
     */
    public static void modifyMoney(BigDecimal value, String email) {
        User user = users.get(email);
        if(user != null) {
            user.setMoney(user.getMoney().add(value));
        }
    }
}
