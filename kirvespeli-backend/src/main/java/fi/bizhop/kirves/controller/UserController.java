package fi.bizhop.kirves.controller;

import fi.bizhop.kirves.exception.UserException;
import fi.bizhop.kirves.model.User;
import fi.bizhop.kirves.model.UserIn;
import fi.bizhop.kirves.service.AuthService;
import fi.bizhop.kirves.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import java.util.Map;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@RestController
@RequiredArgsConstructor
public class UserController {
    final UserService USER_SERVICE;
    final AuthService AUTH_SERVICE;

    @ExceptionHandler(UserException.class)
    public ResponseEntity<Map<String, String>> userError(UserException exception) {
        return ResponseEntity.badRequest().body(Map.of("message", exception.getMessage()));
    }

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
