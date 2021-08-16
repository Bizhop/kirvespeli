package fi.bizhop.jassu.controller;

import fi.bizhop.jassu.TestBase;
import fi.bizhop.jassu.model.kirves.out.GameBrief;
import fi.bizhop.jassu.service.AuthService;
import fi.bizhop.jassu.service.KirvesService;
import fi.bizhop.jassu.service.MessageService;
import fi.bizhop.jassu.service.UserService;
import fi.bizhop.jassu.util.TestUserUtil;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.RequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.util.Collections;
import java.util.List;

import static fi.bizhop.jassu.util.TestUserUtil.TEST_USER_EMAIL;
import static fi.bizhop.jassu.util.TestUserUtil.getTestUser;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(SpringExtension.class)
@WebMvcTest(value = KirvesController.class)
public class KirvesControllerTest extends TestBase {

    @MockBean
    AuthService authService;
    @MockBean
    UserService userService;
    @MockBean
    KirvesService kirvesService;
    @MockBean
    MessageService messageService;

    @Autowired
    private MockMvc mockMvc;

    @Test
    public void getGamesWithoutUserFails() throws Exception {
        RequestBuilder builder = MockMvcRequestBuilders.get("/api/kirves");

        MvcResult result = this.mockMvc.perform(builder).andReturn();
        assertEquals(401, result.getResponse().getStatus());
    }

    @Test
    public void getGamesWithUserSuccess() throws Exception {
        RequestBuilder builder = MockMvcRequestBuilders.get("/api/kirves");

        when(this.authService.getEmailFromJWT(any())).thenReturn(TEST_USER_EMAIL);
        when(this.userService.get(eq(TEST_USER_EMAIL))).thenReturn(TestUserUtil.getTestUser(TEST_USER_EMAIL));
        when(this.kirvesService.getActiveGames()).thenReturn(this.getTestGames());

        MvcResult result = this.mockMvc.perform(builder).andReturn();
        assertEquals(200, result.getResponse().getStatus());

        GameBrief[] response = this.mapper.readValue(result.getResponse().getContentAsString(), GameBrief[].class);

        assertEquals(1, response.length);
        GameBrief brief = response[0];
        assertEquals(1, brief.players.longValue());
        assertEquals(TEST_USER_EMAIL, brief.admin.getEmail());
    }

    private List<GameBrief> getTestGames() {
        GameBrief brief = new GameBrief();
        brief.id = 0L;
        brief.players = 1;
        brief.admin = getTestUser(TEST_USER_EMAIL);
        return Collections.singletonList(brief);
    }
}
