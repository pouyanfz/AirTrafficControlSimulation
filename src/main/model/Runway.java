package model;

import org.json.JSONObject;

import persistence.Writable;

/*
 * Represents a runway of an airport used for taking off and landing.
 * Each runway has a unique runwayID.
 */
public class Runway implements Writable {
    private int runwayID; // Unique
    private Flight currentFlight;
    private static int lastRunwayID = 1;

    /*
     * Constructor
     * EFFECTS: sets currentFlight to null, sets a unique runwayID for this runway
     */
    public Runway() {
        this.runwayID = lastRunwayID++; // increments for the next time
        this.currentFlight = null;
    }

    /*
     * REQUIRES: the passed flight must not be null
     * MODIFIES: this
     * EFFECTS: it sets the flight for the runway and makes it occupied
     */
    public void assignFlight(Flight currentFlight) {
        this.currentFlight = currentFlight;
    }

    /*
     * MODIFIES: this
     * EFFECTS: it removes the assigned flight from the runway and makes it
     * unoccupied
     */
    public void freeRunway() {
        this.currentFlight = null;
    }

    /*
     * EFFECTS: indicates whether or not this runway is occupied
     */
    public boolean isOccupied() {
        return currentFlight != null;
    }

    // ---------- GETTERS & SETTERS ---------- //

    public int getRunwayID() {
        return runwayID;
    }

    public Flight getCurrentFlight() {
        return currentFlight;
    }

    public void setRunwayID(int runwayID) {
        this.runwayID = runwayID;
    }

    /*
     * EFFECTS: Returns a JSON object representing the runway and its details in
     * JSON format.
     */
    @Override
    public JSONObject toJson() {
        JSONObject json = new JSONObject();
        json.put("runwayID", runwayID);
        json.put("occupied", isOccupied());
        return json;
    }

}
