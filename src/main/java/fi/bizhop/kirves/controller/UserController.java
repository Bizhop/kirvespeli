package fi.bizhop.kirves.controller;

import fi.bizhop.kirves.exception.UserException;
import fi.bizhop.kirves.model.User;
import fi.bizhop.kirves.model.UserIn;
import fi.bizhop.kirves.service.AuthService;
import fi.bizhop.kirves.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@RestController
@RequiredArgsConstructor
public class UserController {
    final UserService USER_SERVICE;
    final AuthService AUTH_SERVICE;

    @RequestMapping(value = "/user", method = RequestMethod.PUT, produces = "application/json", consumes = "application/json")
    public @ResponseBody User update(   @RequestBody UserIn userIn,
                                        HttpServletRequest request,
                                        HttpServletResponse response) throws UserException {
        var email = this.AUTH_SERVICE.getEmailFromJWT(request);
        if(email == null) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            return null;
        } else {
            response.setStatus(HttpServletResponse.SC_OK);
            return this.USER_SERVICE.updateUser(email, userIn);
        }
    }
}
