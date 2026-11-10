package fi.bizhop.kirves.model.kirves;

import fi.bizhop.kirves.model.kirves.out.PlayerOut;
import fi.bizhop.kirves.model.kirves.pojo.UserPOJO;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class PlayerOutTest {
    @Test
    void exposesSidelinedAndFoldedPlayersSeparately() {
        var player = new Player(new UserPOJO("seat@example.com", "Seat"));
        assertTrue(new PlayerOut(player).isInGame());
        player.inactivate();
        var sidelined = new PlayerOut(player);
        assertFalse(sidelined.isInGame());
        assertFalse(sidelined.isFolded());
        player.activate();
        assertTrue(new PlayerOut(player).isInGame());
        player.fold();
        assertFalse(new PlayerOut(player).isInGame());
        assertTrue(new PlayerOut(player).isFolded());
    }

    @Test
    void exposesPendingDeparture() {
        var player = new Player(new UserPOJO("seat@example.com", "Seat"));
        player.depart();
        var departing = new PlayerOut(player);
        assertFalse(departing.isInGame());
        assertFalse(departing.isFolded());
        assertTrue(departing.isDeparturePending());
        assertEquals(java.util.List.of("ACKNOWLEDGE_DEPARTURE"), departing.getAvailableActions());
        player.acknowledgeDeparture();
        assertFalse(new PlayerOut(player).isDeparturePending());
        assertTrue(new PlayerOut(player).getAvailableActions().isEmpty());
    }
}
