package fi.bizhop.jassu.service;

import fi.bizhop.jassu.exception.CardException;
import fi.bizhop.jassu.models.KirvesGame;
import fi.bizhop.jassu.models.User;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class KirvesService {
    private static final Logger LOG = LogManager.getLogger(KirvesService.class);

    @Autowired
    UserService userService;

    private Map<Long, KirvesGame> games = new HashMap<>();
    private Long sequence = 0L;

    public KirvesGame newGameForAdmin(User admin) throws CardException {
        KirvesGame game = new KirvesGame(admin);
        games.put(sequence++, game);
        return game;
    }
}
