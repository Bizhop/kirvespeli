package fi.bizhop.jassu.service;

import fi.bizhop.jassu.exception.CardException;
import fi.bizhop.jassu.exception.PokerGameException;
import fi.bizhop.jassu.model.Cards;
import fi.bizhop.jassu.model.User;
import fi.bizhop.jassu.model.poker.PokerGame;
import fi.bizhop.jassu.model.poker.PokerGameIn;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static fi.bizhop.jassu.model.poker.PokerGame.Action.*;

@Service
public class PokerService {
    private static final Logger LOG = LogManager.getLogger(PokerService.class);

    final UserService userService;

    private final Map<Long, PokerGame> games = new HashMap<>();
    private Long sequence = 0L;

    public PokerService(UserService userService) {
        this.userService = userService;
    }

    public PokerGame newGame() throws CardException {
        return this.newGameForPlayer(new User("test@example.com",""));
    }


    public PokerGame newGameForPlayer(User user) throws CardException {
        return this.newGameForPlayer(user, null);
    }

    //use this directly for testing only
    public PokerGame newGameForPlayer(User user, Cards hand) throws CardException {
        BigDecimal wager = BigDecimal.valueOf(1);
        this.userService.modifyMoney(wager.negate(), user.getEmail());
        PokerGame game = new PokerGame(wager);
        game.setPlayer(user.getEmail());
        game.deal(hand);
        this.games.put(this.sequence++, game);
        return game;
    }

    public PokerGame getGame(Long id, User user) throws PokerGameException {
        PokerGame game = this.games.get(id);
        if(game == null) {
            throw new PokerGameException(String.format("No game with id: %d", id));
        }
        else if(!user.getEmail().equals(game.getPlayer())) {
            throw new PokerGameException("Not your game");
        }
        else {
            return game;
        }
    }

    public List<PokerGame> getGames(User user) {
        return this.games.values().stream()
                .filter(game -> user.getEmail().equals(game.getPlayer()))
                .filter(PokerGame::active)
                .collect(Collectors.toList());
    }

    public PokerGame action(Long id, PokerGameIn in, User user) throws PokerGameException, CardException {
        PokerGame game = this.games.get(id);
        if(!user.getEmail().equals(game.getPlayer())) {
            throw new PokerGameException("Not your game");
        }
        if(game.getAvailableActions().contains(in.action)) {
            if(in.action == STAY) {
                game.stay(this.userService);
            }
            else if(in.action == HOLD) {
                game.hold(in.parameters);
            }
            else if(in.action == DOUBLE_HIGH || in.action == DOUBLE_LOW) {
                game.tryDouble(in.action, this.userService);
            }
        }
        return game;
    }
}
