package model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;

public class AirportTest {
    private Airport airport;

    @BeforeEach
    void runBefore() {
        airport = new Airport("JFK", 10, 2);
    }

    @Test
    void testConstructor() {
        assertEquals("JFK", airport.getName());
        assertEquals(10, airport.getCurrentCapacity());
        assertEquals(0, airport.getFlightsQueue().size());
        assertEquals(2, airport.getRunways().size());
        assertTrue(airport.isDone());
    }

    @Test
    void testAddFlight() {
        Flight arrival = new Flight("B757-4", "passenger", Flight.TYPE_ARIVAL);
        airport.addFlight(arrival);
        assertEquals(10, airport.getCurrentCapacity());
        assertEquals(1, airport.getFlightsQueue().size());
        assertEquals(2, airport.getRunways().size());
        Runway runway1 = airport.getRunways().get(0);
        Runway runway2 = airport.getRunways().get(1);
        assertFalse(runway1.isOccupied() || runway2.isOccupied());
        assertFalse(airport.isDone());
        assertEquals(arrival, airport.getFlightsQueue().getFirst());
    }

    @Test
    void testProcessSingleArrival() {
        Flight arrival = new Flight("B757-4", "passenger", Flight.TYPE_ARIVAL);
        Runway runway1 = airport.getRunways().get(0);
        Runway runway2 = airport.getRunways().get(1);
        airport.addFlight(arrival);
        assertEquals(1, airport.getFlightsQueue().size());

        airport.processFlightsOneRound();
        assertEquals(1, airport.getFlightsQueue().size());
        assertTrue(airport.getFlightsQueue().getFirst().isComplete());
        assertFalse(runway1.isOccupied() || runway2.isOccupied());
    }

    @Test
    void testProcessSingleDeparture() {
        Flight arrival = new Flight("B757-4", "passenger", Flight.TYPE_ARIVAL);
        Flight departure = new Flight("B757-4", "passenger", Flight.TYPE_DEPARTURE);
        airport.addFlight(arrival);
        airport.addFlight(departure);
        assertEquals(2, airport.getFlightsQueue().size());

        airport.processFlightsOneRound();
        assertEquals(2, airport.getFlightsQueue().size());
        assertTrue(airport.getFlightsQueue().getLast().isComplete());
    }

    @Test
    void testProcessStall() {
        Flight arrival1 = new Flight("B757-1", "passenger", Flight.TYPE_ARIVAL);
        Flight arrival2 = new Flight("B757-2", "passenger", Flight.TYPE_ARIVAL);
        Flight arrival3 = new Flight("B757-3", "passenger", Flight.TYPE_ARIVAL);
        int arrival3InitialFuel = arrival3.getAirplaneFuel();
        airport.addFlight(arrival1);
        airport.addFlight(arrival2);
        airport.addFlight(arrival3);
        airport.processFlightsOneRound();
        assertEquals(3, airport.getFlightsQueue().size());
        assertEquals(arrival3, airport.getFlightsQueue().getLast());
        assertEquals(arrival3InitialFuel - 10, arrival3.getAirplaneFuel());
        assertFalse(airport.getFlightsQueue().getLast().isComplete());
        assertFalse(airport.isDone());
    }

    @Test
    void testProcessStallEmergency() {
        Flight departure1 = new Flight("B757-1", "passenger", Flight.TYPE_DEPARTURE);
        Flight departure2 = new Flight("B757-2", "passenger", Flight.TYPE_DEPARTURE);
        Flight departure3 = new Flight("B757-3", "passenger", Flight.TYPE_DEPARTURE);
        Flight departure4 = new Flight("B757-1", "passenger", Flight.TYPE_DEPARTURE);
        Flight arrival1 = new Flight("B757-1", "passenger", Flight.TYPE_ARIVAL);
        arrival1.setAirplaneFuel(19);
        Flight departure5 = new Flight("B757-2", "passenger", Flight.TYPE_DEPARTURE);

        airport.addFlight(departure1);
        airport.addFlight(departure2);
        airport.addFlight(departure3);
        airport.addFlight(departure4);
        airport.addFlight(arrival1);
        airport.addFlight(departure5);

        assertEquals(100, departure5.getAirplaneFuel());

        airport.processFlightsOneRound();
        assertEquals(90, departure5.getAirplaneFuel());
        assertEquals(arrival1, airport.getFlightsQueue().getFirst());   // put in front for emergency landing
        assertFalse(arrival1.isComplete());

        airport.processFlightsOneRound();
        assertTrue(arrival1.isComplete());
        assertEquals(100, departure5.getAirplaneFuel());    // refueled
        assertEquals(departure5, airport.getFlightsQueue().getFirst());   // put in front for emergency takeoff
        assertFalse(departure5.isComplete());
    }

