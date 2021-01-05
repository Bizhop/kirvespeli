package fi.bizhop.jassu.controller;

import fi.bizhop.jassu.service.AuthService;
import fi.bizhop.jassu.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class KirvesController {

    @Autowired
    AuthService authService;
    @Autowired
    UserService userService;
}
