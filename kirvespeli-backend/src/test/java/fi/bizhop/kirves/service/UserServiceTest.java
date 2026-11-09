package fi.bizhop.kirves.service;

import fi.bizhop.kirves.db.UserRepo;
import fi.bizhop.kirves.db.UserDB;
import fi.bizhop.kirves.model.User;
import fi.bizhop.kirves.model.UserIn;
import fi.bizhop.kirves.model.kirves.pojo.UserPOJO;
import fi.bizhop.kirves.exception.UserException;
import org.hibernate.exception.ConstraintViolationException;
import org.springframework.dao.DataIntegrityViolationException;
import java.sql.SQLException;
import org.mockito.ArgumentCaptor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.Optional;

import static fi.bizhop.kirves.util.TestUserUtil.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(SpringExtension.class)
public class UserServiceTest {
    @MockitoBean
    UserRepo userRepo;

    UserService userService;

    @BeforeEach
    public void setup() {
        this.userService = new UserService(this.userRepo);
    }

    @Test
    public void testUserObject() {
        when(this.userRepo.findByEmail(eq(TEST_USER_EMAIL))).thenReturn(Optional.of(getTestUserDB(TEST_USER_EMAIL)));

        var user = this.userService.get(TEST_USER_EMAIL);
        assertEquals(TEST_USER_EMAIL, user.getEmail());

        var noUser = this.userService.get("other@example.com");
        assertNull(noUser);
    }

    @Test
    void addGeneratesNicknameAndReturnsItOnUser() {
        var user = new User(TEST_USER_EMAIL, "token");
        this.userService.add(user);

        var saved = ArgumentCaptor.forClass(UserDB.class);
        verify(this.userRepo).save(saved.capture());
        assertTrue(user.getNickname().matches("Pelaaja-[a-z0-9]{6}"));
        assertEquals(user.getNickname(), saved.getValue().nickname);
        assertEquals(TEST_USER_EMAIL, saved.getValue().email);
        assertEquals("token", user.getJwt());
    }

    @Test
    void addRetriesNicknameCollisionWithFreshEntity() {
        var service = spy(this.userService);
        doReturn("Pelaaja-aaaaaa", "Pelaaja-bbbbbb").when(service).generateNickname();
        when(this.userRepo.save(any(UserDB.class)))
                .thenThrow(collision("users_nickname_key"))
                .thenAnswer(invocation -> invocation.getArgument(0));
        var user = new User(TEST_USER_EMAIL, null);

        service.add(user);

        var saved = ArgumentCaptor.forClass(UserDB.class);
        verify(this.userRepo, times(2)).save(saved.capture());
        assertEquals("Pelaaja-aaaaaa", saved.getAllValues().getFirst().nickname);
        assertEquals("Pelaaja-bbbbbb", user.getNickname());
        assertNotSame(saved.getAllValues().getFirst(), saved.getAllValues().getLast());
    }

    @Test
    void addDoesNotRetryOtherConstraintViolations() {
        var failure = collision("users_email_key");
        when(this.userRepo.save(any(UserDB.class))).thenThrow(failure);
        assertSame(failure, assertThrows(DataIntegrityViolationException.class,
                () -> this.userService.add(new User(TEST_USER_EMAIL, null))));
        verify(this.userRepo).save(any(UserDB.class));
    }

    @Test
    void addStopsAfterRepeatedCollisions() {
        when(this.userRepo.save(any(UserDB.class))).thenThrow(collision("users_nickname_key"));
        assertThrows(DataIntegrityViolationException.class,
                () -> this.userService.add(new User(TEST_USER_EMAIL, null)));
        verify(this.userRepo, times(10)).save(any(UserDB.class));
    }

    @Test
    void updateRejectsBlankAndLongNicknamesBeforeSaving() {
        for (var value : new String[] { null, "", "  \t\n", "\u2003", "a".repeat(101) }) {
            var in = new UserIn();
            in.nickname = value;
            var exception = assertThrows(UserException.class,
                    () -> this.userService.updateUser(TEST_USER_EMAIL, in));
            assertEquals(value != null && value.length() > 100
                    ? "Nickname saa olla enintään 100 merkkiä" : "Nickname ei saa olla tyhjä",
                    exception.getMessage());
        }
        verifyNoInteractions(this.userRepo);
    }

    @Test
    void updateTrimsAndAcceptsMaximumLength() throws UserException {
        when(this.userRepo.findByEmail(TEST_USER_EMAIL))
                .thenReturn(Optional.of(getTestUserDB(TEST_USER_EMAIL)));
        when(this.userRepo.save(any(UserDB.class))).thenAnswer(invocation -> invocation.getArgument(0));
        var in = new UserIn();
        in.nickname = "  " + "a".repeat(100) + "  ";
        assertEquals("a".repeat(100), this.userService.updateUser(TEST_USER_EMAIL, in).getNickname());
    }

    @Test
    void missingNicknameNeverFallsBackToEmail() {
        var user = new User(TEST_USER_EMAIL, null);
        assertNull(user.getNickname());
        assertNull(new UserPOJO(TEST_USER_EMAIL, null).getNickname());
        user.setNickname("");
        assertEquals("", user.getNickname());
    }

    private static DataIntegrityViolationException collision(String constraint) {
        return new DataIntegrityViolationException("constraint violation",
                new ConstraintViolationException("duplicate", new SQLException("duplicate", "23505"), constraint));
    }
}
