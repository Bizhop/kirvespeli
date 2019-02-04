package fi.bizhop.jassu.service;

import fi.bizhop.jassu.exception.PokerGameException;
import fi.bizhop.jassu.models.PokerGame;
import fi.bizhop.jassu.models.PokerGameIn;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

import static fi.bizhop.jassu.models.PokerGame.Action.HOLD;
import static fi.bizhop.jassu.models.PokerGame.Action.STAY;

@Service
public class PokerService {
    private Map<Long, PokerGame> games = new HashMap<>();
    private Long sequence = 0L;

    public PokerGame newGame() {
        return newGameForPlayer("");
    }

    public PokerGame newGameForPlayer(String email) {
        BigDecimal wager = BigDecimal.valueOf(1);
        UserService.modifyMoney(wager.negate(), email);
        PokerGame game = new PokerGame(sequence++, wager);
        game.setPlayer(email);
        games.put(game.getGameId(), game);
        return game;
    }

    public PokerGame getGame(Long id) {
        return games.get(id);
    }

    public PokerGame action(Long id, PokerGameIn in, String email) throws PokerGameException {
        PokerGame game = games.get(id);
        if(!email.equals(game.getPlayer())) {
            throw new PokerGameException("Not your game");
        }
        if(game.getAvailableActions().contains(in.getAction())) {
            System.out.println("Perform action " + in.getAction().name());
            if(in.getAction() == STAY) {
                game.collect();
            }
            else if(in.getAction() == HOLD) {
                game.getHand().hold(in.getParameters(), game.getDeck());
                game.collect();
            }
        }
        return game;
    }
}
