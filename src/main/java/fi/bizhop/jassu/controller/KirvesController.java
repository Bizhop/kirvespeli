package fi.bizhop.jassu.controller;

import fi.bizhop.jassu.exception.CardException;
import fi.bizhop.jassu.models.KirvesGame;
import fi.bizhop.jassu.models.KirvesGameOut;
import fi.bizhop.jassu.models.PokerGameOut;
import fi.bizhop.jassu.models.User;
import fi.bizhop.jassu.service.AuthService;
import fi.bizhop.jassu.service.KirvesService;
import fi.bizhop.jassu.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@RestController
public class KirvesController {
    @Autowired
    KirvesService kirvesService;
    @Autowired
    AuthService authService;
    @Autowired
    UserService userService;

    @RequestMapping(value = "/api/kirves/init", method = RequestMethod.GET, produces = "application/json")
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
                KirvesGame game = kirvesService.newGameForAdmin(admin);
                return new KirvesGameOut(game);
            } catch (CardException e) {
                response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                return new KirvesGameOut(e.getMessage());
            }
        }
    }
}
