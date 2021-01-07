package fi.bizhop.jassu.service;

import fi.bizhop.jassu.exception.CardException;
import fi.bizhop.jassu.exception.KirvesGameException;
import fi.bizhop.jassu.models.KirvesGame;
import fi.bizhop.jassu.models.User;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class KirvesService {
    private static final Logger LOG = LogManager.getLogger(KirvesService.class);

    @Autowired
    UserService userService;

    private Map<Long, KirvesGame> games = new HashMap<>();
    private Long sequence = 0L;

    public KirvesGame newGameForAdmin(User admin) throws CardException {
        KirvesGame game = new KirvesGame(admin);
        Long id = this.sequence++;
        this.games.put(id, game);
        LOG.info(String.format("Created new game, id=%d", id));
        return game;
    }

    public Map<Long, KirvesGame> getActiveGames(String email) {
        return this.games.entrySet().stream()
                .filter(entry -> entry.getValue().isActive() && email.equals(entry.getValue().getAdmin()))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
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
}