    @Test
    void testOutOfFuel() {
        Flight arrival1 = new Flight("B757-1", "passenger", Flight.TYPE_ARIVAL);
        Flight arrival2 = new Flight("B757-2", "passenger", Flight.TYPE_ARIVAL);
        Flight arrival3 = new Flight("B757-3", "passenger", Flight.TYPE_ARIVAL);
        Flight arrival4 = new Flight("B757-1", "passenger", Flight.TYPE_ARIVAL);
        Flight arrival5 = new Flight("B757-2", "passenger", Flight.TYPE_ARIVAL);
        Flight arrival6 = new Flight("B757-3", "passenger", Flight.TYPE_ARIVAL);
        arrival4.setAirplaneFuel(10);
        arrival5.setAirplaneFuel(10);
        arrival6.setAirplaneFuel(10);
        airport.addFlight(arrival1);
        airport.addFlight(arrival2);
        airport.addFlight(arrival3);
        airport.addFlight(arrival4);
        airport.addFlight(arrival5);
        airport.addFlight(arrival6);
        airport.processFlightsOneRound();
        assertEquals(arrival6, airport.getFlightsQueue().get(0));
        assertEquals(arrival5, airport.getFlightsQueue().get(1));
        assertEquals(arrival4, airport.getFlightsQueue().get(2));
        assertEquals(0, arrival4.getAirplaneFuel());
        airport.processFlightsOneRound();
    }

    @Test
    void testReachAirportCapacity() {
        Airport smallAirport = new Airport("YVR", 1, 2);
        Flight arrival1 = new Flight("B757-1", "passenger", Flight.TYPE_ARIVAL);
        Flight departure1 = new Flight("B757-1", "passenger", Flight.TYPE_DEPARTURE);
        Flight arrival2 = new Flight("B757-2", "passenger", Flight.TYPE_ARIVAL);
        Flight arrival3 = new Flight("B757-3", "passenger", Flight.TYPE_ARIVAL);
        smallAirport.addFlight(arrival1);
        smallAirport.addFlight(departure1);
        smallAirport.addFlight(arrival2);
        smallAirport.addFlight(arrival3);
        assertEquals(1, smallAirport.getCurrentCapacity());

        smallAirport.processFlightsOneRound();
        assertEquals(1, smallAirport.getCurrentCapacity());
        assertEquals(4, smallAirport.getFlightsQueue().size());

        smallAirport.processFlightsOneRound();
        assertEquals(0, smallAirport.getCurrentCapacity());
        assertEquals(4, smallAirport.getFlightsQueue().size());

        for (int i = 0; i < 10; i++) {
            smallAirport.processFlightsOneRound();
        }
        assertEquals(0, smallAirport.getCurrentCapacity());
        assertEquals(4, smallAirport.getFlightsQueue().size());
        assertTrue(smallAirport.getFlightsQueue().getLast().isComplete()); // last flight crashes
        assertTrue(airport.isDone());
    }

    @Test
    void testGetLogEntry() {
        List<String> log = airport.flushLogEntries();
        Flight arrival1 = new Flight("B757-1", "passenger", Flight.TYPE_ARIVAL);
        airport.addFlight(arrival1);
        assertTrue(log.size() == 0);
        airport.processFlightsOneRound();
        log = airport.flushLogEntries();
        assertTrue(log.size() != 0);

    }
}
