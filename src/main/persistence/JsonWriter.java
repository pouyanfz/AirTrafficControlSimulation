package persistence;

import model.Airport;
import model.Event;
import model.EventLog;

import org.json.JSONObject;

import java.io.*;

// Represents a writer that saves the info of an Airport to a Json file
public class JsonWriter {
    private static final int TAB = 4;
    private PrintWriter writer;
    private String destination;

    // EFFECTS: constructs writer to write to destination file
    public JsonWriter(String destination) {
        this.destination = destination;
    }

    // MODIFIES: this
    // EFFECTS: opens writer; throws FileNotFoundException if destination file
    // cannot be opened for writing
    public void open() throws FileNotFoundException {
        writer = new PrintWriter(new File(destination));
    }

    // MODIFIES: this
    // EFFECTS: writes JSON representation of airport to file
    public void write(Airport airport) {
        JSONObject json = airport.toJson();
        saveToFile(json.toString(TAB));
        EventLog.getInstance().logEvent(new Event("\tThe data was saved on the computer at : " + this.destination));
    }

    // MODIFIES: this
    // EFFECTS: closes writer
    public void close() {
        writer.close();
    }

    // MODIFIES: this
    // EFFECTS: writes string to file
    private void saveToFile(String json) {
        writer.print(json);
    }
}
