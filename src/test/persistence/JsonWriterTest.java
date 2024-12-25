package persistence;

import model.Airport;
import model.Flight;
import org.junit.jupiter.api.Test;

import java.io.FileNotFoundException;

import static org.junit.jupiter.api.Assertions.*;

// Referenced from the JsonSerialization Demo
// https://github.students.cs.ubc.ca/CPSC210/JsonSerializationDemo
// Represents a writer that writes JSON representation of airport to file
public class JsonWriterTest {
    @Test
    public void testWriterInvalidFile() {
        try {
            Airport airport = new Airport("Test Airport", 10, 2);
            JsonWriter writer = new JsonWriter("./data/my\0illegal:fileName.json");
            writer.open();
            fail("Expected FileNotFoundException was not thrown");
        } catch (FileNotFoundException e) {
            // expected
        }
    }

    @Test
    public void testWriterEmptyAirport() {
        try {
            Airport airport = new Airport("Empty Airport", 0, 0);
            JsonWriter writer = new JsonWriter("./data/testWriterEmptyAirport.json");
            writer.open();
            writer.write(airport);
            writer.close();

            JsonReader reader = new JsonReader("./data/testWriterEmptyAirport.json");
            airport = reader.read();
            assertEquals("Empty Airport", airport.getName());
            assertEquals(0, airport.getCurrentCapacity());
            assertEquals(0, airport.getRunways().size());
        } catch (Exception e) {
            fail("Exception should not have been thrown");
        }
    }

    @Test
    public void testWriterGeneralAirport() {
        try {
            Airport airport = new Airport("Test Airport", 10, 2);

            // Add a flight to the airport
            Flight flightA = new Flight("Flight A", "Commercial", Flight.TYPE_ARIVAL);
            flightA.setOrigin("Origin A");
            flightA.setDestination("Destination A");
            airport.addFlight(flightA);

            // Write the airport to JSON
            JsonWriter writer = new JsonWriter("./data/testWriterGeneralAirport.json");
            writer.open();
            writer.write(airport);
            writer.close();

            // Read the airport from JSON
            JsonReader reader = new JsonReader("./data/testWriterGeneralAirport.json");
            airport = reader.read();

            assertEquals("Test Airport", airport.getName());
            assertEquals(10, airport.getCurrentCapacity());
            assertEquals(2, airport.getRunways().size()); // Ensure runways are created automatically

            Flight flight = airport.getFlightsQueue().get(0);
            assertEquals("Flight A", flight.getAirplaneName());
            assertEquals("In Air", flight.getPrintableStatus());
            assertEquals("Origin A", flight.getOrigin());
            assertEquals("Destination A", flight.getDestination());
            assertTrue(flight.getAirplaneFuel() > 0);
        } catch (Exception e) {
            fail("Exception should not have been thrown: " + e.getMessage());
        }
    }

}
