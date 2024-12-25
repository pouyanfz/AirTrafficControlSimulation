package model;

import model.exceptions.*;
import persistence.Writable;

import java.util.List;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import org.json.JSONArray;
import org.json.JSONObject;

/*
 * Represents an airport.
 * Stores info related to runways, flights, and parked airplanes
 */
public class Airport implements Writable {
    private String name;
    private final int maxCapacity;
    private int currentCapacity;
    private List<Flight> flightsQueue;
    private List<Runway> runways;
    private Set<String> parkedAirplanes;
    private List<String> logEntries;
    private int counter = 1;

    /*
     * Constructor
     * REQUIRES: maxCapacity and numRunways must be positive integers
     * EFFECTS: sets the current capacity to maxCapacity
     */
    public Airport(String name, int maxCapacity, int numRunways) {
        this.name = name;
        this.maxCapacity = maxCapacity;
        this.currentCapacity = this.maxCapacity;
        this.flightsQueue = new ArrayList<Flight>();
        this.parkedAirplanes = new HashSet<String>();
        this.runways = new ArrayList<Runway>();
        this.logEntries = new ArrayList<>();
        for (int i = 0; i < numRunways; i++) {
            this.runways.add(new Runway());
            EventLog.getInstance().logEvent(
                    new Event("\tNew runway is added to the airport: Runway" + this.runways.get(i).getRunwayID()));
        }
    }

    /*
     * EFFECTS: indicates whether or not there are any incomplete flight is
     * remaining in the queue of the airport
     */
    public boolean isDone() {
        for (Flight f : this.flightsQueue) {
            if (!f.isComplete()) {
                return false;
            }
        }
        return true;
    }

    /*
     * MODIFIES: this
     * EFFECTS: adds the flight to the queue of flights
     */
    public void addFlight(Flight flight) {
        this.flightsQueue.addLast(flight);
        EventLog.getInstance().logEvent(
                new Event(
                        "\tFlight added to the airport: " + flight.getFlightNumber() + " " + flight.getAirplaneName()));
    }

    /*
     * MODIFIES: this
     * EFFECTS: clears all the runways, then goes through the queue of flights and
     * calls processSingleFlight on them
     */
    public void processFlightsOneRound() {
        logEntries.add(String.format("\n\tRound %d ", counter++));
        for (int i = 0; i < this.flightsQueue.size(); i++) {
            Flight f = this.flightsQueue.get(i);
            if (!f.isComplete()) {
                try {
                    processSingleFlight(f);
                } catch (OutOfFuelException e) {
                    f.setComplete();
                    f.setStatus(Flight.STATUS_CRASHED);
                    logEntries.add(String.format(
                            "\t**** Airplane %s ran out of fuel and crashed!",
                            f.getAirplaneName()));
                    EventLog.getInstance()
                            .logEvent(new Event("\tAirplane ran out of fuel and crashed!: " + f.getFlightNumber() + " "
                                    + f.getAirplaneName()));
                }
            }
        }
        clearRunways();
    }

    /*
     * REQUIRES: flight must not be null
     * MODIFIES: this and flight
     * EFFECTS: Directs the processing of a flight based on its type.
     * If the flight is an arrival, it calls processArrivalFlights, which may stall
     * or land the flight and may throw an OutOfFuelException.
     * If the flight is a departure, it calls processDepartureFlights,
     */
    private void processSingleFlight(Flight flight) throws OutOfFuelException {
        if (flight.getType() == Flight.TYPE_ARIVAL) {
            processArrivalFlights(flight);
        } else {
            processDepartureFlights(flight);
        }
    }

    /*
     * REQUIRES: flight must not be null
     * MODIFIES: this and flight
     * EFFECTS: Attempts to process an arrival flight. Updates its status to
     * STATUS_LANDING.
     * If airport or any runway has no room, tries to stall the flight and throws
     * OutOfFuelException if fuel is critical.
     * If room is available, lands the flight.
     */
    private void processArrivalFlights(Flight flight) throws OutOfFuelException {
        Runway possibleRunway = findFreeRunway();
        StringBuilder log = new StringBuilder();
        log.append(String.format("\tProcessing flight : %s -> ", flight.getAirplaneName()));
        flight.setStatus(Flight.STATUS_LANDING);
        if (this.currentCapacity <= 0 || possibleRunway == null) {
            if (flight.hasNoFuel()) {
                throw new OutOfFuelException(Integer.toString(flight.getFlightNumber()));
            }
            log.append("Stalled due to no space or runway. -> ");
            log.append(String.format("Fuel reduced to %d%%.", flight.getAirplaneFuel()));
            tryStallFlight(flight);
        } else {
            log.append(String.format(("Landing attempt from runway %d -> "), possibleRunway.getRunwayID()));
            log.append("Landed successfully.");
            landArival(flight, possibleRunway);
        }
        this.logEntries.add(log.toString());
    }

    /*
     * REQUIRES: flight must not be null
     * MODIFIES: this and flight
     * EFFECTS: Attempts to process a departure flight. Updates its status to
     * STATUS_TAKING_OFF.
     * If runway is available, proceeds with takeoff.
     */
    private void processDepartureFlights(Flight flight) throws OutOfFuelException {
        Runway possibleRunway = findFreeRunway();
        StringBuilder log = new StringBuilder();
        log.append(String.format("\tProcessing flight : %s -> ", flight.getAirplaneName()));
        flight.setStatus(Flight.STATUS_TAKING_OFF);
        log.append("Takeoff attempt -> ");
        if (possibleRunway == null) {
            log.append("Stalled due to no free runway.");
            tryStallFlight(flight);
        } else {
            log.append(String.format(("Took off successfully from runway %d ."), possibleRunway.getRunwayID()));
            takeoffDeparture(flight, possibleRunway);
        }
        this.logEntries.add(log.toString());
    }

