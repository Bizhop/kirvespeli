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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

import static fi.bizhop.jassu.model.kirves.Game.Action.*;
import static fi.bizhop.jassu.util.TestUserUtil.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(SpringExtension.class)
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

    Map<Integer, Exception> threadExceptions = new HashMap<>();

    @Captor
    ArgumentCaptor<List<ActionLogItemDB>> actionLogItemsCaptor;

    @BeforeEach
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

        var user = getTestUser();

        var input = GameIn.builder()
                .action(CUT)
                .declineCut(false)
                .build();

        var output = this.kirvesService.action(0L, input, user, 0);

        //verify data changed
        var originalDB = getTestGameDB();
        var originalPojo = JsonUtil.getJavaObject(originalDB.gameData, GameDataPOJO.class).orElse(null);
        assertNotNull(originalPojo);

        var json = output.toJson();
        var pojo = JsonUtil.getJavaObject(json, GameDataPOJO.class).orElseThrow();
        assertNotEquals(originalPojo, pojo);
    }

    @Test
    public void testTransactionTimeout() throws IOException, CardException, KirvesGameException, InterruptedException, TransactionException {
        when(this.kirvesGameRepo.findByIdAndActiveTrue(eq(0L))).thenReturn(Optional.of(getTestGameDB()));

        var user = getTestUser();

        var input = GameIn.builder()
                .action(CUT)
                .declineCut(false)
                .build();

        try {
            this.kirvesService.action(0L, input, user, 6 * 1000);
        } catch (TransactionException e) {
            //verify data didn't change
            var originalDB = getTestGameDB();
            var originalPojo = JsonUtil.getJavaObject(originalDB.gameData, GameDataPOJO.class).orElse(null);
            assertNotNull(originalPojo);

            //make sure you get the inMemoryGame by checking the call wasn't made on DB
            verify(this.kirvesGameRepo, times(1)).findByIdAndActiveTrue(any());
            var game = this.kirvesService.getGame(0L);
            verify(this.kirvesGameRepo, times(1)).findByIdAndActiveTrue(any());

            var json = game.toJson();
            var pojo = JsonUtil.getJavaObject(json, GameDataPOJO.class).orElseThrow();
            assertEquals(originalPojo, pojo);
            return;
        }

        //expecting tx timeout, so reaching here is fail
        fail();
    }

    @Test
    public void testTransactionLock() throws IOException, InterruptedException {
        when(this.kirvesGameRepo.findByIdAndActiveTrue(eq(0L))).thenReturn(Optional.of(getTestGameDB()));

        var p1Thread = new Thread(() -> {
            var user = getTestUser();

            var input = GameIn.builder()
                    .action(CUT)
                    .declineCut(false)
                    .build();

            try {
                this.kirvesService.action(0L, input, user, 3 * 1000);
            } catch (Exception e) {
                this.threadExceptions.put(1, e);
            }
        });

        var p2Thread = new Thread(() -> {
            var user = getTestUser("other@mock.com");

            try {
                var input = GameIn.builder()
                        .action(CUT)
                        .declineCut(false)
                        .build();

                this.kirvesService.action(0L, input, user, 0);
            } catch (Exception e) {
                this.threadExceptions.put(2, e);
            }
        });

        p1Thread.start();
        Thread.sleep(1000);
        p2Thread.start();

        p1Thread.join();
        p2Thread.join();

        assertNull(this.threadExceptions.get(1));
        assertTrue(this.threadExceptions.get(2) instanceof TransactionException);
    }

    @Test
    public void testActionLog() throws IOException, CardException, TransactionException, InterruptedException, KirvesGameException {
        when(this.kirvesGameRepo.findByIdAndActiveTrue(eq(0L))).thenReturn(Optional.of(getTestGameDB()));
        when(this.actionLogRepo.findById(eq("0-0"))).thenReturn(Optional.of(getTestActionLog("0-0")));

        User user1 = getTestUser();
        User user2 = getTestUser("other@mock.com");

        when(this.userService.get(eq(user1))).thenReturn(new UserDB(user1));
        when(this.userService.get(eq(user2))).thenReturn(new UserDB(user2));
        when(this.userService.get(eq(TEST_USER_EMAIL))).thenReturn(user1);

        var cut = GameIn.builder()
                .action(CUT)
                .declineCut(false)
                .build();

        this.kirvesService.action(0L, cut, user1);
        Thread.sleep(1000);

        var deal = GameIn.builder()
                .action(DEAL)
                .build();

        Game game = this.kirvesService.action(0L, deal, user2);

        ActionLog actionLog = this.kirvesService.getActionLog(0L, 0L, user1);
        System.out.println(actionLog);
        System.out.println("----------");

        assertNotNull(actionLog.getInitialState());

        assertEquals(1, actionLog.getItems().size());
        ActionLogItem item = actionLog.getItems().get(0);
        assertEquals("other@mock.com", item.getUser().getEmail());
        assertEquals(DEAL, item.getInput().getAction());

        var nextActionBuilder = GameIn.builder();
        var nextUser = user1;

        if(game.userHasActionAvailable(user1, SPEAK)) {
            nextActionBuilder.action(SPEAK).speak(Game.Speak.KEEP);
        } else if(game.userHasActionAvailable(user1, DISCARD)) {
            nextActionBuilder.action(DISCARD).index(0);
        } else if(game.userHasActionAvailable(user1, CUT)) {
            nextActionBuilder.action(CUT).declineCut(false);
        }
        else if(game.userHasActionAvailable(user2, ACE_OR_TWO_DECISION)) {
            nextActionBuilder.action(ACE_OR_TWO_DECISION).keepExtraCard(true);
            nextUser = user2;
        } else if(game.userHasActionAvailable(user2, DISCARD)) {
            nextActionBuilder.action(DISCARD).index(0);
            nextUser = user2;
        }

        var nextAction = nextActionBuilder.build();
        System.out.println("Next action: " + nextAction.getAction());
        System.out.println("Next user: " + nextUser.getEmail());

        this.kirvesService.action(0L, nextAction, nextUser);

        actionLog = this.kirvesService.getActionLog(0L, 0L, user1);
        System.out.println(actionLog);
        System.out.println("----------");

        assertNotNull(actionLog.getInitialState());

        assertEquals(2, actionLog.getItems().size());
        item = actionLog.getItems().get(1);
        assertEquals(nextUser.getEmail(), item.getUser().getEmail());
        assertEquals(nextAction.getAction(), item.getInput().getAction());

        //verify that action log was saved once
        verify(this.actionLogRepo, times(1)).save(any());
        //verify that action log item was saved twice
        verify(this.actionLogItemRepo, times(2)).save(any());
    }

    @Test
    public void testActionLogCache() throws KirvesGameException, IOException {
        when(this.actionLogRepo.findById(any())).thenReturn(Optional.of(getTestActionLog("0-0")));
        User user = getTestUser();

        ActionLog actionLog = this.kirvesService.getActionLog(0L, 0L, user);
        System.out.println(actionLog);

        this.kirvesService.getActionLog(0L,0L, user);

        verify(this.actionLogRepo, times(1)).findById(any());
    }

    @Test
    public void testReplay() throws IOException, KirvesGameException, CardException {
        this.initializeTestActionLog();

        var user = getTestUser();
        var replay = this.kirvesService.getReplay(0L, 0L, 0, user);

        assertEquals(2, replay.getNumberOfPlayers());

        var replay2 = this.kirvesService.getReplay(0L,0L,4, user);
        var player = replay2.getPlayer(TEST_USER_EMAIL).orElseThrow();
        var player2 = replay2.getPlayer("other@mock.com").orElseThrow();

        assertEquals(4, player.cardsInHand());
        assertEquals(4, player2.cardsInHand());

        try {
            this.kirvesService.getReplay(0L, 0L, 15, user);
        } catch (Exception e) {
            assertEquals(e.getMessage(), "gameId: 0 handId: 0 has no ActionLogItem for index 15");
        }
    }

    @Test
    public void testRestoreGame() throws IOException, TransactionException, CardException, KirvesGameException {
        this.initializeTestActionLog();

        var testGameDB = getTestGameDB();
        when(this.kirvesGameRepo.save(any())).thenReturn(testGameDB);
        when(this.kirvesGameRepo.findByIdAndActiveTrue(any())).thenReturn(Optional.of(testGameDB));
        var user = getTestUser();
        var id = this.kirvesService.init(user);
        assertEquals(0, id);

        when(this.userService.get(eq(user))).thenReturn(getTestUserDB());
        this.kirvesService.restoreGame(0L, 0L, 4, user);
        var game = this.kirvesService.getGame(0L);

        var player = game.getPlayer(TEST_USER_EMAIL).orElseThrow();
        var player2 = game.getPlayer("other@mock.com").orElseThrow();

        assertEquals(4, player.cardsInHand());
        assertEquals(4, player2.cardsInHand());

        verify(this.actionLogItemRepo, times(1)).deleteByActionLog(any());
        verify(this.actionLogItemRepo).saveAll(this.actionLogItemsCaptor.capture());
        assertEquals(4, this.actionLogItemsCaptor.getValue().size());
    }

    private void initializeTestActionLog() throws IOException {
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
    }

    private static KirvesGameDB getTestGameDB() throws IOException {
        var db = new KirvesGameDB();
        db.id = 0L;
        db.admin = getTestUserDB();
        db.canJoin = true;
        db.active = true;
        db.players = 4;
        db.gameData = FileUtils.readFileToString(new File("src/test/resources/testData.json"), "UTF-8");
        return db;
    }

    private static ActionLogDB getTestActionLog(String key) throws IOException {
        var db = new ActionLogDB();
        db.key = key;
        db.owner = getTestUserDB();
        db.initialState = FileUtils.readFileToString(new File("src/test/resources/actionLogInitialState.json"), "UTF-8");

        var item = new ActionLogItemDB();
        item.actionLog = db;
        item.user = getTestUserDB();
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
