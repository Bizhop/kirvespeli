package fi.bizhop.kirves.service;

import fi.bizhop.kirves.dbtest.DbTestBase;
import fi.bizhop.kirves.model.User;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class UserNicknameDbTest extends DbTestBase {
    @Test
    void nicknameCollisionCanBeRetriedAfterRepositoryRollback() {
        saveUser("existing@example.com", "Pelaaja-aaaaaa");
        var service = new UserService(this.userRepo) {
            private int attempts;

            @Override
            String generateNickname() {
                return ++attempts == 1 ? "Pelaaja-aaaaaa" : "Pelaaja-bbbbbb";
            }
        };
        var user = new User("new@example.com", null);

        service.add(user);

        assertEquals("Pelaaja-bbbbbb", user.getNickname());
        assertEquals(user.getNickname(), this.userRepo.findByEmail(user.getEmail()).orElseThrow().nickname);
        assertEquals("Pelaaja-aaaaaa", this.userRepo.findByEmail("existing@example.com").orElseThrow().nickname);
    }
}
