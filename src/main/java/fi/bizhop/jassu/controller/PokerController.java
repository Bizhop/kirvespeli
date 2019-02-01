package fi.bizhop.jassu.controller;

import fi.bizhop.jassu.models.PokerGame;
import fi.bizhop.jassu.models.PokerGameIn;
import fi.bizhop.jassu.models.PokerGameOut;
import fi.bizhop.jassu.service.PokerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
public class PokerController {
    @Autowired
    PokerService service;

    @RequestMapping(value = "/api/poker/deal", method = RequestMethod.GET, produces = "application/json")
    public @ResponseBody PokerGameOut deal() {
        PokerGame game = service.newGame();
        game.deal();
        return new PokerGameOut(game);
    }

    @RequestMapping(value = "/api/poker/{id}", method = RequestMethod.GET, produces = "application/json")
    public @ResponseBody PokerGameOut getGame(@PathVariable Long id) {
        PokerGame game = service.getGame(id);
        return new PokerGameOut(game);
    }

    @RequestMapping(value = "/api/poker", method = RequestMethod.POST, consumes = "application/json", produces = "application/json")
    public @ResponseBody PokerGameOut action(@RequestBody PokerGameIn in) {
        PokerGame game = service.action(in);
        return new PokerGameOut(game);
    }
}
