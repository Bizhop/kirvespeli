package fi.bizhop.jassu.service;

import fi.bizhop.jassu.db.UserRepo;
import fi.bizhop.jassu.model.User;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Optional;

import static fi.bizhop.jassu.util.TestUserUtil.*;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@RunWith(SpringRunner.class)
public class UserServiceTest {
    @MockBean
    UserRepo userRepo;

    UserService userService;

    @Before
    public void setup() {
        this.userService = new UserService(userRepo);
    }

    @Test
    public void testUserObject() {
        when(userRepo.findByEmail(eq(TEST_USER_EMAIL))).thenReturn(Optional.of(getTestUserDB(TEST_USER_EMAIL)));

        User user = userService.get(TEST_USER_EMAIL);
        assertEquals(TEST_USER_EMAIL, user.getEmail());

        User noUser = userService.get("other@example.com");
        assertNull(noUser);
    }
}
