package model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class FlightTest {
    private Flight arrivalFlight;
    private Flight departureFlight;

    @BeforeEach
    public void runBefore() {
        arrivalFlight = new Flight("B747-1", "commercial", Flight.TYPE_ARIVAL);
        departureFlight = new Flight("B747-1", "commercial", Flight.TYPE_DEPARTURE);
    }

    @Test
    void testConstructor() {
        assertEquals("B747-1", arrivalFlight.getAirplaneName());
        assertEquals(Flight.TYPE_ARIVAL, arrivalFlight.getType());
        assertEquals("commercial", arrivalFlight.getClassification());
        assertEquals("No destination set", arrivalFlight.getDestination());
        assertEquals("No origin set", arrivalFlight.getOrigin());
        assertEquals("In Air", arrivalFlight.getPrintableStatus());
        assertFalse(arrivalFlight.isComplete());
        assertTrue(arrivalFlight.getAirplaneFuel() >= 20);
        assertTrue(arrivalFlight.getAirplaneFuel() <= 80);
        assertTrue(arrivalFlight.getFlightNumber() >= 100);
    }

    @Test
    void testSetters() {
        arrivalFlight.setAirplaneName("1234");
        assertEquals("1234", arrivalFlight.getAirplaneName());
        arrivalFlight.setClassification("Cargo");
        assertEquals("Cargo", arrivalFlight.getClassification());
        arrivalFlight.setDestination("vancouver");
        assertEquals("vancouver", arrivalFlight.getDestination());
        arrivalFlight.setOrigin("new york");
        assertEquals("new york", arrivalFlight.getOrigin());
        arrivalFlight.setComplete();
        assertTrue(arrivalFlight.isComplete());
    }

    @Test
    void testSetStatus() {
        arrivalFlight.setStatus(Flight.STATUS_PARKED);
        assertEquals("Parked", arrivalFlight.getPrintableStatus());
        arrivalFlight.setStatus(Flight.STATUS_IN_AIR);
        assertEquals("In Air", arrivalFlight.getPrintableStatus());
        arrivalFlight.setStatus(Flight.STATUS_LANDING);
        assertEquals("Landing", arrivalFlight.getPrintableStatus());
        arrivalFlight.setStatus(Flight.STATUS_TAKING_OFF);
        assertEquals("Taking Off", arrivalFlight.getPrintableStatus());
        arrivalFlight.setStatus(5);
        assertEquals("Crashed!", arrivalFlight.getPrintableStatus());
        arrivalFlight.setStatus(6);
        assertEquals("Unknown", arrivalFlight.getPrintableStatus());
    }

    @Test
    void testSetAirplaneFuel() {
        arrivalFlight.setAirplaneFuel(50);
        assertEquals(50, arrivalFlight.getAirplaneFuel());
        assertFalse(arrivalFlight.hasNoFuel());
        arrivalFlight.setAirplaneFuel(-10); // should not set
        assertEquals(50, arrivalFlight.getAirplaneFuel());
        arrivalFlight.setAirplaneFuel(110); // should not set
        assertEquals(50, arrivalFlight.getAirplaneFuel());
        arrivalFlight.setAirplaneFuel(0); // should not set
        assertEquals(0, arrivalFlight.getAirplaneFuel());
        assertTrue(arrivalFlight.hasNoFuel());
    }

    @Test
    void testStallArrival() {
        int initialFuel = arrivalFlight.getAirplaneFuel();
        assertTrue(initialFuel >= 20);
        assertTrue(initialFuel <= 80);
        assertTrue(arrivalFlight.canStall());
        arrivalFlight.stall();
        assertEquals(initialFuel - 10, arrivalFlight.getAirplaneFuel());
        arrivalFlight.setAirplaneFuel(10);
        assertFalse(arrivalFlight.canStall());
        assertEquals(10, arrivalFlight.getAirplaneFuel());

    }

    @Test
    void testStallDeparture() {
        assertEquals(100, departureFlight.getAirplaneFuel());
        assertTrue(departureFlight.canStall());
        departureFlight.setAirplaneFuel(80);
        assertFalse(departureFlight.canStall());
        departureFlight.stall();
        assertEquals(70, departureFlight.getAirplaneFuel());
    }

    @Test
    void testStallBellowZero() {
        arrivalFlight.setAirplaneFuel(10);
        arrivalFlight.stall();
        assertEquals(0, arrivalFlight.getAirplaneFuel());
        arrivalFlight.stall(); // fuel should not go bellow 0
        assertEquals(0, arrivalFlight.getAirplaneFuel());
    }
}
