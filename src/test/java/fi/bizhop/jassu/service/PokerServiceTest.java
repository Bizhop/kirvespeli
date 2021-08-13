package fi.bizhop.jassu.service;


import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.mockito.Mockito.mock;

public class PokerServiceTest {
    static UserService userService;

    @BeforeAll
    public static void setup() {
        userService = mock(UserService.class);
    }

    @Test
    public void dummy() {
        PokerService service = new PokerService(userService);
        service.dummy();
    }
}
