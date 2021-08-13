package fi.bizhop.jassu.service;

import fi.bizhop.jassu.db.*;
import fi.bizhop.jassu.exception.CardException;
import fi.bizhop.jassu.exception.KirvesGameException;
import fi.bizhop.jassu.exception.TransactionException;
import fi.bizhop.jassu.model.User;
import fi.bizhop.jassu.model.kirves.ActionLog;
import fi.bizhop.jassu.model.kirves.ActionLogItem;
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
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import static fi.bizhop.jassu.model.kirves.Game.Action.*;
import static fi.bizhop.jassu.util.TestUserUtil.*;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

@RunWith(SpringRunner.class)
public class KirvesServiceTest {
    @MockBean
    KirvesGameRepo kirvesGameRepo;

    KirvesService kirvesService;

    @MockBean
    ActionLogRepo actionLogRepo;

    @MockBean
    ActionLogItemRepo actionLogItemRepo;

    @MockBean
    UserService userService;

    @Before
    public void setup() {
        this.kirvesService = new KirvesService(this.userService, this.kirvesGameRepo, this.actionLogRepo, this.actionLogItemRepo);
    }

    @Test
    public void testInMemoryGames() throws CardException, KirvesGameException, IOException, TransactionException {
        when(this.kirvesGameRepo.findByIdAndActiveTrue(eq(0L))).thenReturn(Optional.of(getTestGameDB()));

        this.kirvesService.getGame(0L);
        this.kirvesService.getGame(0L);

        //verify that db was called just once
        verify(this.kirvesGameRepo, times(1)).findByIdAndActiveTrue(any());
    }

