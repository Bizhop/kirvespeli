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
import fi.bizhop.jassu.model.kirves.out.GameBrief;
import fi.bizhop.jassu.model.kirves.pojo.GameDataPOJO;
import fi.bizhop.jassu.util.JsonUtil;
import fi.bizhop.jassu.util.TransactionHandler;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import static fi.bizhop.jassu.exception.KirvesGameException.Type.BAD_REQUEST;
import static fi.bizhop.jassu.exception.KirvesGameException.Type.UNAUTHORIZED;
import static fi.bizhop.jassu.exception.TransactionException.Type.TIMEOUT;
import static fi.bizhop.jassu.model.kirves.Game.Action.*;
import static java.util.stream.Collectors.toList;

@Service
public class KirvesService {
    private static final Logger LOG = LogManager.getLogger(KirvesService.class);

    private final UserService USER_SERVICE;
    private final KirvesGameRepo GAME_REPO;

    private final ActionLogRepo ACTION_LOG_REPO;
    private final ActionLogItemRepo ACTION_LOG_ITEM_REPO;

    private final Map<Long, Game> IN_MEMORY_GAMES = new ConcurrentHashMap<>();
    private final Map<String, ActionLog> IN_MEMORY_ACTION_LOGS = new HashMap<>();

    private final TransactionHandler TRANSACTION_HANDLER = new TransactionHandler();

    public KirvesService(UserService userService, KirvesGameRepo gameRepo, ActionLogRepo actionLogRepo, ActionLogItemRepo actionLogItemRepo) {
        this.USER_SERVICE = userService;
        this.GAME_REPO = gameRepo;
        this.ACTION_LOG_REPO = actionLogRepo;
        this.ACTION_LOG_ITEM_REPO = actionLogItemRepo;
    }

    public Long init(User admin) throws CardException, KirvesGameException, TransactionException {
        Game game = new Game(admin);
        LOG.info("Created new game");
        UserDB adminDB = this.USER_SERVICE.get(admin);
        KirvesGameDB db = new KirvesGameDB();
        db.admin = adminDB;
        db.active = true;
        db.players = game.getNumberOfPlayers();
        db.canJoin = true;
        db.gameData = game.toJson();

        Long id = this.GAME_REPO.save(db).id;
        LOG.info(String.format("New game saved with id=%d", id));
        db.id = id;
        this.IN_MEMORY_GAMES.put(id, game);
        this.TRANSACTION_HANDLER.registerGame(id);
        return id;
    }

    public List<GameBrief> getActiveGames() {
        List<KirvesGameDB> activeGames = this.GAME_REPO.findByActiveTrue();
        return activeGames == null
                ? Collections.emptyList()
                : activeGames.stream().map(GameBrief::new).collect(toList());
    }

    public void joinGame(Long id, User user) throws KirvesGameException, TransactionException {
        Game game = this.getGame(id);
        game.addPlayer(user);
        this.saveGame(id, game, null, user);
        LOG.info(String.format("Added player email=%s to game id=%d", user.getEmail(), id));
    }

    public Game getGame(Long id) throws KirvesGameException, TransactionException {
        Game fromMemory = this.IN_MEMORY_GAMES.get(id);
        if(fromMemory != null) return fromMemory;

        //game not found in memory, get game from db and register TransactionHandler
        KirvesGameDB game = this.getGameDB(id);
        GameDataPOJO pojo = JsonUtil.getJavaObject(game.gameData, GameDataPOJO.class)
                .orElseThrow(() -> new KirvesGameException("Muunnos json -> GameDataPOJO ei onnistunut"));
        Game deserializedGame = new Game(pojo);
        this.IN_MEMORY_GAMES.put(id, deserializedGame);
        this.TRANSACTION_HANDLER.registerGame(id);
        return deserializedGame;
    }

    private KirvesGameDB getGameDB(Long id) throws KirvesGameException {
        return this.GAME_REPO.findByIdAndActiveTrue(id)
                .orElseThrow(() -> new KirvesGameException(String.format("Peliä ei löytynyt, id=%d", id)));
    }

    private void sleep(long delay) throws InterruptedException {
        if(delay > 0) {
            Thread.sleep(delay);
        }
    }

    public Game action(Long id, GameIn in, User user) throws CardException, TransactionException, InterruptedException, KirvesGameException {
        return this.action(id, in, user, 0);
    }

