package fi.bizhop.jassu.service;

import fi.bizhop.jassu.db.UserDB;
import fi.bizhop.jassu.db.UserRepo;
import fi.bizhop.jassu.exception.UserException;
import fi.bizhop.jassu.model.User;
import fi.bizhop.jassu.model.UserIn;
import lombok.RequiredArgsConstructor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;


@Service
@RequiredArgsConstructor
public class UserService {
    private static final Logger LOG = LogManager.getLogger(UserService.class);

    final UserRepo USER_REPO;

    public User get(String email) {
        var user = this.USER_REPO.findByEmail(email);
        if(user.isPresent()) {
            return new User(user.get());
        }
        else {
            LOG.warn(String.format("User email: %s not found", email));
            return null;
        }
    }

    public void add(User user) {
        var userDB = new UserDB(user);
        this.USER_REPO.save(userDB);
    }

    /**
     * Modify users money. Use negative values to subtract
     */
    public void modifyMoney(BigDecimal value, String email) {
        this.USER_REPO.findByEmail(email)
                .ifPresent(u -> {
                    u.money = u.money.add(value);
                    this.USER_REPO.save(u);
                });
    }

    public BigDecimal getUserMoney(String email) {
        return this.get(email).getMoney();
    }

    public User updateUser(String email, UserIn userIn) throws UserException {
        var user = this.USER_REPO.findByEmail(email).orElse(null);
        if(user == null) throw new UserException("Käyttäjän tallennus epäonnistui");

        user.nickname = userIn.nickname;
        try {
            var updated = this.USER_REPO.save(user);
            return new User(updated);
        } catch (DataIntegrityViolationException e) {
            throw new UserException("Nickname on jo käytössä");
        } catch (Exception e) {
            throw new UserException("Käyttäjän tallennus epäonnistui");
        }
    }

    public UserDB get(User user) {
        return this.USER_REPO.findByEmail(user.getEmail()).orElseThrow();
    }
}
