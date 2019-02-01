package fi.bizhop.jassu.controller;

import fi.bizhop.jassu.models.User;
import fi.bizhop.jassu.service.AuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@RestController
public class AuthController {
    @Autowired
    AuthService authService;

    @RequestMapping(value = "/api/auth", method = RequestMethod.GET)
    public @ResponseBody User auth(HttpServletRequest request, HttpServletResponse response) throws Exception {
        User user = authService.login(request);
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
