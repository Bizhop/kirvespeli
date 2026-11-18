package fi.bizhop.kirves.controller;

import fi.bizhop.kirves.TestBase;
import fi.bizhop.kirves.exception.KirvesGameException;
import fi.bizhop.kirves.model.kirves.out.GameBrief;
import fi.bizhop.kirves.service.AuthService;
import fi.bizhop.kirves.service.KirvesService;
import fi.bizhop.kirves.service.MessageService;
import fi.bizhop.kirves.service.UserService;
import fi.bizhop.kirves.util.TestUserUtil;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.util.Collections;
import java.util.List;

import static fi.bizhop.kirves.util.TestUserUtil.TEST_USER_EMAIL;
import static fi.bizhop.kirves.util.TestUserUtil.getTestUser;
import static jakarta.servlet.http.HttpServletResponse.SC_OK;
import static jakarta.servlet.http.HttpServletResponse.SC_FORBIDDEN;
import static jakarta.servlet.http.HttpServletResponse.SC_UNAUTHORIZED;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.doThrow;

@ExtendWith(SpringExtension.class)
@WebMvcTest(value = KirvesController.class)
public class KirvesControllerTest extends TestBase {

    @MockitoBean
    AuthService authService;
    @MockitoBean
    UserService userService;
    @MockitoBean
    KirvesService kirvesService;
    @MockitoBean
    MessageService messageService;

    @Autowired
    private MockMvc mockMvc;

    @Test
    public void getGamesWithoutUserFails() throws Exception {
        var builder = MockMvcRequestBuilders.get("/api/kirves");

        var result = this.mockMvc.perform(builder).andReturn();
        assertEquals(SC_UNAUTHORIZED, result.getResponse().getStatus());
    }

    @Test
    public void getGamesWithUserSuccess() throws Exception {
        var builder = MockMvcRequestBuilders.get("/api/kirves");

        when(this.authService.getEmailFromJWT(any())).thenReturn(TEST_USER_EMAIL);
        when(this.userService.get(eq(TEST_USER_EMAIL))).thenReturn(TestUserUtil.getTestUser(TEST_USER_EMAIL));
        when(this.kirvesService.getActiveGames(any())).thenReturn(this.getTestGames());

        var result = this.mockMvc.perform(builder).andReturn();
        assertEquals(SC_OK, result.getResponse().getStatus());

        var response = this.mapper.readValue(result.getResponse().getContentAsString(), GameBrief[].class);

        assertEquals(1, response.length);
        var brief = response[0];
        assertEquals(1, brief.getPlayers());
        assertEquals(true, brief.getJoined());
        assertEquals(TEST_USER_EMAIL, brief.getAdmin().getEmail());
    }

    @Test
    public void validTokenForUnknownUserIsForbidden() throws Exception {
        when(this.authService.getEmailFromJWT(any())).thenReturn(TEST_USER_EMAIL);
        var result = this.mockMvc.perform(MockMvcRequestBuilders.get("/api/kirves")).andReturn();
        assertEquals(SC_FORBIDDEN, result.getResponse().getStatus());
    }

    @Test
    public void deleteByNonOwnerIsForbidden() throws Exception {
        when(this.authService.getEmailFromJWT(any())).thenReturn(TEST_USER_EMAIL);
        when(this.userService.get(eq(TEST_USER_EMAIL))).thenReturn(getTestUser(TEST_USER_EMAIL));
        doThrow(new KirvesGameException("Not owner", KirvesGameException.Type.UNAUTHORIZED))
                .when(this.kirvesService).inactivateGame(eq(1L), any());
        var result = this.mockMvc.perform(MockMvcRequestBuilders.delete("/api/kirves/1")).andReturn();
        assertEquals(SC_FORBIDDEN, result.getResponse().getStatus());
    }

    @Test
    public void restoreByNonAdminIsForbidden() throws Exception {
        when(this.authService.getEmailFromJWT(any())).thenReturn(TEST_USER_EMAIL);
        when(this.userService.get(eq(TEST_USER_EMAIL))).thenReturn(getTestUser(TEST_USER_EMAIL));
        doThrow(new KirvesGameException("Not admin", KirvesGameException.Type.UNAUTHORIZED))
                .when(this.kirvesService).restoreGame(eq(1L), eq(2L), eq(0), any());
        var result = this.mockMvc.perform(MockMvcRequestBuilders.post("/api/kirves/1/2/0")).andReturn();
        assertEquals(SC_FORBIDDEN, result.getResponse().getStatus());
    }

    private List<GameBrief> getTestGames() {
        var brief = GameBrief.builder()
                .id(0L)
                .players(1)
                .joined(true)
                .admin(getTestUser(TEST_USER_EMAIL))
                .build();
        return Collections.singletonList(brief);
    }
}
