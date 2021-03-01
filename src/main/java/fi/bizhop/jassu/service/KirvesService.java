package fi.bizhop.jassu.service;

import fi.bizhop.jassu.exception.CardException;
import fi.bizhop.jassu.exception.KirvesGameException;
import fi.bizhop.jassu.model.KirvesGame;
import fi.bizhop.jassu.model.KirvesGameIn;
import fi.bizhop.jassu.model.User;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static fi.bizhop.jassu.model.KirvesGame.Action.DEAL;
import static fi.bizhop.jassu.model.KirvesGame.Action.PLAY_CARD;
import static java.util.stream.Collectors.toList;

@Service
public class KirvesService {
    private static final Logger LOG = LogManager.getLogger(KirvesService.class);

    final
    UserService userService;

    private Map<Long, KirvesGame> games = new HashMap<>();
    private Long sequence = 0L;

    public KirvesService(UserService userService) {
        this.userService = userService;
    }

    public KirvesGame newGameForAdmin(User admin) throws CardException {
        Long id = this.sequence;
        KirvesGame game = new KirvesGame(admin, id);
        this.games.put(id, game);
        LOG.info(String.format("Created new game, id=%d", id));
        this.sequence++;
        return game;
    }

    public List<KirvesGame> getActiveGames() {
        return this.games.values().stream()
                .filter(KirvesGame::isActive)
                .collect(toList());
    }

    public void joinGame(Long id, String email) throws KirvesGameException {
        User player = this.userService.get(email);
        if(player == null) {
            throw new KirvesGameException("Email not found");
        } else {
            KirvesGame game = this.games.get(id);
            if(game == null || !game.isActive()) {
                throw new KirvesGameException("Game not found");
            } else {
                game.addPlayer(player);
                LOG.info(String.format("Added player email=%s to game id=%d", player.getEmail(), id));
            }
        }
    }

    public KirvesGame getGame(Long id) throws KirvesGameException {
        KirvesGame game = this.games.get(id);
        if(game == null) {
            throw new KirvesGameException("Game not found");
        } else {
            return game;
        }
    }

    public KirvesGame action(Long id, KirvesGameIn in, User user) throws KirvesGameException{
        KirvesGame game = this.getGame(id);
        if(in.action == DEAL) {
            if(game.userHasActionAvailable(user, DEAL)) {
                try {
                    game.deal(user);
                } catch (CardException e) {
                    throw new KirvesGameException(String.format("Unable to deal cards: %s", e.getMessage()));
                }
            } else {
                game.setMessage("You can't deal now");
            }
        }
        if(in.action == PLAY_CARD) {
            if(game.userHasActionAvailable(user, PLAY_CARD)) {
                try {
                    game.playCard(user, in.index);
                } catch (CardException e) {
                    game.setMessage(String.format("Unable to PLAY_CARD with index %d", in.index));
                }
            }
            else {
                game.setMessage("It's not your turn to PLAY_CARD");
            }
        }
        return game;
    }
}