    @Test
    public void testTransactionSuccess() throws IOException, TransactionException, CardException, KirvesGameException, InterruptedException {
        when(this.kirvesGameRepo.findByIdAndActiveTrue(eq(0L))).thenReturn(Optional.of(getTestGameDB()));

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
    public void testTransactionTimeout() throws IOException, CardException, KirvesGameException, InterruptedException, TransactionException {
        when(this.kirvesGameRepo.findByIdAndActiveTrue(eq(0L))).thenReturn(Optional.of(getTestGameDB()));

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
            verify(this.kirvesGameRepo, times(1)).findByIdAndActiveTrue(any());
            Game game = this.kirvesService.getGame(0L);
            verify(this.kirvesGameRepo, times(1)).findByIdAndActiveTrue(any());

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
        when(this.kirvesGameRepo.findByIdAndActiveTrue(eq(0L))).thenReturn(Optional.of(getTestGameDB()));

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
            User user = getTestUser("other@mock.com");

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

    @Test
    public void testActionLog() throws IOException, CardException, TransactionException, InterruptedException, KirvesGameException {
        when(this.kirvesGameRepo.findByIdAndActiveTrue(eq(0L))).thenReturn(Optional.of(getTestGameDB()));
        when(this.actionLogRepo.findById(eq("0-0"))).thenReturn(Optional.of(getTestActionLog("0-0")));

        User user1 = getTestUser();
        User user2 = getTestUser("other@mock.com");

        when(this.userService.get(eq(user1))).thenReturn(new UserDB(user1));
        when(this.userService.get(eq(user2))).thenReturn(new UserDB(user2));

        GameIn cut = new GameIn();
        cut.action = CUT;
        cut.declineCut = false;

        this.kirvesService.action(0L, cut, user1);
        Thread.sleep(1000);

        GameIn deal = new GameIn();
        deal.action = DEAL;

        Game game = this.kirvesService.action(0L, deal, user2);

        ActionLog actionLog = this.kirvesService.getActionLog(0L, 0L);
        System.out.println(actionLog);
        System.out.println("----------");

        assertNotNull(actionLog.getInitialState());

        assertEquals(1, actionLog.getItems().size());
        ActionLogItem item = actionLog.getItems().get(0);
        assertEquals("other@mock.com", item.getUser().getEmail());
        assertEquals(DEAL, item.getInput().action);

        GameIn nextAction = new GameIn();
        User nextUser = user1;

        if(game.userHasActionAvailable(user1, SPEAK)) {
            nextAction.action = SPEAK;
            nextAction.speak = Game.Speak.KEEP;
        } else if(game.userHasActionAvailable(user1, DISCARD)) {
            nextAction.action = DISCARD;
            nextAction.index = 0;
        } else if(game.userHasActionAvailable(user1, CUT)) {
            nextAction.action = CUT;
            nextAction.declineCut = true;
        }
        else if(game.userHasActionAvailable(user2, ACE_OR_TWO_DECISION)) {
            nextAction.action = ACE_OR_TWO_DECISION;
            nextAction.keepExtraCard = true;
            nextUser = user2;
        } else if(game.userHasActionAvailable(user2, DISCARD)) {
            nextAction.action = DISCARD;
            nextAction.index = 0;
            nextUser = user2;
        }

        System.out.println("Next action: " + nextAction.action);
        System.out.println("Next user: " + nextUser.getEmail());

        this.kirvesService.action(0L, nextAction, nextUser);

        actionLog = this.kirvesService.getActionLog(0L, 0L);
        System.out.println(actionLog);
        System.out.println("----------");

        assertNotNull(actionLog.getInitialState());

        assertEquals(2, actionLog.getItems().size());
        item = actionLog.getItems().get(1);
        assertEquals(nextUser.getEmail(), item.getUser().getEmail());
        assertEquals(nextAction.action, item.getInput().action);

        //verify that action log was saved once
        verify(this.actionLogRepo, times(1)).save(any());
        //verify that action log item was saved twice
        verify(this.actionLogItemRepo, times(2)).save(any());
    }

    @Test
    public void testActionLogCache() throws KirvesGameException, IOException {
        when(this.actionLogRepo.findById(any())).thenReturn(Optional.of(getTestActionLog("0-0")));

        ActionLog actionLog = this.kirvesService.getActionLog(0L, 0L);
        System.out.println(actionLog);

        this.kirvesService.getActionLog(0L,0L);

        verify(this.actionLogRepo, times(1)).findById(any());
    }

    @Test
    public void testReplay() throws IOException, KirvesGameException, CardException {
        var actionLogDB = getTestActionLog("0-0");
        var itemsJson = FileUtils.readFileToString(new File("src/test/resources/actionLogItems.json"), "UTF-8");
        var actionLogItems = JsonUtil.getJavaObject(itemsJson, ActionLogItems.class).orElseThrow();
        var itemDBs = actionLogItems.items.stream()
                .map(item -> {
                    var user = getTestUserDB(item.getUser().getEmail());
                    try {
                        return item.getDB(user, actionLogDB);
                    } catch (KirvesGameException e) {
                        return null;
                    }
                })
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
        actionLogDB.items.addAll(itemDBs);
        when(this.actionLogRepo.findById(any())).thenReturn(Optional.of(actionLogDB));

        var replay = this.kirvesService.getReplay(0L, 0L, 0);

        assertEquals(2, replay.getNumberOfPlayers());

        var replay2 = this.kirvesService.getReplay(0L,0L,4);
        var player = replay2.getPlayer(TEST_USER_EMAIL).orElseThrow();
        var player2 = replay2.getPlayer("other@mock.com").orElseThrow();

        assertEquals(4, player.cardsInHand());
        assertEquals(4, player2.cardsInHand());

        try {
            this.kirvesService.getReplay(0L, 0L, 15);
        } catch (Exception e) {
            assertEquals(e.getMessage(), "gameId: 0 handId: 0 has no ActionLogItem for index 15");
        }
    }

    private static KirvesGameDB getTestGameDB() throws IOException {
        KirvesGameDB db = new KirvesGameDB();
        db.id = 0L;
        db.admin = getTestUserDB();
        db.canJoin = true;
        db.active = true;
        db.players = 4;
        db.gameData = FileUtils.readFileToString(new File("src/test/resources/testData.json"), "UTF-8");
        return db;
    }

    private static ActionLogDB getTestActionLog(String key) throws IOException {
        ActionLogDB db = new ActionLogDB();
        db.key = key;
        db.initialState = FileUtils.readFileToString(new File("src/test/resources/actionLogInitialState.json"), "UTF-8");

        ActionLogItemDB item = new ActionLogItemDB();
        item.actionLog = db;
        item.user = getTestUserDB(TEST_USER_EMAIL);
        item.input = "{\"action\":\"DEAL\",\"index\":0,\"keepExtraCard\":false,\"suit\":null,\"declineCut\":false,\"speak\":null}";

        db.items = new ArrayList<>();
        db.items.add(item);

        return db;
    }

    static class ActionLogItems {
        public List<ActionLogItem> items;

        public ActionLogItems() {}
    }
}
