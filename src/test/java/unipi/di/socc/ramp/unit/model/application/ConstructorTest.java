package unipi.di.socc.ramp.unit.model.application;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import unipi.di.socc.ramp.core.model.Application;
import unipi.di.socc.ramp.core.model.PiVersion;

public class ConstructorTest {

    @Test
    public void constructorTest(){
        assertThrows(NullPointerException.class, () -> new Application(null, PiVersion.GREEDYPI));
        assertThrows(NullPointerException.class, () -> new Application("testApp", null));
        assertThrows(IllegalArgumentException.class, () -> new Application("", PiVersion.GREEDYPI));

        Application app = new Application("app", PiVersion.GREEDYPI);
        assertNotNull(app);
        assertNotNull(app.getBindingFunction());
        assertNotNull(app.getGlobalState());
        assertNotNull(app.getNodes());
        assertEquals("app", app.getName());
        assertEquals(PiVersion.GREEDYPI, app.getPiVersion());
        assertTrue(app.isPiDeterministic());
    }

}
