package fi.bizhop.kirves.dbtest;

import fi.bizhop.kirves.exception.UserException;
import fi.bizhop.kirves.model.UserIn;
import fi.bizhop.kirves.service.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import org.springframework.dao.DataIntegrityViolationException;

import static org.junit.jupiter.api.Assertions.*;

@Import(UserService.class)
class UserDbTest extends DbTestBase {
    @Autowired
    UserService userService;

    @Test
    void findByEmail() {
        var saved = saveUser("a@example.com", "A");

        var found = this.userRepo.findByEmail("a@example.com").orElseThrow();
        assertEquals(saved.id, found.id);
        assertEquals("A", found.nickname);
        assertTrue(this.userRepo.findByEmail("A@EXAMPLE.COM").isEmpty(), "email lookup is case sensitive");
    }

    @Test
    void timestampsAreSet() throws InterruptedException {
        var saved = saveUser("a@example.com", null);
        assertNotNull(saved.createdAt);

        Thread.sleep(10);
        saved.nickname = "A";
        var updated = this.userRepo.save(saved);

        var reloaded = this.userRepo.findById(updated.id).orElseThrow();
        assertNotNull(reloaded.createdAt);
        assertNotNull(reloaded.updatedAt);
        assertTrue(reloaded.updatedAt.isAfter(reloaded.createdAt));
    }

    @Test
    void uniqueNicknameAllowsSeveralNulls() {
        saveUser("a@example.com", null);
        assertDoesNotThrow(() -> saveUser("b@example.com", null));
    }

    @Test
    void uniqueNicknameRejectsDuplicate() {
        saveUser("a@example.com", "Same");
        assertThrows(DataIntegrityViolationException.class, () -> saveUser("b@example.com", "Same"));
    }

    @Test
    void updateUserReportsTakenNickname() throws UserException {
        saveUser("a@example.com", null);
        saveUser("b@example.com", null);
        this.userService.updateUser("a@example.com", nickname("Same"));

        // relies on the constraint violation surfacing at save, i.e. no surrounding transaction
        var e = assertThrows(UserException.class, () -> this.userService.updateUser("b@example.com", nickname("Same")));
        assertEquals("Nickname on jo käytössä", e.getMessage());
        assertNull(this.userRepo.findByEmail("b@example.com").orElseThrow().nickname);
    }

    private static UserIn nickname(String nickname) {
        var in = new UserIn();
        in.nickname = nickname;
        return in;
    }
}
