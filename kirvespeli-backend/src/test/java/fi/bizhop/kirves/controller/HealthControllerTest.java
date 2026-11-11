package fi.bizhop.kirves.controller;

import fi.bizhop.kirves.TestBase;
import fi.bizhop.kirves.service.AuthService;
import fi.bizhop.kirves.service.UserService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import static jakarta.servlet.http.HttpServletResponse.SC_OK;
import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(SpringExtension.class)
@WebMvcTest(value = HealthController.class)
public class HealthControllerTest extends TestBase {

    @MockitoBean
    AuthService authService;
    @MockitoBean
    UserService userService;

    @Autowired
    private MockMvc mockMvc;

    @Test
    public void liveWithoutUserSuccess() throws Exception {
        var result = this.mockMvc.perform(MockMvcRequestBuilders.get("/api/health/live")).andReturn();

        assertEquals(SC_OK, result.getResponse().getStatus());
        assertEquals("UP", this.mapper.readTree(result.getResponse().getContentAsString()).get("status").asString());
    }
}
