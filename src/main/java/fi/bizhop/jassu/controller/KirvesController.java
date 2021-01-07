package fi.bizhop.jassu.controller;

import fi.bizhop.jassu.exception.CardException;
import fi.bizhop.jassu.exception.KirvesGameException;
import fi.bizhop.jassu.models.KirvesGame;
import fi.bizhop.jassu.models.KirvesGameOut;
import fi.bizhop.jassu.models.User;
import fi.bizhop.jassu.service.AuthService;
import fi.bizhop.jassu.service.KirvesService;
import fi.bizhop.jassu.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.HashMap;
import java.util.Map;

@RestController
public class KirvesController {
    @Autowired
    KirvesService kirvesService;
    @Autowired
    AuthService authService;
    @Autowired
    UserService userService;

    @RequestMapping(value = "/api/kirves", method = RequestMethod.POST, produces = "application/json")
    public @ResponseBody KirvesGameOut init(HttpServletRequest request, HttpServletResponse response) {
        String email = this.authService.getEmailFromJWT(request);
        if(email == null) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            return new KirvesGameOut("Unauthorized");
        }
        else {
            User admin = this.userService.get(email);
            if(admin == null) {
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                return new KirvesGameOut(String.format("Email not found: %s", email));
            }
            try {
                response.setStatus(HttpServletResponse.SC_OK);
                return this.kirvesService.newGameForAdmin(admin).out();
            } catch (CardException e) {
                response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                return new KirvesGameOut(e.getMessage());
            }
        }
    }

    @RequestMapping(value = "/api/kirves", method = RequestMethod.GET, produces = "application/json")
    public @ResponseBody Map<Long, KirvesGameOut> getGames(HttpServletRequest request, HttpServletResponse response) {
        String email = this.authService.getEmailFromJWT(request);
        if(email == null) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            return null;
        }
        else {
            response.setStatus(HttpServletResponse.SC_OK);
            Map<Long, KirvesGameOut> responseMap = new HashMap<>();
            for(Map.Entry<Long, KirvesGame> entry : this.kirvesService.getActiveGames(email).entrySet()) {
                responseMap.put(entry.getKey(), entry.getValue().out());
            }
            return responseMap;
        }
    }

    @RequestMapping(value = "/api/kirves/{id}", method = RequestMethod.POST, produces = "application/json")
    public @ResponseBody KirvesGameOut joinGame(@PathVariable Long id, HttpServletRequest request, HttpServletResponse response) {
        String email = this.authService.getEmailFromJWT(request);
        if(email == null) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            return null;
        }
        else {
            response.setStatus(HttpServletResponse.SC_OK);
            try {
                this.kirvesService.joinGame(id, email);
                return this.kirvesService.getGame(id).out();
            } catch (KirvesGameException e) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                return new KirvesGameOut(e.getMessage());
            }
        }
    }
}
