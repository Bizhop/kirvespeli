package fi.bizhop.jassu.controller;

import fi.bizhop.jassu.exception.KirvesGameException;
import fi.bizhop.jassu.exception.TransactionException;
import fi.bizhop.jassu.model.User;
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
    final KirvesService kirvesService;
    final AuthService authService;
    final UserService userService;
    final MessageService messageService;

    public KirvesController(KirvesService kirvesService, AuthService authService, UserService userService, MessageService messageService) {
        this.kirvesService = kirvesService;
        this.authService = authService;
        this.userService = userService;
        this.messageService = messageService;
    }

    @RequestMapping(value = "/api/kirves", method = RequestMethod.POST, produces = "application/json")
    public @ResponseBody GameOut init(HttpServletRequest request, HttpServletResponse response) {
        String email = this.authService.getEmailFromJWT(request);
        if(email == null) throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Unauthorized");

        User admin = this.userService.get(email);
        if(admin == null) throw new ResponseStatusException(HttpStatus.FORBIDDEN, String.format("Tunnusta ei löytynyt: %s", email));

        try {
            response.setStatus(HttpServletResponse.SC_OK);
            Long id = this.kirvesService.newGameForAdmin(admin);
            return this.kirvesService.getGame(id).out(admin).setId(id);
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage());
        }
    }

    @RequestMapping(value = "/api/kirves", method = RequestMethod.GET, produces = "application/json")
    public @ResponseBody List<GameBrief> getGames(HttpServletRequest request, HttpServletResponse response) {
        String email = this.authService.getEmailFromJWT(request);
        if(email == null) throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Unauthorized");

        response.setStatus(HttpServletResponse.SC_OK);
        return this.kirvesService.getActiveGames();
    }

    @RequestMapping(value = "/api/kirves/{id}", method = RequestMethod.POST, produces = "application/json")
    public @ResponseBody GameOut joinGame(@PathVariable Long id, HttpServletRequest request, HttpServletResponse response) {
        String email = this.authService.getEmailFromJWT(request);
        if(email == null) throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Unauthorized");

        User user = userService.get(email);
        if(user == null) throw new ResponseStatusException(HttpStatus.FORBIDDEN, String.format("Tunnusta ei löytynyt: %s", email));

        try {
            response.setStatus(HttpServletResponse.SC_OK);
            this.kirvesService.joinGame(id, user);
            GameOut out = this.kirvesService.getGame(id).out(user);
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
        String email = this.authService.getEmailFromJWT(request);
        if(email == null) throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Unauthorized");

        response.setStatus(HttpServletResponse.SC_OK);
        try {
            User me = this.userService.get(email);
            Game game = this.kirvesService.getGame(id);
            return game.out(me).setId(id);
        } catch (KirvesGameException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage());
        }
    }

    @RequestMapping(value = "/api/kirves/{id}", method = RequestMethod.DELETE)
    public void deleteGame(@PathVariable Long id, HttpServletRequest request, HttpServletResponse response) {
        String email = this.authService.getEmailFromJWT(request);
        if(email == null) throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Unauthorized");

        response.setStatus(HttpServletResponse.SC_OK);
        try {
            User me = this.userService.get(email);
            this.kirvesService.inactivateGame(id, me);
        } catch (KirvesGameException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
        } catch (TransactionException e) {
            throw createTransactionResponseStatus(e);
        }
    }

    @RequestMapping(value = "/api/kirves/{id}", method = RequestMethod.PUT, produces = "application/json", consumes = "application/json")
    public @ResponseBody GameOut action(@PathVariable Long id, @RequestBody GameIn in, HttpServletRequest request, HttpServletResponse response) {
        String email = this.authService.getEmailFromJWT(request);
        if(email == null) throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Unauthorized");

        User me = this.userService.get(email);
        response.setStatus(HttpServletResponse.SC_OK);
        try {
            GameOut out = this.kirvesService.action(id, in, me).out(me);
            refresh(id);
            return out;
        } catch (KirvesGameException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
        } catch (TransactionException e) {
            throw createTransactionResponseStatus(e);
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage());
        }
    }

    private ResponseStatusException createTransactionResponseStatus(TransactionException e) {
        if(List.of(UNKNOWN, INTERNAL).contains(e.getType())) {
            return new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage());
        }
        return new ResponseStatusException(HttpStatus.CONFLICT, String.format("%s: %s", e.getType(), e.getMessage()));
    }

    private void refresh(Long id) {
        this.messageService.send("/topic/refresh", id.toString());
    }
}
