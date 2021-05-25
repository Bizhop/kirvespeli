package fi.bizhop.jassu.service;

import fi.bizhop.jassu.db.KirvesGameDB;
import fi.bizhop.jassu.db.KirvesGameRepo;
import fi.bizhop.jassu.exception.CardException;
import fi.bizhop.jassu.exception.KirvesGameException;
import fi.bizhop.jassu.exception.TransactionException;
import fi.bizhop.jassu.model.User;
import fi.bizhop.jassu.model.kirves.Game;
import fi.bizhop.jassu.model.kirves.in.GameIn;
import fi.bizhop.jassu.model.kirves.pojo.GameDataPOJO;
import fi.bizhop.jassu.util.JsonUtil;
import org.apache.commons.io.FileUtils;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.File;
import java.io.IOException;
import java.util.Optional;

import static fi.bizhop.jassu.model.kirves.Game.Action.CUT;
import static fi.bizhop.jassu.util.TestUserUtil.*;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

@RunWith(SpringRunner.class)
public class KirvesServiceTest {
    @MockBean
    KirvesGameRepo kirvesGameRepo;

    KirvesService kirvesService;

    @Before
    public void setup() {
        this.kirvesService = new KirvesService(null, kirvesGameRepo);
    }

    @Test
    public void testInMemoryGames() throws CardException, KirvesGameException, IOException {
        when(kirvesGameRepo.findByIdAndActiveTrue(eq(0L))).thenReturn(Optional.of(getTestGameDB()));

        this.kirvesService.getGame(0L);
        this.kirvesService.getGame(0L);

        //verify that db was called just once
        verify(kirvesGameRepo, times(1)).findByIdAndActiveTrue(any());
    }

    @Test
    public void testTransactionSuccess() throws IOException, TransactionException, CardException, KirvesGameException {
        when(kirvesGameRepo.findByIdAndActiveTrue(eq(0L))).thenReturn(Optional.of(getTestGameDB()));

        User user = getTestUser();

        GameIn input = new GameIn();
        input.action = CUT;
        input.declineCut = false;

        Game output = this.kirvesService.action(0L, input, user, 0);

        //verify data changed
        KirvesGameDB originalDB = getTestGameDB();
        GameDataPOJO originalPojo = JsonUtil.getJavaObject(originalDB.gameData, GameDataPOJO.class).orElse(null);
        assertNotNull(originalPojo);

        String json = output.toJson();
        GameDataPOJO pojo = JsonUtil.getJavaObject(json, GameDataPOJO.class).orElseThrow();
        assertNotEquals(originalPojo, pojo);
    }

    @Test
    public void testTransactionTimeout() throws IOException, CardException, KirvesGameException {
        when(kirvesGameRepo.findByIdAndActiveTrue(eq(0L))).thenReturn(Optional.of(getTestGameDB()));

        User user = getTestUser();

        GameIn input = new GameIn();
        input.action = CUT;
        input.declineCut = false;

        try {
            this.kirvesService.action(0L, input, user, 6 * 1000);
        } catch (TransactionException e) {
            //verify data didn't change
            KirvesGameDB originalDB = getTestGameDB();
            GameDataPOJO originalPojo = JsonUtil.getJavaObject(originalDB.gameData, GameDataPOJO.class).orElse(null);
            assertNotNull(originalPojo);

            //make sure you get the inMemoryGame by checking the call wasn't made on DB
            verify(kirvesGameRepo, times(1)).findByIdAndActiveTrue(any());
            Game game = this.kirvesService.getGame(0L);
            verify(kirvesGameRepo, times(1)).findByIdAndActiveTrue(any());

            String json = game.toJson();
            GameDataPOJO pojo = JsonUtil.getJavaObject(json, GameDataPOJO.class).orElseThrow();
            assertEquals(originalPojo, pojo);
            return;
        }

        //expecting tx timeout, so reaching here is fail
        fail();
    }

    @Test
    public void testTransactionLock() throws IOException, InterruptedException {
        when(kirvesGameRepo.findByIdAndActiveTrue(eq(0L))).thenReturn(Optional.of(getTestGameDB()));

        Thread p1Thread = new Thread(() -> {
            User user = getTestUser();

            GameIn input = new GameIn();
            input.action = CUT;
            input.declineCut = false;

            try {
                this.kirvesService.action(0L, input, user, 3 * 1000);
            } catch (Exception e) {
                fail();
            }
        });

        Thread p2Thread = new Thread(() -> {
            User user = getTestUser("other@example.com");

            try {
                this.kirvesService.action(0L, new GameIn(), user, 0);
            } catch (Exception e) {
                assertTrue(e instanceof TransactionException);
            }
        });

        p1Thread.start();
        Thread.sleep(1000);
        p2Thread.start();
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
