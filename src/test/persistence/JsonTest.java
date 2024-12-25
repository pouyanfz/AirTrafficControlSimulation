package persistence;

import model.Airport;
import model.Flight;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class JsonTest {
    protected void checkAirport(String name, int capacity, int numRunways, Airport airport) {
        assertEquals(name, airport.getName());
        assertEquals(capacity, airport.getCurrentCapacity());
        assertEquals(numRunways, airport.getRunways().size());
    }

    protected void checkFlight(String airplaneName, String classification, String origin, String destination,
            int flightNumber, Flight flight) {
        assertEquals(airplaneName, flight.getAirplaneName());
        assertEquals(classification, flight.getClassification());
        assertEquals(origin, flight.getOrigin());
        assertEquals(destination, flight.getDestination());
        assertEquals(flightNumber, flight.getFlightNumber());
        assertFalse(flight.isComplete());
    }

    @Test
    public void testAirportToJson() {
        Airport airport = new Airport("Test Airport", 10, 2);
        Flight flight = new Flight("Flight A", "Commercial", Flight.TYPE_DEPARTURE);
        airport.addFlight(flight);

        checkAirport("Test Airport", 10, 2, airport);
    }

    @Test
    public void testFlightToJson() {
        Flight flight = new Flight("Flight A", "Commercial", Flight.TYPE_DEPARTURE);
        checkFlight("Flight A", "Commercial", "No origin set", "No destination set", flight.getFlightNumber(), flight);
    }
}
