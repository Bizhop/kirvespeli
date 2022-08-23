package fi.bizhop.jassu.controller;

import fi.bizhop.jassu.exception.UserException;
import fi.bizhop.jassu.model.User;
import fi.bizhop.jassu.model.UserIn;
import fi.bizhop.jassu.service.AuthService;
import fi.bizhop.jassu.service.UserService;
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
