package fi.bizhop.jassu;

import fi.bizhop.jassu.exception.CardException;
import fi.bizhop.jassu.models.Cards;
import fi.bizhop.jassu.models.KirvesDeck;
import fi.bizhop.jassu.service.KirvesService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import static org.junit.Assert.assertEquals;

@RunWith(SpringRunner.class)
@SpringBootTest
public class KirvesTests {
    @Autowired
    KirvesService service;

    @Test
    public void testKirvesDeck() throws CardException {
        Cards kirvesDeck = new KirvesDeck();

        assertEquals(54, kirvesDeck.size());
    }
}
