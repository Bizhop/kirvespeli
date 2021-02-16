package fi.bizhop.jassu;

import fi.bizhop.jassu.db.UserDB;
import fi.bizhop.jassu.db.UserRepo;
import fi.bizhop.jassu.models.User;
import fi.bizhop.jassu.service.UserService;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.Optional;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class UserServiceTest {
    static UserRepo userRepo;

    final String TEST_USER_EMAIL = "user@mock.com";

    @BeforeClass
    public static void setup() {
        userRepo = mock(UserRepo.class);
    }

    @Test
    public void testUserObject() {
        assertNotNull(userRepo);
        when(userRepo.findByEmail(eq(TEST_USER_EMAIL))).thenReturn(getTestUser());

        UserService service = new UserService(userRepo);
        User user = service.get(TEST_USER_EMAIL);
        assertEquals(TEST_USER_EMAIL, user.getEmail());

        User noUser = service.get("other@example.com");
        assertNull(noUser);
    }

    private Optional<UserDB> getTestUser() {
        return Optional.of(new UserDB(new User(TEST_USER_EMAIL, "")));
    }
}
