package persistence;

import model.Airport;
import model.Flight;
import model.Runway;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;

// Referenced from the JsonSerialization Demo
// https://github.students.cs.ubc.ca/CPSC210/JsonSerializationDemo
public class JsonReaderTest {
    @Test
    public void testReaderNonExistentFile() {
        JsonReader reader = new JsonReader("./data/noSuchFile.json");
        try {
            Airport airport = reader.read();
            fail("Expected IOException was not thrown");
        } catch (IOException e) {
            // expected
        }
    }

    @Test
    public void testReaderEmptyAirport() {
        JsonReader reader = new JsonReader("./data/testReaderEmptyAirport.json");
        try {
            Airport airport = reader.read();
            assertEquals("Empty Airport", airport.getName());
            assertEquals(0, airport.getCurrentCapacity());
            assertEquals(0, airport.getRunways().size());
        } catch (IOException e) {
            fail("Exception should not have been thrown");
        }
    }

    @Test
    public void testAirportRunwayCount() {
        Airport airport = new Airport("Test Airport", 100, 2);
        assertEquals(2, airport.getRunways().size());
    }

    @SuppressWarnings("methodlength")
    @Test
    public void testReaderGeneralAirport() {
        JsonReader reader = new JsonReader("./data/testReaderGeneralAirport.json");
        try {
            Airport airport = reader.read();
            assertEquals("Test Airport", airport.getName());
            assertEquals(10, airport.getCurrentCapacity());
            assertEquals(2, airport.getRunways().size());
            assertEquals(1, airport.getRunways().get(0).getRunwayID());
            assertEquals(2, airport.getRunways().get(1).getRunwayID());

            Runway runway1 = airport.getRunways().get(0);
            assertEquals(1, runway1.getRunwayID());
            assertFalse(runway1.isOccupied());

            Runway runway2 = airport.getRunways().get(1);
            assertEquals(2, runway2.getRunwayID());
            assertFalse(runway2.isOccupied());

            assertEquals(2, airport.getFlightsQueue().size());
            Flight flight = airport.getFlightsQueue().get(0);
            assertEquals("Airbus A320", flight.getAirplaneName());
            assertEquals("San Francisco", flight.getOrigin());
            assertEquals("Vancouver", flight.getDestination());
            assertEquals(2, airport.getFlightsQueue().size());
            assertEquals("In Air", flight.getPrintableStatus());
            assertFalse(flight.isComplete());
            Flight flight2 = airport.getFlightsQueue().get(1);
            assertEquals("Boeing 737", flight2.getAirplaneName());
            assertEquals("Parked", flight2.getPrintableStatus());
            assertEquals("Vancouver", flight2.getOrigin());
            assertEquals("Chicago", flight2.getDestination());
            assertTrue(flight2.isComplete());
            assertEquals(100, flight2.getAirplaneFuel());
            assertEquals(40, flight.getAirplaneFuel());
        } catch (IOException e) {
            fail("Exception should not have been thrown");
        }
    }
}
