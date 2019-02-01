package fi.bizhop.jassu.service;

import fi.bizhop.jassu.models.Cards;
import fi.bizhop.jassu.models.PokerGame;
import fi.bizhop.jassu.models.PokerGameIn;
import fi.bizhop.jassu.models.StandardDeck;
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
        PokerGame game = new PokerGame(sequence++, BigDecimal.valueOf(1));
        games.put(game.getGameId(), game);
        return game;
    }

    public PokerGame getGame(Long id) {
        return games.get(id);
    }

    public PokerGame action(PokerGameIn in) {
        PokerGame game = games.get(in.getGameId());
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
