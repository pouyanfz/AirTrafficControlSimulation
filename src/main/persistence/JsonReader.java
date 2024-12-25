package persistence;

import model.Airport;
import model.Event;
import model.EventLog;
import model.Flight;
import model.Runway;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;

// Referenced from the JsonSerialization Demo
// https://github.students.cs.ubc.ca/CPSC210/JsonSerializationDemo
// Represents a reader that reads airport from JSON data stored in file
public class JsonReader {
    private String source;

    // EFFECTS: constructs reader to read from source file
    public JsonReader(String source) {
        this.source = source;
    }

    // EFFECTS: reads Airport from file and returns it;
    // throws IOException if an error occurs reading data from file
    public Airport read() throws IOException {
        String jsonData = readFile(source);
        JSONObject jsonObject = new JSONObject(jsonData);
        return parseAirport(jsonObject);
    }

    // EFFECTS: reads source file as string and returns it
    private String readFile(String source) throws IOException {
        return new String(Files.readAllBytes(Paths.get(source)), StandardCharsets.UTF_8);
    }

    // EFFECTS: parses airport from JSON object and returns it
    private Airport parseAirport(JSONObject jsonObject) {
        String name = jsonObject.getString("name");
        int capacity = jsonObject.getInt("maxCapacity");
        int currentCapacity = jsonObject.getInt("currentCapacity");
        int numRunways = jsonObject.getInt("numRunways");
        int[] runwayIds = getRunwayIds(jsonObject.getJSONArray("runways"));
        Airport airport = new Airport(name, capacity, numRunways);
        EventLog.getInstance().logEvent(new Event("\tLoading the airport from saved file: " + source));
        airport.setCurrentCapacity(currentCapacity);
        List<Runway> currentRunways = airport.getRunways();
        for (int i = 0; i < currentRunways.size(); i++) {
            currentRunways.get(i).setRunwayID(runwayIds[i]);
        }

        // Parse flights
        addFlights(airport, jsonObject.getJSONArray("flights"));

        return airport;
    }

    // EFFECTS: parses runway IDs from JSON array and returns an array of integers
    // (runway IDs)
    private int[] getRunwayIds(JSONArray jsonRunways) {
        int[] runwayIds = new int[jsonRunways.length()];
        for (int i = 0; i < jsonRunways.length(); i++) {
            JSONObject nextRunway = jsonRunways.getJSONObject(i);
            int runwayID = nextRunway.getInt("runwayID");
            runwayIds[i] = runwayID;
        }
        return runwayIds;
    }

    // MODIFIES: airport
    // EFFECTS: parses flights from JSON array and adds them to the airport
    private void addFlights(Airport airport, JSONArray jsonFlights) {
        for (Object json : jsonFlights) {
            JSONObject nextFlight = (JSONObject) json;
            addFlight(airport, nextFlight);
        }
    }

    // MODIFIES: airport
    // EFFECTS: parses flight from JSON object and adds it to the airport
    private void addFlight(Airport airport, JSONObject jsonObject) {
        String airplaneName = jsonObject.getString("airplaneName");
        String classification = jsonObject.getString("classification");
        int status = jsonObject.getInt("status");
        int fuel = jsonObject.getInt("airplaneFuel");
        int flightType = jsonObject.getInt("type");
        boolean isComplete = jsonObject.getBoolean("isComplete");
        Flight flight = new Flight(airplaneName, classification, flightType);
        flight.setStatus(status);
        flight.setAirplaneFuel(fuel);
        flight.setOrigin(jsonObject.getString("origin"));
        flight.setDestination(jsonObject.getString("destination"));
        if (isComplete) {
            flight.setComplete();
        }
        airport.addFlight(flight);
    }
}
