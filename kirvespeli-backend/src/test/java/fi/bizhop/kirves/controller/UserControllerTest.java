package fi.bizhop.kirves.controller;

import fi.bizhop.kirves.exception.UserException;
import fi.bizhop.kirves.service.AuthService;
import fi.bizhop.kirves.service.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(UserController.class)
class UserControllerTest {
    @Autowired
    MockMvc mvc;
    @MockitoBean
    AuthService authService;
    @MockitoBean
    UserService userService;

    @Test
    void validationErrorHasFinnishMessageAndBadRequestStatus() throws Exception {
        when(this.authService.getEmailFromJWT(any())).thenReturn("user@example.com");
        when(this.userService.updateUser(eq("user@example.com"), any()))
                .thenThrow(new UserException("Nickname ei saa olla tyhjä"));

        this.mvc.perform(put("/api/user").contentType("application/json").content("{\"nickname\":\"\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Nickname ei saa olla tyhjä"));
    }
}
