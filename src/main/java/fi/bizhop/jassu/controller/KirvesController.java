package fi.bizhop.jassu.controller;

import fi.bizhop.jassu.exception.CardException;
import fi.bizhop.jassu.exception.KirvesGameException;
import fi.bizhop.jassu.model.*;
import fi.bizhop.jassu.model.kirves.Game;
import fi.bizhop.jassu.model.kirves.GameBrief;
import fi.bizhop.jassu.model.kirves.GameIn;
import fi.bizhop.jassu.model.kirves.GameOut;
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
        if(email == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Unauthorized");
        } else {
            User admin = this.userService.get(email);
            if(admin == null) {
                throw new ResponseStatusException(HttpStatus.FORBIDDEN, String.format("Tunnusta ei löytynyt: %s", email));
            }
            try {
                response.setStatus(HttpServletResponse.SC_OK);
                Long id = this.kirvesService.newGameForAdmin(admin);
                return this.kirvesService.getGame(id).out(admin).setId(id);
            } catch (CardException | KirvesGameException e) {
                throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage());
            }
        }
    }

    @RequestMapping(value = "/api/kirves", method = RequestMethod.GET, produces = "application/json")
    public @ResponseBody List<GameBrief> getGames(HttpServletRequest request, HttpServletResponse response) {
        String email = this.authService.getEmailFromJWT(request);
        if(email == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Unauthorized");
        } else {
            response.setStatus(HttpServletResponse.SC_OK);
            return this.kirvesService.getActiveGames();
        }
    }

    @RequestMapping(value = "/api/kirves/{id}", method = RequestMethod.POST, produces = "application/json")
    public @ResponseBody GameOut joinGame(@PathVariable Long id, HttpServletRequest request, HttpServletResponse response) {
        String email = this.authService.getEmailFromJWT(request);
        if(email == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Unauthorized");
        } else {
            User user = userService.get(email);
            if(user == null) {
                throw new ResponseStatusException(HttpStatus.FORBIDDEN, String.format("Tunnusta ei löytynyt: %s", email));
            }
            response.setStatus(HttpServletResponse.SC_OK);
            try {
                this.kirvesService.joinGame(id, email);
                GameOut out = this.kirvesService.getGame(id).out(user);
                this.refresh(id);
                return out;
            } catch (KirvesGameException e) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
            } catch (CardException e) {
                throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage());
            }
        }
    }

    @RequestMapping(value = "/api/kirves/{id}", method = RequestMethod.GET, produces = "application/json")
    public @ResponseBody GameOut getGame(@PathVariable Long id, HttpServletRequest request, HttpServletResponse response) {
        String email = this.authService.getEmailFromJWT(request);
        if(email == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Unauthorized");
        } else {
            response.setStatus(HttpServletResponse.SC_OK);
            try {
                User me = this.userService.get(email);
                Game game = this.kirvesService.getGame(id);
                if(game.hasPlayer(me)) {
                    return game.out(me).setId(id);
                } else {
                    return game.out(null).setId(id);
                }
            } catch (KirvesGameException e) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
            } catch (CardException e) {
                throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage());
            }

        }
    }

    @RequestMapping(value = "/api/kirves/{id}", method = RequestMethod.DELETE)
    public void deleteGame(@PathVariable Long id, HttpServletRequest request, HttpServletResponse response) {
        String email = this.authService.getEmailFromJWT(request);
        if(email == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Unauthorized");
        } else {
            response.setStatus(HttpServletResponse.SC_OK);
            try {
                User me = this.userService.get(email);
                this.kirvesService.inactivateGame(id, me);
            } catch (KirvesGameException e) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
            }
        }
    }

    @RequestMapping(value = "/api/kirves/{id}", method = RequestMethod.PUT, produces = "application/json", consumes = "application/json")
    public @ResponseBody GameOut action(@PathVariable Long id, @RequestBody GameIn in, HttpServletRequest request, HttpServletResponse response) {
        String email = this.authService.getEmailFromJWT(request);
        if(email == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Unauthorized");
        } else {
            User me = this.userService.get(email);
            response.setStatus(HttpServletResponse.SC_OK);
            try {
                GameOut out = this.kirvesService.action(id, in, me).out(me);
                refresh(id);
                return out;
            } catch (KirvesGameException e) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
            } catch (CardException e) {
                throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage());
            }
        }
    }

    private void refresh(Long id) {
        this.messageService.send("/topic/refresh", id.toString());
    }
}
