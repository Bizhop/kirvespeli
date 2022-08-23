package fi.bizhop.jassu.controller;

import fi.bizhop.jassu.exception.KirvesGameException;
import fi.bizhop.jassu.exception.TransactionException;
import fi.bizhop.jassu.model.User;
import fi.bizhop.jassu.model.kirves.ActionLogItem;
import fi.bizhop.jassu.model.kirves.in.GameIn;
import fi.bizhop.jassu.model.kirves.out.GameBrief;
import fi.bizhop.jassu.model.kirves.out.GameOut;
import fi.bizhop.jassu.service.KirvesService;
import fi.bizhop.jassu.service.MessageService;
import fi.bizhop.jassu.util.ParameterUser;
import lombok.RequiredArgsConstructor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

import static fi.bizhop.jassu.exception.KirvesGameException.Type.BAD_REQUEST;
import static fi.bizhop.jassu.exception.KirvesGameException.Type.UNAUTHORIZED;
import static fi.bizhop.jassu.exception.TransactionException.Type.INTERNAL;
import static fi.bizhop.jassu.exception.TransactionException.Type.UNKNOWN;

@RestController
@RequiredArgsConstructor
public class KirvesController {
    private static final Logger LOG = LogManager.getLogger(KirvesController.class);

    final KirvesService KIRVES_SERVICE;
    final MessageService MESSAGE_SERVICE;

    @RequestMapping(value = "/kirves", method = RequestMethod.POST, produces = "application/json")
    public @ResponseBody List<GameBrief> init(@ParameterUser User user) {
        try {
            var id = this.KIRVES_SERVICE.init(user);
            return this.KIRVES_SERVICE.getActiveGames();
        } catch (Exception e) {
            LOG.error("Failed to initialize game", e);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage());
        }
    }

    //user is injected here to invoke resolver
    @RequestMapping(value = "/kirves", method = RequestMethod.GET, produces = "application/json")
    public @ResponseBody List<GameBrief> getGames(@ParameterUser User user) {
        return this.KIRVES_SERVICE.getActiveGames();
    }

    @RequestMapping(value = "/kirves/{id}", method = RequestMethod.POST, produces = "application/json")
    public void joinGame(@PathVariable Long id, @ParameterUser User user) {
        try {
            this.KIRVES_SERVICE.joinGame(id, user);
            this.refresh(id);
        } catch (KirvesGameException e) {
            throw createKirvesResponseStatus(e);
        } catch (Exception e) {
            LOG.error("Failed to join game", e);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage());
        }
    }

    @RequestMapping(value = "/kirves/{id}", method = RequestMethod.GET, produces = "application/json")
    public @ResponseBody GameOut getGame(@PathVariable Long id, @ParameterUser User user) {
        try {
            var game = this.KIRVES_SERVICE.getGame(id);
            return game.out(user).setId(id);
        } catch (KirvesGameException e) {
            throw createKirvesResponseStatus(e);
        } catch (Exception e) {
            LOG.error("Failed to get game", e);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage());
        }
    }

    @RequestMapping(value = "/kirves/{id}", method = RequestMethod.DELETE)
    public void deleteGame(@PathVariable Long id, @ParameterUser User user) {
        try {
            this.KIRVES_SERVICE.inactivateGame(id, user);
        } catch (KirvesGameException e) {
            throw createKirvesResponseStatus(e);
        } catch (TransactionException e) {
            throw createTransactionResponseStatus(e);
        }
    }

    @RequestMapping(value = "/kirves/{id}", method = RequestMethod.PUT, produces = "application/json", consumes = "application/json")
    public void action(@PathVariable Long id, @RequestBody GameIn in, @ParameterUser User user) {
        try {
            this.KIRVES_SERVICE.action(id, in, user).out(user);
            this.refresh(id);
        } catch (KirvesGameException e) {
            throw createKirvesResponseStatus(e);
        } catch (TransactionException e) {
            throw createTransactionResponseStatus(e);
        } catch (Exception e) {
            LOG.error("Action failed", e);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage());
        }
    }

    @RequestMapping(value = "/kirves/{id}/{handId}", method = RequestMethod.GET, produces = "application/json")
    public @ResponseBody List<ActionLogItem> getActionLog(@PathVariable Long id, @PathVariable Long handId, @ParameterUser User user) {
        try {
            return this.KIRVES_SERVICE.getActionLog(id, handId, user).getItems();
        } catch (KirvesGameException e) {
            throw createKirvesResponseStatus(e);
        }
    }

    @RequestMapping(value = "/kirves/{id}/{handId}/{actionLogItemIndex}", method = RequestMethod.GET, produces = "application/json")
    public @ResponseBody GameOut getReplay(@PathVariable Long id, @PathVariable Long handId, @PathVariable int actionLogItemIndex, @ParameterUser User user) {
        try {
            var game = this.KIRVES_SERVICE.getReplay(id, handId, actionLogItemIndex, user);
            return game.out();
        } catch (KirvesGameException e) {
            throw createKirvesResponseStatus(e);
        } catch (Exception e) {
            LOG.error("Getting replay failed", e);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage());
        }
    }

    @RequestMapping(value = "/kirves/{id}/{handId}/{actionLogItemIndex}", method = RequestMethod.POST)
    public void restoreGame(@PathVariable Long id, @PathVariable Long handId, @PathVariable int actionLogItemIndex, @ParameterUser User user) {
        try {
            this.KIRVES_SERVICE.restoreGame(id, handId, actionLogItemIndex, user);
            this.refresh(id);
        } catch (KirvesGameException e) {
            throw createKirvesResponseStatus(e);
        } catch (TransactionException e) {
            throw createTransactionResponseStatus(e);
        } catch (Exception e) {
            LOG.error("Failed to restore game", e);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage());
        }
    }

    private static ResponseStatusException createTransactionResponseStatus(TransactionException e) {
        if(List.of(UNKNOWN, INTERNAL).contains(e.getType())) {
            return new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage());
        }
        return new ResponseStatusException(HttpStatus.CONFLICT, String.format("%s: %s", e.getType(), e.getMessage()));
    }

    private static ResponseStatusException createKirvesResponseStatus(KirvesGameException e) {
        if(e.getType() == UNAUTHORIZED) {
            return new ResponseStatusException(HttpStatus.UNAUTHORIZED, e.getMessage());
        }
        else if(e.getType() == BAD_REQUEST) {
            return new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
        }
        return new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage());
    }

    private void refresh(Long id) {
        this.MESSAGE_SERVICE.send("/topic/refresh", id.toString());
    }
}
