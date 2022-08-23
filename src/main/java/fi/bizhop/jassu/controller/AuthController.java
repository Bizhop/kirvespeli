package fi.bizhop.jassu.controller;

import fi.bizhop.jassu.model.User;
import fi.bizhop.jassu.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@RestController
@RequiredArgsConstructor
public class AuthController {
    final AuthService authService;

    @RequestMapping(value = "/auth", method = RequestMethod.GET, produces = "application/json")
    public @ResponseBody User auth(HttpServletRequest request, HttpServletResponse response) {
        var user = this.authService.login(request);
        if(user == null) {
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            return null;
        }
        else {
            response.setStatus(HttpServletResponse.SC_ACCEPTED);
            return user;
        }
    }
}
