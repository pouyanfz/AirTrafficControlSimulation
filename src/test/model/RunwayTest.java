package model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class RunwayTest {
    private Runway runway;
    private Flight flight1;
    private Flight flight2;

    @BeforeEach
    public void runBefore() {
        runway = new Runway();
        flight1 = new Flight("B747-1", "commercial", Flight.TYPE_ARIVAL);
        flight2 = new Flight("B747-1", "commercial", Flight.TYPE_DEPARTURE);
    }

    @Test
    public void testConstructor() {
        assertTrue(runway.getRunwayID() >= 1);
        assertEquals(null, runway.getCurrentFlight());
    }

    @Test
    public void testAssignFlight() {
        assertFalse(runway.isOccupied());
        runway.assignFlight(flight1);
        assertTrue(runway.isOccupied());
        assertEquals(flight1, runway.getCurrentFlight());
        runway.assignFlight(flight2);
        assertEquals(flight2, runway.getCurrentFlight());
    }

    @Test
    public void testFreeRunway() {
        runway.assignFlight(flight1);
        assertTrue(runway.isOccupied());
        runway.freeRunway();
        assertFalse(runway.isOccupied());
        assertEquals(null, runway.getCurrentFlight());
    }
}
