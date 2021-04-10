package fi.bizhop.jassu.controller;

import fi.bizhop.jassu.TestBase;
import fi.bizhop.jassu.model.KirvesGameBrief;
import fi.bizhop.jassu.service.AuthService;
import fi.bizhop.jassu.service.KirvesService;
import fi.bizhop.jassu.service.MessageService;
import fi.bizhop.jassu.service.UserService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.RequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.util.Collections;
import java.util.List;

import static fi.bizhop.jassu.util.TestUserUtil.TEST_USER_EMAIL;
import static fi.bizhop.jassu.util.TestUserUtil.getTestUser;
import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@RunWith(SpringRunner.class)
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

        MvcResult result = mockMvc.perform(builder).andReturn();

        assertEquals(401, result.getResponse().getStatus());
    }

    @Test
    public void getGamesWithUserSuccess() throws Exception {
        RequestBuilder builder = MockMvcRequestBuilders.get("/api/kirves");

        when(authService.getEmailFromJWT(any())).thenReturn(TEST_USER_EMAIL);
        when(kirvesService.getActiveGames()).thenReturn(getTestGames());

        MvcResult result = mockMvc.perform(builder).andReturn();

        KirvesGameBrief[] response = mapper.readValue(result.getResponse().getContentAsString(), KirvesGameBrief[].class);

        assertEquals(1, response.length);
        KirvesGameBrief brief = response[0];
        assertEquals(1, brief.players.longValue());
        assertEquals(TEST_USER_EMAIL, brief.admin.getEmail());
    }

    private List<KirvesGameBrief> getTestGames() {
        KirvesGameBrief brief = new KirvesGameBrief();
        brief.id = 0L;
        brief.players = 1;
        brief.admin = getTestUser(TEST_USER_EMAIL);
        return Collections.singletonList(brief);
    }
}
