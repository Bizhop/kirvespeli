package fi.bizhop.jassu.controller;

import fi.bizhop.jassu.exception.PokerGameException;
import fi.bizhop.jassu.models.PokerGame;
import fi.bizhop.jassu.models.PokerGameIn;
import fi.bizhop.jassu.models.PokerGameOut;
import fi.bizhop.jassu.service.AuthService;
import fi.bizhop.jassu.service.PokerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@RestController
public class PokerController {
    @Autowired
    PokerService pokerService;
    @Autowired
    AuthService authService;

    @RequestMapping(value = "/api/poker/deal", method = RequestMethod.GET, produces = "application/json")
    public @ResponseBody PokerGameOut deal(HttpServletRequest request, HttpServletResponse response) {
        String email = authService.getEmailFromJWT(request);
        if(email == null) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            return null;
        }
        else {
            response.setStatus(HttpServletResponse.SC_OK);
            PokerGame game = pokerService.newGameForPlayer(email);
            game.deal();
            return new PokerGameOut(game);
        }
    }

    @RequestMapping(value = "/api/poker/{id}", method = RequestMethod.GET, produces = "application/json")
    public @ResponseBody PokerGameOut getGame(@PathVariable Long id) {
        return new PokerGameOut(pokerService.getGame(id));
    }

    @RequestMapping(value = "/api/poker/{id}", method = RequestMethod.POST, consumes = "application/json", produces = "application/json")
    public @ResponseBody PokerGameOut action(@RequestBody PokerGameIn in, @PathVariable Long id, HttpServletRequest request, HttpServletResponse response) {
        String email = authService.getEmailFromJWT(request);
        if(email == null) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            return null;
        }
        else {
            try {
                return new PokerGameOut(pokerService.action(id, in, email));
            } catch (PokerGameException e) {
                response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                return new PokerGameOut(e.getMessage());
            }
        }
    }
}