    //Use delay only for testing transaction timeout
    public Game action(Long id, GameIn in, User user, long delay) throws KirvesGameException, TransactionException, InterruptedException, CardException {
        if(in.action == null) throw new KirvesGameException("Toiminto ei voi olla tyhjä (null)", BAD_REQUEST);
        Game game = this.getGame(id);
        try {
            this.TRANSACTION_HANDLER.begin(id, user, game.toJson());
        } catch (TransactionException e) {
            if(e.getType() == TIMEOUT) {
                this.IN_MEMORY_GAMES.put(id, new Game(this.TRANSACTION_HANDLER.rollback(id, GameDataPOJO.class)));
                this.TRANSACTION_HANDLER.begin(id, user, game.toJson());
            } else {
                throw e;
            }
        }
        this.sleep(delay);
        try {
            this.executeAction(in, user, game);
        } catch (Exception e) {
            this.IN_MEMORY_GAMES.put(id, new Game(this.TRANSACTION_HANDLER.rollback(id, GameDataPOJO.class)));
            throw e;
        }
        try {
            this.TRANSACTION_HANDLER.end(id);
            this.saveGame(id, game, in, user);
            return game;
        } catch (TransactionException e) {
            //ending transaction failed, probably for timeout. Log and rollback;
            LOG.warn(String.format("Ending transaction failed (id=%d, user=%s, message=%s), rolling back", id, user.getEmail(), e.getMessage()));
            this.IN_MEMORY_GAMES.put(id, new Game(this.TRANSACTION_HANDLER.rollback(id, GameDataPOJO.class)));
            throw new TransactionException(e.getType(), "Transaktion päättäminen epäonnistui. Edellinen tilanne palautettu.");
        }
    }

    private void executeAction(GameIn in, User user, Game game) throws KirvesGameException, CardException {
        if(!game.userHasActionAvailable(user, in.action)) {
            throw new KirvesGameException(String.format("Toiminto %s ei ole mahdollinen nyt", in.action), BAD_REQUEST);
        }
        switch (in.action) {
            case DEAL: game.deal(user); break;
            case PLAY_CARD: game.playCard(user, in.index); break;
            case FOLD: game.fold(user); break;
            case CUT: game.cut(user, in.declineCut); break;
            case ACE_OR_TWO_DECISION: game.aceOrTwoDecision(user, in.keepExtraCard); break;
            case SPEAK: game.speak(user, in.speak); break;
            case SPEAK_SUIT: game.speakSuit(user, in.suit); break;
            case DISCARD: game.discard(user, in.index); break;
        }
    }

    public void inactivateGame(Long id, User me) throws KirvesGameException, TransactionException {
        this.TRANSACTION_HANDLER.begin(id, me, null);

        KirvesGameDB game = this.getGameDB(id);
        if(me.getEmail().equals(game.admin.email)) {
            game.active = false;
            this.GAME_REPO.save(game);
            this.IN_MEMORY_GAMES.remove(id);
            LOG.info(String.format("Inactivated game id=%d", id));
        } else {
            throw new KirvesGameException(String.format("Et voi poistaa peliä, %s ei ole pelin omistaja (gameId=%d)", me.getNickname(), id), UNAUTHORIZED);
        }

        this.TRANSACTION_HANDLER.end(id);
    }

    private void saveGame(Long id, Game game, GameIn in, User user) throws KirvesGameException {
        KirvesGameDB gameDB = this.getGameDB(id);
        gameDB.gameData = game.toJson();
        gameDB.players = game.getNumberOfPlayers();
        gameDB.canJoin = game.getCanJoin();
        gameDB.lastHandId = game.getCurrentHandId();
        this.GAME_REPO.save(gameDB);
        this.IN_MEMORY_GAMES.put(id, game);

        if(in != null) {
            if (List.of(PLAY_CARD, FOLD, ACE_OR_TWO_DECISION, SPEAK, SPEAK_SUIT, DISCARD).contains(in.action)) {
                Long handId = game.getCurrentHandId();
                ActionLogItem actionLogItem = this.saveActionLogItem(in, user, id, handId, null);
                ActionLog inMemoryLog = this.IN_MEMORY_ACTION_LOGS.get(actionLogKey(id, handId));
                if(inMemoryLog == null) {
                    //log was not in memory
                    String key = actionLogKey(id, handId);
                    inMemoryLog = this.getActionLogFromDB(key);
                    this.IN_MEMORY_ACTION_LOGS.put(key, inMemoryLog);
                } else {
                    inMemoryLog.addItem(actionLogItem);
                }
            } else if (in.action == DEAL) {
                var ownerDB = this.USER_SERVICE.get(new User(game.getAdmin(), null));
                this.initializeActionLog(in, ownerDB, user, game, id);
            }
        }
    }

