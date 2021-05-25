package fi.bizhop.jassu.service;

import fi.bizhop.jassu.db.KirvesGameDB;
import fi.bizhop.jassu.db.KirvesGameRepo;
import fi.bizhop.jassu.db.UserDB;
import fi.bizhop.jassu.exception.CardException;
import fi.bizhop.jassu.exception.KirvesGameException;
import fi.bizhop.jassu.exception.TransactionException;
import fi.bizhop.jassu.model.User;
import fi.bizhop.jassu.model.kirves.Game;
import fi.bizhop.jassu.model.kirves.Transaction;
import fi.bizhop.jassu.model.kirves.in.GameIn;
import fi.bizhop.jassu.model.kirves.out.GameBrief;
import fi.bizhop.jassu.model.kirves.pojo.GameDataPOJO;
import fi.bizhop.jassu.util.JsonUtil;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static fi.bizhop.jassu.exception.TransactionException.Type.TIMEOUT;
import static fi.bizhop.jassu.model.kirves.Game.Action.*;
import static java.util.stream.Collectors.toList;

@Service
public class KirvesService {
    private static final Logger LOG = LogManager.getLogger(KirvesService.class);

    private final UserService userService;
    private final KirvesGameRepo kirvesGameRepo;

    private final Map<Long, Game> inMemoryGames = new ConcurrentHashMap<>();

    private final Transaction TX = new Transaction();

    public KirvesService(UserService userService, KirvesGameRepo kirvesGameRepo) {
        this.userService = userService;
        this.kirvesGameRepo = kirvesGameRepo;
    }

    public Long newGameForAdmin(User admin) throws CardException, KirvesGameException {
        Game game = new Game(admin);
        LOG.info("Created new game");
        UserDB adminDB = this.userService.get(admin);
        KirvesGameDB db = new KirvesGameDB();
        db.admin = adminDB;
        db.active = true;
        db.players = game.getNumberOfPlayers();
        db.canJoin = true;
        db.gameData = game.toJson();

        Long id = kirvesGameRepo.save(db).id;
        LOG.info(String.format("New game saved with id=%d", id));
        db.id = id;
        this.inMemoryGames.put(id, game);
        return id;
    }

    public List<GameBrief> getActiveGames() {
        List<KirvesGameDB> activeGames = kirvesGameRepo.findByActiveTrue();
        if(activeGames != null) {
            return activeGames.stream().map(GameBrief::new).collect(toList());
        } else {
            return Collections.emptyList();
        }
    }

    public void joinGame(Long id, String email) throws KirvesGameException, CardException {
        User player = this.userService.get(email);
        if(player == null) {
            throw new KirvesGameException("Tunnusta ei löytynyt");
        } else {
            Game game = this.getGame(id);
            game.addPlayer(player);
            saveGame(id, game);
            LOG.info(String.format("Added player email=%s to game id=%d", player.getEmail(), id));
        }
    }

    public Game getGame(Long id) throws KirvesGameException, CardException {
        Game fromMemory = this.inMemoryGames.get(id);
        if(fromMemory != null) {
            return fromMemory;
        }
        KirvesGameDB game = this.getGameDB(id);
        GameDataPOJO pojo = JsonUtil.getJavaObject(game.gameData, GameDataPOJO.class)
                .orElseThrow(() -> new KirvesGameException("Unable to convert gameData to KirvesGame"));
        Game deserializedGame = new Game(pojo);
        this.inMemoryGames.put(id, deserializedGame);
        return deserializedGame;
    }

    private KirvesGameDB getGameDB(Long id) throws KirvesGameException {
        return kirvesGameRepo.findByIdAndActiveTrue(id)
                .orElseThrow(() -> new KirvesGameException(String.format("Peliä ei löytynyt, id=%d", id)));
    }

    private void sleep(long delay) throws InterruptedException {
        if(delay > 0) {
            Thread.sleep(delay);
        }
    }

