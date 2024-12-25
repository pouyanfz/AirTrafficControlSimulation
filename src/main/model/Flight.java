package model;

import org.json.JSONObject;

import persistence.Writable;

/* 
 * Represents and stores info of a flight and the airplane assigned to the flight.
 * Each flight has a unique flightNumber 
*/
public class Flight implements Writable {
    private int flightNumber; // Unique
    private String airplaneName;
    private int airplaneFuel;
    private int type; // arrival or departure
    private boolean isComplete;
    private String origin;
    private String destination;
    private String classification;
    private int status;
    private static int lastFlightNumber = 100;
    public static final int MAX_AIRPLANE_FUEL = 100;
    public static final int STATUS_IN_AIR = 1;
    public static final int STATUS_LANDING = 2;
    public static final int STATUS_PARKED = 3;
    public static final int STATUS_TAKING_OFF = 4;
    public static final int STATUS_CRASHED = 5;
    public static final int TYPE_ARIVAL = 1;
    public static final int TYPE_DEPARTURE = -1;

    /*
     * Constructor
     * REQUIRES: type must be equivalent to TYPE_ARIVAL or TYPE_DEPARTURE
     * EFFECTS: sets a unique flightNumber for this flight
     * for departure flights sets airplane fuel to 100%, for arrival flights sets
     * fuel
     * to a random number between [20%,79%]
     */
    public Flight(String airplaneName, String classification, int type) {
        this.flightNumber = lastFlightNumber++; // increments for the next time
        this.airplaneName = airplaneName;
        this.type = type;
        this.isComplete = false;
        this.classification = classification;
        this.status = type == TYPE_ARIVAL ? STATUS_IN_AIR : STATUS_PARKED;
        this.airplaneFuel = type == TYPE_ARIVAL ? generateRandomArrivalFuel() : MAX_AIRPLANE_FUEL;
        this.origin = "No origin set";
        this.destination = "No destination set";
    }

    /*
     * MODIFIES: this
     * EFFECTS: it randomly chooses a fuel level for an airplane between [20%,79%]
     */
    private int generateRandomArrivalFuel() {
        return (int) Math.floor(Math.random() * 0.6 * 100 + 20);
    }

    /*
     * MODIFIES: this
     * EFFECTS: reduces 10% from the airplane fuel
     * will not reduce the fuel amount bellow 0%
     */
    public void stall() {
        this.airplaneFuel = Math.max(0, this.airplaneFuel - 10);
    }

    /*
     * EFFECTS: Determines if the plane can stall or not depending on its type
     * arrival flights need to have >10% and departure flights need >80% fuel to
     * return true
     */
    public boolean canStall() {
        if (this.type == TYPE_ARIVAL) {
            return this.airplaneFuel > 10;
        } else {
            return this.airplaneFuel > 80;
        }
    }

    /*
     * EFFECTS: Returns true if the plane is out of fuel and false otherwise.
     */
    public boolean hasNoFuel() {
        return this.airplaneFuel == 0;
    }

    // ---------- GETTERS & SETTERS ---------- //

    /*
     * EFFECTS: returns string equivalent of current flight status
     */
    public String getPrintableStatus() {
        switch (this.status) {
            case STATUS_IN_AIR:
                return "In Air";
            case STATUS_PARKED:
                return "Parked";
            case STATUS_LANDING:
                return "Landing";
            case STATUS_TAKING_OFF:
                return "Taking Off";
            case STATUS_CRASHED:
                return "Crashed!";
            default:
                return "Unknown";
        }
    }

    /*
     * REQUIRES: the passed status to be between STATUS_IN_AIR and STATUS_TAKING_OFF
     * MODIFIES: this
     * EFFECTS: sets current flight status
     */
    public void setStatus(int status) {
        this.status = status;
    }

    public int getType() {
        return this.type;
    }

    public boolean isComplete() {
        return this.isComplete;
    }

    public void setComplete() {
        this.isComplete = true;
    }

    public String getClassification() {
        return this.classification;
    }

    public void setClassification(String classification) {
        this.classification = classification;
    }

    public int getFlightNumber() {
        return this.flightNumber;
    }

    public String getAirplaneName() {
        return this.airplaneName;
    }

    public void setAirplaneName(String airplaneID) {
        this.airplaneName = airplaneID;
    }

    public int getAirplaneFuel() {
        return this.airplaneFuel;
    }

    public void setAirplaneFuel(int fuel) {
        if (fuel >= 0 && fuel <= 100) {
            this.airplaneFuel = fuel;
        }
    }

    public String getOrigin() {
        return this.origin;
    }

    public void setOrigin(String origin) {
        this.origin = origin;
    }

    public String getDestination() {
        return this.destination;
    }

    public void setDestination(String destination) {
        this.destination = destination;
    }

    /*
     * EFFECTS: Returns a JSON object representing the flight and its details in
     * JSON format.
     */
    @Override
    public JSONObject toJson() {
        JSONObject json = new JSONObject();
        json.put("airplaneName", airplaneName);
        json.put("flightNumber", flightNumber);
        json.put("type", type);
        json.put("classification", classification);
        json.put("origin", origin);
        json.put("status", status);
        json.put("destination", destination);
        json.put("isComplete", isComplete);
        json.put("airplaneFuel", airplaneFuel);
        return json;
    }

}
