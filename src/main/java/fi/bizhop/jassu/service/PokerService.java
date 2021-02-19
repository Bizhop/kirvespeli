package fi.bizhop.jassu.service;

import fi.bizhop.jassu.exception.CardException;
import fi.bizhop.jassu.exception.PokerGameException;
import fi.bizhop.jassu.model.PokerGame;
import fi.bizhop.jassu.model.PokerGameIn;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static fi.bizhop.jassu.model.PokerGame.Action.*;

@Service
public class PokerService {
    private static final Logger LOG = LogManager.getLogger(PokerService.class);

    final
    UserService userService;

    private Map<Long, PokerGame> games = new HashMap<>();
    private Long sequence = 0L;

    public PokerService(UserService userService) {
        this.userService = userService;
    }

    public PokerGame newGame() throws CardException {
        return newGameForPlayer("test@example.com");
    }

    public PokerGame newGameForPlayer(String email) throws CardException {
        BigDecimal wager = BigDecimal.valueOf(1);
        this.userService.modifyMoney(wager.negate(), email);
        PokerGame game = new PokerGame(wager);
        game.setPlayer(email);
        games.put(sequence++, game);
        return game;
    }

    public PokerGame getGame(Long id, String email) throws PokerGameException {
        PokerGame game = games.get(id);
        if(game == null) {
            throw new PokerGameException(String.format("No game with id: %d", id));
        }
        else if(!email.equals(game.getPlayer())) {
            throw new PokerGameException(String.format("Not your game"));
        }
        else {
            return game;
        }
    }

    public List<PokerGame> getGames(String email) {
        return games.values().stream()
                .filter(game -> email.equals(game.getPlayer()))
                .filter(PokerGame::active)
                .collect(Collectors.toList());
    }

    public PokerGame action(Long id, PokerGameIn in, String email) throws PokerGameException, CardException {
        PokerGame game = games.get(id);
        if(!email.equals(game.getPlayer())) {
            throw new PokerGameException("Not your game");
        }
        if(game.getAvailableActions().contains(in.action)) {
            if(in.action == STAY) {
                game.stay(userService);
            }
            else if(in.action == HOLD) {
                game.hold(in.parameters);
            }
            else if(in.action == DOUBLE_HIGH || in.action == DOUBLE_LOW) {
                game.tryDouble(in.action, userService);
            }
        }
        return game;
    }

    public void dummy() {
        System.out.println("Dummy");
    }
}
