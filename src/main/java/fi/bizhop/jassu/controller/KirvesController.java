package fi.bizhop.jassu.controller;

import fi.bizhop.jassu.exception.CardException;
import fi.bizhop.jassu.exception.KirvesGameException;
import fi.bizhop.jassu.exception.TransactionException;
import fi.bizhop.jassu.model.User;
import fi.bizhop.jassu.model.kirves.ActionLog;
import fi.bizhop.jassu.model.kirves.Game;
import fi.bizhop.jassu.model.kirves.in.GameIn;
import fi.bizhop.jassu.model.kirves.out.GameBrief;
import fi.bizhop.jassu.model.kirves.out.GameOut;
import fi.bizhop.jassu.service.AuthService;
import fi.bizhop.jassu.service.KirvesService;
import fi.bizhop.jassu.service.MessageService;
import fi.bizhop.jassu.service.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.List;

import static fi.bizhop.jassu.exception.TransactionException.Type.INTERNAL;
import static fi.bizhop.jassu.exception.TransactionException.Type.UNKNOWN;

@RestController
public class KirvesController {
    final KirvesService KIRVES_SERVICE;
    final AuthService AUTH_SERVICE;
    final UserService USER_SERVICE;
    final MessageService MESSAGE_SERVICE;

    public KirvesController(KirvesService kirvesService, AuthService authService, UserService userService, MessageService messageService) {
        this.KIRVES_SERVICE = kirvesService;
        this.AUTH_SERVICE = authService;
        this.USER_SERVICE = userService;
        this.MESSAGE_SERVICE = messageService;
    }

    private User authorizeAndAuthenticate(HttpServletRequest request) throws ResponseStatusException {
        String email = this.AUTH_SERVICE.getEmailFromJWT(request);
        if(email == null) throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Unauthorized");

        User user = this.USER_SERVICE.get(email);
        if(user == null) throw new ResponseStatusException(HttpStatus.FORBIDDEN, String.format("Tunnusta ei löytynyt: %s", email));

        return user;
    }

    @RequestMapping(value = "/api/kirves", method = RequestMethod.POST, produces = "application/json")
    public @ResponseBody GameOut init(HttpServletRequest request, HttpServletResponse response) {
        User user = this.authorizeAndAuthenticate(request);

        try {
            response.setStatus(HttpServletResponse.SC_OK);
            Long id = this.KIRVES_SERVICE.init(user);
            return this.KIRVES_SERVICE.getGame(id).out(user).setId(id);
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage());
        }
    }

    @RequestMapping(value = "/api/kirves", method = RequestMethod.GET, produces = "application/json")
    public @ResponseBody List<GameBrief> getGames(HttpServletRequest request, HttpServletResponse response) {
        this.authorizeAndAuthenticate(request);

        response.setStatus(HttpServletResponse.SC_OK);
        return this.KIRVES_SERVICE.getActiveGames();
    }

    @RequestMapping(value = "/api/kirves/{id}", method = RequestMethod.POST, produces = "application/json")
    public @ResponseBody GameOut joinGame(@PathVariable Long id, HttpServletRequest request, HttpServletResponse response) {
        User user = this.authorizeAndAuthenticate(request);

        try {
            response.setStatus(HttpServletResponse.SC_OK);
            this.KIRVES_SERVICE.joinGame(id, user);
            GameOut out = this.KIRVES_SERVICE.getGame(id).out(user);
            this.refresh(id);
            return out;
        } catch (KirvesGameException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage());
        }
    }

    @RequestMapping(value = "/api/kirves/{id}", method = RequestMethod.GET, produces = "application/json")
    public @ResponseBody GameOut getGame(@PathVariable Long id, HttpServletRequest request, HttpServletResponse response) {
        User user = this.authorizeAndAuthenticate(request);

        response.setStatus(HttpServletResponse.SC_OK);
        try {
            Game game = this.KIRVES_SERVICE.getGame(id);
            return game.out(user).setId(id);
        } catch (KirvesGameException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage());
        }
    }

    @RequestMapping(value = "/api/kirves/{id}", method = RequestMethod.DELETE)
    public void deleteGame(@PathVariable Long id, HttpServletRequest request, HttpServletResponse response) {
        User user = this.authorizeAndAuthenticate(request);

        response.setStatus(HttpServletResponse.SC_OK);
        try {
            this.KIRVES_SERVICE.inactivateGame(id, user);
        } catch (KirvesGameException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
        } catch (TransactionException e) {
            throw createTransactionResponseStatus(e);
        }
    }

    @RequestMapping(value = "/api/kirves/{id}", method = RequestMethod.PUT, produces = "application/json", consumes = "application/json")
    public @ResponseBody GameOut action(@PathVariable Long id, @RequestBody GameIn in, HttpServletRequest request, HttpServletResponse response) {
        User user = this.authorizeAndAuthenticate(request);

        response.setStatus(HttpServletResponse.SC_OK);
        try {
            GameOut out = this.KIRVES_SERVICE.action(id, in, user).out(user);
            this.refresh(id);
            return out;
        } catch (KirvesGameException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
        } catch (TransactionException e) {
            throw createTransactionResponseStatus(e);
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage());
        }
    }

    @RequestMapping(value = "/api/kirves/{id}/{handId}", method = RequestMethod.GET, produces = "application/json")
    public @ResponseBody ActionLog getActionLog(@PathVariable Long id, @PathVariable Long handId, HttpServletRequest request, HttpServletResponse response) {
        this.authorizeAndAuthenticate(request);

        response.setStatus(HttpServletResponse.SC_OK);
        try {
            return this.KIRVES_SERVICE.getActionLog(id, handId);
        } catch (KirvesGameException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
        }
    }

    @RequestMapping(value = "/api/kirves/{id}/{handId}/{actionLogItemIndex}", method = RequestMethod.GET, produces = "application/json")
    public @ResponseBody GameOut getReplay(@PathVariable Long id, @PathVariable Long handId, @PathVariable long actionLogItemIndex, HttpServletRequest request, HttpServletResponse response) {
        this.authorizeAndAuthenticate(request);

        response.setStatus(HttpServletResponse.SC_OK);
        try {
            var game = this.KIRVES_SERVICE.getReplay(id, handId, actionLogItemIndex);
            return game.out();
        } catch (KirvesGameException | CardException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
        }
    }

    private static ResponseStatusException createTransactionResponseStatus(TransactionException e) {
        if(List.of(UNKNOWN, INTERNAL).contains(e.getType())) {
            return new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage());
        }
        return new ResponseStatusException(HttpStatus.CONFLICT, String.format("%s: %s", e.getType(), e.getMessage()));
    }

    private void refresh(Long id) {
        this.MESSAGE_SERVICE.send("/topic/refresh", id.toString());
    }
}
