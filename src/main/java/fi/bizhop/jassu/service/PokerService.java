package fi.bizhop.jassu.service;

import fi.bizhop.jassu.exception.PokerGameException;
import fi.bizhop.jassu.models.PokerGame;
import fi.bizhop.jassu.models.PokerGameIn;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static fi.bizhop.jassu.models.PokerGame.Action.*;

@Service
public class PokerService {
    @Autowired
    UserService userService;

    private Map<Long, PokerGame> games = new HashMap<>();
    private Long sequence = 0L;

    public PokerGame newGame() {
        return newGameForPlayer("test@example.com");
    }

    public PokerGame newGameForPlayer(String email) {
        BigDecimal wager = BigDecimal.valueOf(1);
        this.userService.modifyMoney(wager.negate(), email);
        PokerGame game = new PokerGame(sequence++, wager);
        game.setPlayer(email);
        games.put(game.getGameId(), game);
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

    public PokerGame action(Long id, PokerGameIn in, String email) throws PokerGameException {
        PokerGame game = games.get(id);
        if(!email.equals(game.getPlayer())) {
            throw new PokerGameException("Not your game");
        }
        if(game.getAvailableActions().contains(in.getAction())) {
            if(in.getAction() == STAY) {
                game.stay(userService);
            }
            else if(in.getAction() == HOLD) {
                game.hold(in.getParameters());
            }
            else if(in.getAction() == DOUBLE_HIGH || in.getAction() == DOUBLE_LOW) {
                game.tryDouble(in.getAction(), userService);
            }
        }
        return game;
    }
}
