package ui;

import javax.swing.*;
import javax.swing.text.*;
import java.awt.*;
import java.util.List;
import model.Airport;

// Referenced from the codementor
// https://www.codementor.io/@isaib.cicourel/swingworker-in-java-du1084lyl

// Referenced from the stackOverflow
// https://stackoverflow.com/questions/23125642/swingworker-in-another-swingworkers-done-method

/**
 * The SimulationWorker class extends SwingWorker to manage and execute the 
 * simulation of an airport's flight processing in the background, updating the 
 * provided JTextPane with the progress.
 */
public class SimulationWorker extends SwingWorker<Void, String> {
    private final JTextPane simulationArea;
    private final Airport airport;

    /**
     * Constructs a SimulationWorker with a specified JTextPane for output and an
     * Airport model for processing.
     */
    public SimulationWorker(JTextPane simulationArea, Airport airport) {
        this.simulationArea = simulationArea;
        this.airport = airport;
    }

    /**
     * 
     * MODIFIES:this
     * REQUIRES: The airport instance should be properly initialized and not null.
     * EFFECTS: Runs the airport simulation in the background, publishing progress
     * messages and log entries to be displayed in the JTextPane.
     */
    @Override
    protected Void doInBackground() throws Exception {
        publish("Starting simulation");
        for (int i = 0; i < 5; i++) {
            publish(".");
            Thread.sleep(500);
        }
        publish("\n");

        while (!airport.isDone()) {
            airport.processFlightsOneRound();
        }
        publish(String.format("\n---------- The report of %s airport ----------\n", airport.getName()));

        for (String log : airport.flushLogEntries()) {
            Thread.sleep(350);
            publish(log + "\n");
        }
        publish(String.format("\n---------- End of the %s airport report ----------\n", airport.getName()));
        return null;
    }

    /**
     * MODIFIES: this
     * EFFECTS: Processes a list of published strings and appends them to the
     * JTextPane with specific styling.
     */
    @Override
    protected void process(List<String> chunks) {
        StyledDocument doc = simulationArea.getStyledDocument();
        Style style = simulationArea.addStyle("ColorStyle", null);

        for (String text : chunks) {
            try {
                if (text.toLowerCase().contains("crashed")) {
                    StyleConstants.setBold(style, true);
                    StyleConstants.setForeground(style, Color.RED);
                } else if (text.contains("Round")) {
                    StyleConstants.setForeground(style, Color.WHITE);
                } else if (text.contains("report")) {
                    StyleConstants.setForeground(style, new Color(0, 221, 255));
                } else {
                    StyleConstants.setForeground(style, Color.GREEN);
                }
                doc.insertString(doc.getLength(), text, style);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        simulationArea.setCaretPosition(simulationArea.getDocument().getLength());
    }

    /**
     * 
     * EFFECTS: Called when the background task is done. Handles any exceptions that occurred during the execution.
     */
    @Override
    protected void done() {
        try {
            get();
        } catch (Exception ex) {
            try {
                StyledDocument doc = simulationArea.getStyledDocument();
                doc.insertString(doc.getLength(),
                        "\nAn error occurred during the simulation: " + ex.getMessage(),
                        simulationArea.getStyle("ColorStyle"));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
