package fi.bizhop.jassu.service;

import fi.bizhop.jassu.db.UserRepo;
import fi.bizhop.jassu.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.Optional;

import static fi.bizhop.jassu.util.TestUserUtil.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(SpringExtension.class)
public class UserServiceTest {
    @MockBean
    UserRepo userRepo;

    UserService userService;

    @BeforeEach
    public void setup() {
        this.userService = new UserService(this.userRepo);
    }

    @Test
    public void testUserObject() {
        when(this.userRepo.findByEmail(eq(TEST_USER_EMAIL))).thenReturn(Optional.of(getTestUserDB(TEST_USER_EMAIL)));

        User user = this.userService.get(TEST_USER_EMAIL);
        assertEquals(TEST_USER_EMAIL, user.getEmail());

        User noUser = this.userService.get("other@example.com");
        assertNull(noUser);
    }
}
