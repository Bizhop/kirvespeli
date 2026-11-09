package fi.bizhop.kirves.service;

import fi.bizhop.kirves.db.UserDB;
import fi.bizhop.kirves.db.UserRepo;
import fi.bizhop.kirves.exception.UserException;
import fi.bizhop.kirves.model.User;
import fi.bizhop.kirves.model.UserIn;
import lombok.RequiredArgsConstructor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hibernate.exception.ConstraintViolationException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

import java.util.UUID;


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
        for (int attempt = 0; attempt < 10; attempt++) {
            // A fresh entity and repository transaction are needed after a failed insert.
            var userDB = new UserDB(user);
            userDB.nickname = generateNickname();
            try {
                this.USER_REPO.save(userDB);
                user.setNickname(userDB.nickname);
                return;
            } catch (DataIntegrityViolationException e) {
                if (attempt == 9 || !isNicknameCollision(e)) throw e;
            }
        }
    }

    String generateNickname() {
        return "Pelaaja-" + UUID.randomUUID().toString().substring(0, 6);
    }

    private boolean isNicknameCollision(DataIntegrityViolationException exception) {
        for (Throwable cause = exception; cause != null; cause = cause.getCause()) {
            if (cause instanceof ConstraintViolationException violation) {
                return "users_nickname_key".equals(violation.getConstraintName());
            }
        }
        return false;
    }

    public User updateUser(String email, UserIn userIn) throws UserException {
        var nickname = userIn.nickname == null ? "" : userIn.nickname.strip();
        if (nickname.isBlank()) throw new UserException("Nickname ei saa olla tyhjä");
        if (nickname.codePointCount(0, nickname.length()) > 100) {
            throw new UserException("Nickname saa olla enintään 100 merkkiä");
        }

        var user = this.USER_REPO.findByEmail(email).orElse(null);
        if(user == null) throw new UserException("Käyttäjän tallennus epäonnistui");

        user.nickname = nickname;
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
