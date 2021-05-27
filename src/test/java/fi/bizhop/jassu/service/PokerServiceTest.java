package fi.bizhop.jassu.service;

import org.junit.BeforeClass;
import org.junit.Test;

import static org.mockito.Mockito.mock;

public class PokerServiceTest {
    static UserService userService;

    @BeforeClass
    public static void setup() {
        userService = mock(UserService.class);
    }

    @Test
    public void dummy() {
        PokerService service = new PokerService(userService);
        service.dummy();
    }
}
