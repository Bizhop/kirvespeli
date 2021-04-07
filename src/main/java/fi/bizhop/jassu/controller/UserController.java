package fi.bizhop.jassu.controller;

import fi.bizhop.jassu.model.User;
import fi.bizhop.jassu.model.UserIn;
import fi.bizhop.jassu.service.AuthService;
import fi.bizhop.jassu.service.UserService;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@RestController
public class UserController {
    final UserService userService;
    final AuthService authService;

    public UserController(UserService userService, AuthService authService) {
        this.userService = userService;
        this.authService = authService;
    }

    @RequestMapping(value = "/api/user", method = RequestMethod.PUT, produces = "application/json", consumes = "application/json")
    public @ResponseBody User update(   @RequestBody UserIn userIn,
                                        HttpServletRequest request,
                                        HttpServletResponse response) {
        String email = this.authService.getEmailFromJWT(request);
        if(email == null) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            return null;
        } else {
            response.setStatus(HttpServletResponse.SC_OK);
            User updated = userService.updateUser(email, userIn);
            if(updated == null) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            }
            return updated;
        }
    }
}
