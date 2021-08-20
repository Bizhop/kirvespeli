package fi.bizhop.jassu.controller;

import fi.bizhop.jassu.exception.CardException;
import fi.bizhop.jassu.exception.PokerGameException;
import fi.bizhop.jassu.model.User;
import fi.bizhop.jassu.model.poker.PokerGame;
import fi.bizhop.jassu.model.poker.PokerGameIn;
import fi.bizhop.jassu.model.poker.PokerGameOut;
import fi.bizhop.jassu.service.PokerService;
import fi.bizhop.jassu.util.ParameterUser;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.stream.Collectors;

@RestController
public class PokerController {
    final PokerService POKER_SERVICE;

    public PokerController(PokerService pokerService) {
        this.POKER_SERVICE = pokerService;
    }

    @RequestMapping(value = "/poker/deal", method = RequestMethod.GET, produces = "application/json")
    public @ResponseBody PokerGameOut deal(@ParameterUser User user) {
        try {
            return this.POKER_SERVICE.newGameForPlayer(user);
        } catch (CardException e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage());
        }
    }

    @RequestMapping(value = "/poker/{id}", method = RequestMethod.GET, produces = "application/json")
    public @ResponseBody PokerGameOut getGame(@PathVariable Long id, @ParameterUser User user) {
        try {
            return this.POKER_SERVICE.getGame(id, user);
        } catch (PokerGameException e) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN);
        }
    }

    @RequestMapping(value = "/poker", method = RequestMethod.GET, produces = "application/json")
    public @ResponseBody List<PokerGameOut> getGames(@ParameterUser User user) {
        return this.POKER_SERVICE.getGames(user);
    }

    @RequestMapping(value = "/poker/{id}", method = RequestMethod.POST, consumes = "application/json", produces = "application/json")
    public @ResponseBody PokerGameOut action(@RequestBody PokerGameIn in, @PathVariable Long id, @ParameterUser User user) {
        try {
            return this.POKER_SERVICE.action(id, in, user);
        } catch (PokerGameException e) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN);
        } catch (CardException e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
