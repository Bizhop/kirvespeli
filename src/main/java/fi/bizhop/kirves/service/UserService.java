package fi.bizhop.kirves.service;

import fi.bizhop.kirves.db.UserDB;
import fi.bizhop.kirves.db.UserRepo;
import fi.bizhop.kirves.exception.UserException;
import fi.bizhop.kirves.model.User;
import fi.bizhop.kirves.model.UserIn;
import lombok.RequiredArgsConstructor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;


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