    private void initializeActionLog(GameIn in, UserDB ownerDB, User user, Game game, Long gameId) throws KirvesGameException {
        Long handId = game.incrementHandId();

        String initialState = game.toJson();
        String key = actionLogKey(gameId, handId);

        ActionLog actionLog = new ActionLog(initialState, ownerDB.email);

        ActionLogDB actionLogDB = new ActionLogDB();
        actionLogDB.key = key;
        actionLogDB.initialState = initialState;
        actionLogDB.owner = ownerDB;

        actionLogDB = this.ACTION_LOG_REPO.save(actionLogDB);
        ActionLogItem actionLogItem = this.saveActionLogItem(in, user, gameId, handId, actionLogDB);
        
        actionLog.addItem(actionLogItem);
        this.IN_MEMORY_ACTION_LOGS.put(actionLogKey(gameId, handId), actionLog);
        LOG.info(String.format("Action log initialized for gameId: %d, handId: %d", gameId, handId));
    }

    private ActionLogItem saveActionLogItem(GameIn in, User user, Long gameId, Long handId, ActionLogDB actionLogDB) throws KirvesGameException {
        String key = actionLogKey(gameId, handId);
        if(actionLogDB == null) {
            actionLogDB = this.ACTION_LOG_REPO.findById(key).orElseThrow();
        }

        ActionLogItem actionLogItem = ActionLogItem.of(user, in);

        UserDB userDB = this.USER_SERVICE.get(user);
        ActionLogItemDB actionLogItemDB = actionLogItem.getDB(userDB, actionLogDB);
        this.ACTION_LOG_ITEM_REPO.save(actionLogItemDB);

        return actionLogItem;
    }

    public ActionLog getActionLog(Long gameId, Long handId, User user) throws KirvesGameException {
        ActionLog actionLog = this.IN_MEMORY_ACTION_LOGS.get(actionLogKey(gameId, handId));
        if(actionLog == null) {
            String key = actionLogKey(gameId, handId);
            actionLog = this.getActionLogFromDB(key);
            this.IN_MEMORY_ACTION_LOGS.put(key, actionLog);
        }
        if(!user.getEmail().equals(actionLog.getOwner())) throw new KirvesGameException("You are not admin in this game", UNAUTHORIZED);

        return actionLog;
    }

    private ActionLog getActionLogFromDB(String key) throws KirvesGameException {
        ActionLogDB actionLogDB = this.ACTION_LOG_REPO.findById(key)
                .orElseThrow(() -> new KirvesGameException(String.format("Action log not found for id: %s", key)));
        return ActionLog.of(actionLogDB);
    }

    public Game getReplay(Long gameId, Long handId, int actionLogItemIndex, User user) throws KirvesGameException, CardException {
        var actionLog = this.getActionLog(gameId, handId, user);
        if(actionLogItemIndex >= actionLog.getItems().size()) {
            throw new KirvesGameException(String.format("gameId: %d handId: %d has no ActionLogItem for index %d", gameId, handId, actionLogItemIndex));
        }

        var pojo = JsonUtil.getJavaObject(actionLog.getInitialState(), GameDataPOJO.class)
                .orElseThrow(() -> new KirvesGameException("Muunnos json -> GameDataPOJO ei onnistunut"));
        var game = new Game(pojo);

        //start iteration from index 1, skippind DEAL
        for(int i = 1; i <= actionLogItemIndex; i++) {
            var item = actionLog.getItems().get(i);
            this.executeAction(item.getInput(), item.getUser(), game);
        }

        return game;
    }

    @Transactional
    public void restoreGame(Long id, Long handId, int actionLogItemIndex, User user) throws KirvesGameException, CardException, TransactionException {
        var game = this.getReplay(id, handId, actionLogItemIndex, user);

        this.TRANSACTION_HANDLER.begin(id, user, null);

        try {
            //save game
            this.saveGame(id, game, null, user);

            //clear items from in-memory action log
            var key = actionLogKey(id, handId);
            var actionLog = this.IN_MEMORY_ACTION_LOGS.get(key);
            actionLog.removeItemsAfter(actionLogItemIndex);

            //clear items from db action log
            var actionLogDB = this.ACTION_LOG_REPO.findById(key).orElseThrow();
            this.ACTION_LOG_ITEM_REPO.deleteByActionLog(actionLogDB);

            //add in-memory items to db
            var userDB = this.USER_SERVICE.get(user);
            var itemDBs = actionLog.getItems().stream()
                    .map(item -> {
                        try {
                            return item.getDB(userDB, actionLogDB);
                        } catch (KirvesGameException e) {
                            return null;
                        }
                    })
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList());
            this.ACTION_LOG_ITEM_REPO.saveAll(itemDBs);
        } catch (Exception e) {
            LOG.error("Failed to restore game", e);
            this.TRANSACTION_HANDLER.end(id);
            throw new KirvesGameException("Failed to restore game");
        }

        this.TRANSACTION_HANDLER.end(id);
    }

    private static String actionLogKey(Long gameId, Long handId) {
        return String.format("%d-%d", gameId, handId);
    }
}
