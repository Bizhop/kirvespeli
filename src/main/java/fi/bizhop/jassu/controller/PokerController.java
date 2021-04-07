package fi.bizhop.jassu.controller;

import fi.bizhop.jassu.exception.CardException;
import fi.bizhop.jassu.exception.PokerGameException;
import fi.bizhop.jassu.model.PokerGame;
import fi.bizhop.jassu.model.PokerGameIn;
import fi.bizhop.jassu.model.PokerGameOut;
import fi.bizhop.jassu.service.AuthService;
import fi.bizhop.jassu.service.PokerService;
import fi.bizhop.jassu.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.List;
import java.util.stream.Collectors;

@RestController
public class PokerController {
    final PokerService pokerService;
    final AuthService authService;
    final UserService userService;

    public PokerController(PokerService pokerService, AuthService authService, UserService userService) {
        this.pokerService = pokerService;
        this.authService = authService;
        this.userService = userService;
    }

    @RequestMapping(value = "/api/poker/deal", method = RequestMethod.GET, produces = "application/json")
    public @ResponseBody PokerGameOut deal(HttpServletRequest request, HttpServletResponse response) {
        String email = this.authService.getEmailFromJWT(request);
        if(email == null) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            return new PokerGameOut("Unauthorized");
        }
        else {
            try {
                response.setStatus(HttpServletResponse.SC_OK);
                PokerGame game = this.pokerService.newGameForPlayer(email);
                game.deal();
                return new PokerGameOut(game, this.userService.getUserMoney(email));
            } catch (CardException e) {
                response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                return new PokerGameOut(e.getMessage());
            }
        }
    }

    @RequestMapping(value = "/api/poker/{id}", method = RequestMethod.GET, produces = "application/json")
    public @ResponseBody PokerGameOut getGame(@PathVariable Long id, HttpServletRequest request, HttpServletResponse response) {
        String email = this.authService.getEmailFromJWT(request);
        if(email == null) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            return new PokerGameOut("Unauthorized");
        }
        else {
            try {
                return new PokerGameOut(this.pokerService.getGame(id, email), this.userService.getUserMoney(email));
            } catch (PokerGameException e) {
                response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                return new PokerGameOut(e.getMessage());
            }
        }
    }

    @RequestMapping(value = "/api/poker", method = RequestMethod.GET, produces = "application/json")
    public @ResponseBody List<PokerGameOut> getGames(HttpServletRequest request, HttpServletResponse response) {
        String email = this.authService.getEmailFromJWT(request);
        if(email == null) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            return null;
        }
        else {
            return this.pokerService.getGames(email).stream()
                    .map(PokerGameOut::new)
                    .collect(Collectors.toList());
        }
    }

    @RequestMapping(value = "/api/poker/{id}", method = RequestMethod.POST, consumes = "application/json", produces = "application/json")
    public @ResponseBody PokerGameOut action(@RequestBody PokerGameIn in, @PathVariable Long id, HttpServletRequest request, HttpServletResponse response) {
        String email = this.authService.getEmailFromJWT(request);
        if(email == null) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            return new PokerGameOut("Unauthorized");
        }
        else {
            try {
                return new PokerGameOut(this.pokerService.action(id, in, email), this.userService.getUserMoney(email));
            } catch (PokerGameException e) {
                response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                return new PokerGameOut(e.getMessage());
            } catch (CardException e) {
                response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                return new PokerGameOut(e.getMessage());
            }
        }
    }
}