    //Use delay only for testing transaction timeout
    public Game action(Long id, GameIn in, User user, long delay) throws KirvesGameException, CardException, TransactionException, InterruptedException {
        Game game = this.getGame(id);
        try {
            this.TX.begin(user, game.toJson());
        } catch (TransactionException e) {
            if(e.getType() == TIMEOUT) {
                this.inMemoryGames.put(id, new Game(this.TX.rollback()));
                this.TX.begin(user, game.toJson());
            } else {
                throw e;
            }
        }
        this.TX.check(user);
        sleep(delay);
        try {
            if (in.action == DEAL) {
                if (game.userHasActionAvailable(user, DEAL)) {
                    try {
                        game.deal(user);
                    } catch (CardException e) {
                        throw new KirvesGameException(String.format("Jako ei onnistunut: %s", e.getMessage()));
                    }
                } else {
                    throw new KirvesGameException("Et voi jakaa nyt (DEAL)");
                }
            } else if (in.action == PLAY_CARD) {
                if (game.userHasActionAvailable(user, PLAY_CARD)) {
                    try {
                        game.playCard(user, in.index);
                    } catch (CardException e) {
                        throw new KirvesGameException(String.format("Kortin pelaaminen ei onnistunut (index=%d)", in.index));
                    }
                } else {
                    throw new KirvesGameException("Ei ole vuorosi pelata (PLAY_CARD)");
                }
            } else if (in.action == CUT) {
                if (game.userHasActionAvailable(user, CUT)) {
                    try {
                        game.cut(user, in.declineCut);
                    } catch (CardException e) {
                        throw new KirvesGameException("Nosto ei onnistunut (CUT)");
                    }
                } else {
                    throw new KirvesGameException("Et voi nostaa nyt (CUT)");
                }
            } else if (in.action == DISCARD) {
                if (game.userHasActionAvailable(user, DISCARD)) {
                    try {
                        game.discard(user, in.index);
                    } catch (CardException e) {
                        throw new KirvesGameException(String.format("Tyhjennys ei onnistunut (DISCARD, index=%d)", in.index));
                    }
                } else {
                    throw new KirvesGameException("Et voi tyhjentää nyt (DISCARD)");
                }
            } else if (in.action == ACE_OR_TWO_DECISION) {
                if (game.userHasActionAvailable(user, ACE_OR_TWO_DECISION)) {
                    game.aceOrTwoDecision(user, in.keepExtraCard);
                } else {
                    throw new KirvesGameException("Et voi tehdä hakkipäätöstä nyt (ACE_OR_TWO_DECISION)");
                }
            } else if (in.action == SPEAK) {
                if (game.userHasActionAvailable(user, SPEAK)) {
                    game.speak(user, in.speak);
                } else {
                    throw new KirvesGameException("Et voi puhua nyt (SPEAK)");
                }
            } else if (in.action == SPEAK_SUIT) {
                if (game.userHasActionAvailable(user, SPEAK_SUIT)) {
                    game.speakSuit(user, in.valtti);
                } else {
                    throw new KirvesGameException("Et voi puhua nyt (SPEAK)");
                }
            } else if (in.action == FOLD) {
                if (game.userHasActionAvailable(user, FOLD)) {
                    game.fold(user);
                } else {
                    throw new KirvesGameException("Et voi mennä pakkaan nyt (FOLD)");
                }
            }
        } catch (KirvesGameException e) {
            this.inMemoryGames.put(id, new Game(this.TX.rollback()));
            throw e;
        }
        try {
            this.TX.end();
        } catch (TransactionException e) {
            //ending transaction failed, probably for timeout. Log and rollback;
            LOG.warn(String.format("Ending transaction failed (id=%d, user=%s, message=%s), rolling back", id, user.getEmail(), e.getMessage()));
            this.inMemoryGames.put(id, new Game(this.TX.rollback()));
            throw new TransactionException(e.getType(), "Ending transaction failed, rolled back to previous state");
        }
        saveGame(id, game);
        return game;
    }

    public void inactivateGame(Long id, User me) throws KirvesGameException {
        KirvesGameDB game = this.getGameDB(id);
        if(me.getEmail().equals(game.admin.email)) {
            game.active = false;
            this.kirvesGameRepo.save(game);
            LOG.info(String.format("Inactivated game id=%d", id));
        } else {
            throw new KirvesGameException(String.format("Et voi poistaa peliä, %s ei ole pelin omistaja (gameId=%d)", me.getNickname(), id));
        }
    }

    private void saveGame(Long id, Game game) throws KirvesGameException {
        KirvesGameDB gameDB = this.getGameDB(id);
        gameDB.gameData = game.toJson();
        gameDB.players = game.getNumberOfPlayers();
        gameDB.canJoin = game.getCanJoin();
        this.kirvesGameRepo.save(gameDB);
        this.inMemoryGames.put(id, game);
    }
}