    /*
     * REQUIRES: flight and runway must not be null
     * MODIFIES: this, flight and runway
     * EFFECTS: occupies the runway with the flight, reduces current airport
     * capacity,
     * adds airplane to parkedAirplanes, updates flight status to STATUS_PARKED
     */
    private void landArival(Flight flight, Runway runway) {
        runway.assignFlight(flight);
        this.currentCapacity = Math.max(0, this.currentCapacity - 1);
        this.parkedAirplanes.add(flight.getAirplaneName());
        flight.setStatus(Flight.STATUS_PARKED);
        flight.setComplete();
        EventLog.getInstance().logEvent(
                new Event(
                        "\tFlight landed successfully: " + flight.getFlightNumber() + " " + flight.getAirplaneName()));
    }

    /*
     * REQUIRES: flight and runway must not be null
     * MODIFIES: this, flight and runway
     * EFFECTS: removes airplane from parkedAirplanes, occupies the runway with the
     * flight,
     * increases current airport capacity, updates flight status to STATUS_IN_AIR
     */
    private void takeoffDeparture(Flight flight, Runway runway) {
        this.parkedAirplanes.remove(flight.getAirplaneName());
        runway.assignFlight(flight);
        this.currentCapacity = Math.min(this.maxCapacity, this.currentCapacity + 1);
        flight.setStatus(Flight.STATUS_IN_AIR);
        flight.setComplete();
        EventLog.getInstance().logEvent(new Event(
                "\tFlight departed successfully: " + flight.getFlightNumber() + " " + flight.getAirplaneName()));
    }

    /*
     * REQUIRES: flight must not be null
     * MODIFIES: this and flight
     * EFFECTS: stalls the airplane resulting in a 10% reduction in its fuel.
     * Afterwards, if fuel becomes too low, puts the flight at the begining of the
     * flights queue for emergency landing/take-off, departure flights will refuel
     * in this scenario
     */
    private void tryStallFlight(Flight flight) {
        flight.stall();
        if (!flight.canStall()) {
            if (flight.getType() == Flight.TYPE_DEPARTURE) {
                flight.setAirplaneFuel(Flight.MAX_AIRPLANE_FUEL); // refuel only departure flights
            }
            this.flightsQueue.remove(flight);
            this.flightsQueue.addFirst(flight);
            EventLog.getInstance()
                    .logEvent(new Event("\tFlight was put on the head of the queue: " + flight.getFlightNumber() + " "
                            + flight.getAirplaneName()));
        }
    }

    /*
     * MODIFIES: this
     * EFFECTS: Frees up all runways, adds the completed flight to completed flights
     * list and removes it from flights queue
     */
    private void clearRunways() {
        for (Runway rw : this.runways) {
            if (rw.isOccupied()) {
                rw.freeRunway();
            }
        }
    }

    /*
     * EFFECTS: returns a runway that is not occupied, or returns null if no runway
     * is empty
     */
    private Runway findFreeRunway() {
        for (Runway rw : this.runways) {
            if (!rw.isOccupied()) {
                return rw;
            }
        }
        return null;
    }

    /*
     * MODIFIES: this
     * EFFECTS: will return the list of logs and clears them
     */
    public List<String> flushLogEntries() {
        List<String> logsToReturn = new ArrayList<>(this.logEntries);
        this.logEntries = new ArrayList<>();
        return logsToReturn;
    }

    /**
     * EFFECTS: Prints the event log on the console when user quits the application
     */
    public void quitApplication() {
        System.out.println("\nApplication is shutting down. \nEvent log:");

        EventLog eventLog = EventLog.getInstance();
        for (model.Event event : eventLog) {
            System.out.println(event);
        }

        System.out.println("End of log\nGoodbye!");
    }

    // ---------- GETTERS & SETTERS ---------- //

    public String getName() {
        return this.name;
    }

    public int getCurrentCapacity() {
        return this.currentCapacity;
    }

    public void setCurrentCapacity(int capacity) {
        this.currentCapacity = capacity;
    }

    public List<Runway> getRunways() {
        return this.runways;
    }

    public List<Flight> getFlightsQueue() {
        return this.flightsQueue;
    }

    /*
     * EFFECTS: Returns a JSON object representing the airport, including its name,
     * maximum capacity, current capacity, number of runways, and the lists of
     * runways
     * and flights in JSON format.
     */
    @Override
    public JSONObject toJson() {
        JSONObject json = new JSONObject();
        json.put("name", name);
        json.put("maxCapacity", maxCapacity);
        json.put("currentCapacity", currentCapacity);
        json.put("numRunways", this.runways.size());
        json.put("runways", runwaysToJson());
        json.put("flights", flightsToJson());
        return json;
    }

    /*
     * EFFECTS: Returns a JSON array representing the runways in the airport.
     */
    private JSONArray runwaysToJson() {
        JSONArray jsonArray = new JSONArray();
        for (Runway runway : runways) {
            jsonArray.put(runway.toJson());
        }
        return jsonArray;
    }

    /*
     * EFFECTS: Returns a JSON array representing the flights in the airport's
     * queue.
     */
    private JSONArray flightsToJson() {
        JSONArray jsonArray = new JSONArray();
        for (Flight flight : this.flightsQueue) {
            jsonArray.put(flight.toJson());
        }
        return jsonArray;
    }

}
