package fi.bizhop.jassu.service;

import fi.bizhop.jassu.db.KirvesGameDB;
import fi.bizhop.jassu.db.KirvesGameRepo;
import fi.bizhop.jassu.exception.CardException;
import fi.bizhop.jassu.exception.KirvesGameException;
import org.apache.commons.io.FileUtils;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.File;
import java.io.IOException;
import java.util.Optional;

import static fi.bizhop.jassu.util.TestUserUtil.TEST_USER_EMAIL;
import static fi.bizhop.jassu.util.TestUserUtil.getTestUserDB;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@RunWith(SpringRunner.class)
public class KirverServiceTest {
    @MockBean
    UserService userService;

    @MockBean
    KirvesGameRepo kirvesGameRepo;

    KirvesService kirvesService;

    @Before
    public void setup() {
        this.kirvesService = new KirvesService(userService, kirvesGameRepo);
    }

    @Test
    public void testInMemoryGames() throws CardException, KirvesGameException, IOException {
        when(kirvesGameRepo.findByIdAndActiveTrue(eq(0L))).thenReturn(Optional.of(getTestGameDB()));

        this.kirvesService.getGame(0L);
        this.kirvesService.getGame(0L);

        //verify that db was called just once
        verify(kirvesGameRepo, times(1)).findByIdAndActiveTrue(any());
    }

    private KirvesGameDB getTestGameDB() throws IOException {
        KirvesGameDB db = new KirvesGameDB();
        db.id = 0L;
        db.admin = getTestUserDB(TEST_USER_EMAIL);
        db.canJoin = true;
        db.active = true;
        db.players = 4;
        db.gameData = FileUtils.readFileToString(new File("src/test/resources/testData.json"), "UTF-8");
        return db;
    }
}
