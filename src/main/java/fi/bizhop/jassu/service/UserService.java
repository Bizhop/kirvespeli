package fi.bizhop.jassu.service;

import fi.bizhop.jassu.db.UserDB;
import fi.bizhop.jassu.db.UserRepo;
import fi.bizhop.jassu.model.User;
import fi.bizhop.jassu.model.UserIn;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Optional;


@Service
public class UserService {
    private static final Logger LOG = LogManager.getLogger(UserService.class);

    final UserRepo userRepo;

    public UserService(UserRepo userRepo) {
        this.userRepo = userRepo;
    }

    public User get(String email) {
        Optional<UserDB> user = userRepo.findByEmail(email);
        if(user.isPresent()) {
            return new User(user.get());
        }
        else {
            LOG.warn(String.format("User email: %s not found", email));
            return null;
        }
    }

    public void add(User user) {
        UserDB userDB = new UserDB(user);
        userRepo.save(userDB);
    }

    /**
     * Modify users money. Use negative values to subtract
     */
    public void modifyMoney(BigDecimal value, String email) {
        userRepo.findByEmail(email)
                .ifPresent(u -> {
                    u.money = u.money.add(value);
                    this.userRepo.save(u);
                });
    }

    public BigDecimal getUserMoney(String email) {
        return this.get(email).getMoney();
    }

    public User updateUser(String email, UserIn userIn) {
        UserDB user = userRepo.findByEmail(email).orElse(null);
        if(user == null) {
            return null;
        } else {
            user.nickname = userIn.nickname;
            return new User(this.userRepo.save(user));
        }
    }

    public UserDB get(User admin) {
        return userRepo.findByEmail(admin.getEmail()).orElseThrow();
    }
}
