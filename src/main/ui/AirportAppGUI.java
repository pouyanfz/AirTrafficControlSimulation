package ui;

import javax.swing.*;
import javax.swing.border.Border;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import model.Airport;
import model.Flight;
import persistence.JsonReader;
import persistence.JsonWriter;
import java.io.IOException;

import javax.swing.table.DefaultTableModel;
import javax.swing.text.Style;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;

/**
 * Represents the GUI version of the Airport application.
 * Responsible for displaying frames and handling user inputs.
 */
public class AirportAppGUI extends JFrame {
    private Airport airport;
    private JsonWriter jsonWriter;
    private JsonReader jsonReader;
    private static final String JSON_STORE = "./data/airport.json";

    private static final int WIDTH = 800;
    private static final int HEIGHT = 600;

    private JTextPane simulationArea;

    /**
     * Constructor
     * EFFECTS: Sets up fields and starts GUI.
     */
    public AirportAppGUI() {
        super("Airport Traffic Control");
        jsonWriter = new JsonWriter(JSON_STORE);
        jsonReader = new JsonReader(JSON_STORE);
        runGUI();
    }

    /*
     * EFFECTS: Starts displaying the loading screen and then the main menu
     */
    private void runGUI() {
        showSplashScreen();
        showInitialDialog();

        // At this point a valid airport must exist. Otherwise, the app should close
        if (airport != null) {
            setTitle(airport.getName().toUpperCase() + " Airport Traffic Control");
            setupMainScreen();
        }
    }

    /*
     * EFFECTS: it shows the initial splash screen and removes it after a delay
     * 
     * The background image is generated using Microsoft Bing Image Creator.
     * Text is added to it by myself.
     * https://www.bing.com/images/create
     */
    private void showSplashScreen() {
        JWindow splash = new JWindow();
        JLabel splashLabel = new JLabel(new ImageIcon("./data/Images/SplashImage.jpeg"));
        splash.add(splashLabel);
        splash.setSize(WIDTH, WIDTH);
        splash.setLocationRelativeTo(null);
        splash.setVisible(true);

        try {
            Thread.sleep(4000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        splash.setVisible(false);
        splash.dispose();
    }

    /*
     * EFFECTS: it shows a dialog box and asks user if they want to load an existing
     * airport
     * or not based on the choice it either loads the saved airport or set up a new
     * one
     */
    private void showInitialDialog() {
        int dialogChoice;

        // Continue until airport is created or user closes the dialog
        do {
            dialogChoice = JOptionPane.showOptionDialog(
                    null,
                    "Would you like to create a new airport or load an existing one?",
                    "Choose an Option",
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.QUESTION_MESSAGE,
                    null,
                    new String[] { "Create New", "Load Existing" },
                    "Create New");

            if (dialogChoice == JOptionPane.YES_OPTION) {
                createAirportDialog();
            } else if (dialogChoice == JOptionPane.NO_OPTION) {
                loadAirport();
            }
        } while (dialogChoice != JOptionPane.CLOSED_OPTION && airport == null);
    }

    /*
     * EFFECTS : it shows a diolag box and asks for user to insert the Airport Name,
     * Capacity and Number of runways.
     */
    private void createAirportDialog() {
        JPanel panel = new JPanel(new GridLayout(3, 2));
        JTextField nameField = new JTextField();
        JTextField capacityField = new JTextField();
        JTextField runwaysField = new JTextField();

        panel.add(new JLabel("Airport Name:"));
        panel.add(nameField);
        panel.add(new JLabel("Capacity:"));
        panel.add(capacityField);
        panel.add(new JLabel("Number of Runways:"));
        panel.add(runwaysField);

        int dialogChoice;
        boolean success = false;
        do {
            dialogChoice = JOptionPane.showConfirmDialog(
                    null,
                    panel,
                    "Create New Airport",
                    JOptionPane.OK_CANCEL_OPTION);

            if (dialogChoice == JOptionPane.OK_OPTION) {
                success = processUserInput(nameField, capacityField, runwaysField);
            }

        } while (dialogChoice == JOptionPane.OK_OPTION && !success);
    }

    /*
     * MODIFIES : this
     * EFFECTS: Procceses the user input and returns a boolean whether or not the
     * input was valid and the airport was successfully
     * created
     */
    private boolean processUserInput(JTextField nameField, JTextField capacityField, JTextField runwaysField) {
        String airportName = nameField.getText();
        String airportCapacityStr = capacityField.getText();
        String numRunwaysStr = runwaysField.getText();
        if (airportName.isEmpty() || airportCapacityStr.isEmpty() || numRunwaysStr.isEmpty()) {
            JOptionPane.showMessageDialog(null, "Fields cannot be empty.", "Error", JOptionPane.ERROR_MESSAGE);
            return false;
        }
        try {
            if (Integer.parseInt(airportCapacityStr) <= 0 || Integer.parseInt(numRunwaysStr) <= 0) {
                throw new NumberFormatException("Not positive");
            }
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(null, "«Capacity» and «Number of Runways» must be positive integers.",
                    "Error", JOptionPane.ERROR_MESSAGE);
            return false;
        }
        airport = new Airport(airportName, Integer.parseInt(airportCapacityStr), Integer.parseInt(numRunwaysStr));
        String message = String.format("Airport «%s» with a capacity of «%d» and «%d» runways has been created.",
                airportName, Integer.parseInt(airportCapacityStr), Integer.parseInt(numRunwaysStr));
        JOptionPane.showMessageDialog(null, message, "Success", JOptionPane.INFORMATION_MESSAGE);
        return true;
    }

    /*
     * EFFECTS: sets up the main screen panels.
     */
    private void setupMainScreen() {
        initializeFrame();
        JPanel mainPanel = new JPanel(new BorderLayout());

        JPanel simulationPanel = createSimulationPanel();
        mainPanel.add(simulationPanel, BorderLayout.CENTER);

        JPanel buttonPanel = createButtonPanel();
        mainPanel.add(buttonPanel, BorderLayout.SOUTH);

        add(mainPanel);
        finalizeFrame();
    }

    /*
     * EFFECTS: Initialize the frame with the width and height
     */
    private void initializeFrame() {
        setLayout(new BorderLayout());
        setSize(WIDTH, HEIGHT);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    }

    /*
     * EFFECTS: Finalize a frame by making it visible and setting its position
     */
    private void finalizeFrame() {
        setLocationRelativeTo(null);
        setVisible(true);
    }

    /*
     * MODIFIES: this
     * EFFECTS: Creates and configures the panel which shows the simulation results
     */
    private JPanel createSimulationPanel() {
        JPanel simulationPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        simulationPanel.setPreferredSize(new Dimension(WIDTH, HEIGHT * 2 / 3));

        setupTextArea();
        JScrollPane scrollPane = createScrollPane();
        simulationPanel.add(scrollPane);

        return simulationPanel;
    }

    /*
     * MODIFIES: this
     * EFFECTS: Sets up the text area in the simulation panel
     */
    private void setupTextArea() {
        simulationArea = new JTextPane();
        simulationArea.setEditable(false);
        simulationArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
        simulationArea.setBackground(Color.BLACK);
        addDefaultText();
    }

    /*
     * EFFECTS: Adds the welcome text to the simulation area
     */
    private void addDefaultText() {
        StyledDocument doc = simulationArea.getStyledDocument();
        Style style = simulationArea.addStyle("DefaultStyle", null);
        StyleConstants.setForeground(style, new Color(255, 38, 0));

        try {
            doc.insertString(doc.getLength(), getAsciiArt(), style);
            StyleConstants.setForeground(style, new Color(170, 170, 170));
            doc.insertString(doc.getLength(),
                    "\n  Welcome to Airport Traffic Control Simulation!\n\n"
                            + "  • Click 'Add Flight' to add new flights to the queue\n"
                            + "  • Click 'Show All Flights' to view all scheduled flights\n"
                            + "  • Click 'Search Flight' to view the details of a specific flight\n"
                            + "  • Click 'Simulation' to start the traffic control simulation\n"
                            + "  \t• The simulation results will appear here\n"
                            + "  • Click 'Save' to save the current list of the scheduled flights\n"
                            + "  • Click 'Load' to load the list of flights from the memory\n\n"
                            + "  Ready to start...",
                    style);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /*
     * EFFECTS: Creates an asci art for the main menu
     * 
     * Obtained from the ASCII Art Archive
     * https://www.asciiart.eu/vehicles/airplanes
     */
    private String getAsciiArt() {
        return "\t\t\t\t__  _\n"
                + "\t\t\t\t\\ `/ |\n"
                + "\t\t\t\t \\__`!\n"
                + "\t\t\t\t / ,' `-.__________________\n"
                + "\t\t\t\t'-'\\_____                LI`-.\n"
                + "\t\t\t\t   <____()-=O=O=O=O=O=[]====--)\n"
                + "\t\t\t\t     `.___ ,-----,_______...-'\n"
                + "\t\t\t\t          /    .'\n"
                + "\t\t\t\t         /   .'\n"
                + "\t\t\t\t        /  .'\n"
                + "\t\t\t\t        `-'\n";

    }

    /*
     * EFFECTS: Creates and configures the scroll pane which will be used for the
     * simultion field
     */
    private JScrollPane createScrollPane() {
        JScrollPane scrollPane = new JScrollPane(simulationArea);
        scrollPane.setPreferredSize(new Dimension(WIDTH - 20, HEIGHT * 2 / 3 - 50));
        scrollPane.setBackground(Color.BLACK);
        return scrollPane;
    }

    /*
     * EFFECTS: Creates and configures the button panel
     */
    private JPanel createButtonPanel() {
        JPanel buttonPanel = initializeButtonPanel();
        addAllButtons(buttonPanel);
        return buttonPanel;
    }

    /*
     * EFFECTS: Initializes the button panel with layout and styling
     */
    private JPanel initializeButtonPanel() {
        JPanel buttonPanel = new JPanel(new GridLayout(3, 3, 10, 10));
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        buttonPanel.setOpaque(true);
        buttonPanel.setBackground(new Color(84, 86, 92));
        buttonPanel.setPreferredSize(new Dimension(WIDTH - 20, HEIGHT / 3));
        return buttonPanel;
    }

    /*
     * REQUIRES: buttonPanel must not be null
     * MODIFIES: buttonPanel by adding buttons to it
     * EFFECTS: Adds buttons to the adding flight panel
     */
    private void addAllButtons(JPanel buttonPanel) {
        JButton[] buttons = {
                new JButton("Add Flight"), new JButton("Show All Flights"),
                new JButton("Save Airport"), new JButton("Search Flight"),
                new JButton("Start Simulation"), new JButton("Load Airport"),
                new JButton("Exit App")
        };
        buttonStyle(buttons[0], 26, 158, 0);
        buttonStyle(buttons[1], 0, 110, 7);
        buttonStyle(buttons[2], 13, 56, 186);
        buttonStyle(buttons[3], 124, 0, 232);
        buttonStyle(buttons[4], 232, 162, 0);
        buttonStyle(buttons[5], 9, 33, 105);
        buttonStyle(buttons[6], 189, 16, 0);

        for (JButton button : buttons) {
            buttonPanel.add(button); // Add to panel
        }

        buttons[0].addActionListener(e -> showAddFlightWindow());
        buttons[1].addActionListener(e -> viewAllFlights());
        buttons[2].addActionListener(e -> saveAirport());
        buttons[3].addActionListener(e -> searchAirplane());
        buttons[4].addActionListener(e -> startSimulation());
        buttons[5].addActionListener(e -> loadAirport());
        buttons[6].addActionListener(e -> exitApp());
    }

    /*
     * REQUIRES: button must not be null, r , g and b must be between 0 to 255
     * EFFECTS: Adds style to a button with the rgb color
     */
    private void buttonStyle(JButton button, int r, int g, int b) {
        button.setFont(new Font("Arial", Font.BOLD, 20));
        button.setBackground(new Color(r, g, b));
        button.setForeground(Color.WHITE);
        button.setOpaque(true);
        button.setBorderPainted(false);
        button.setOpaque(true);
    }

    /*
     * EFFECTS: By clicking on the exit button, it asks the user if they want to
     * save
     * before exit. If the say yes, it saves the current flight schedule on the
     * computer
     */
    private void exitApp() {
        int option = JOptionPane.showConfirmDialog(null,
                "Do you want to save before exiting?",
                "Exit Confirmation",
                JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);

        if (option == JOptionPane.YES_OPTION) {
            saveAirport();
        }
        this.airport.quitApplication();
        System.exit(0);
    }

    /*
     * MODIFIES: this
     * EFFECTS: loads airport from file
     */
    private void loadAirport() {
        try {
            airport = jsonReader.read();
            String airportName = airport.getName();
            JOptionPane.showMessageDialog(null, "Airport «" + airportName + "» loaded successfully!");
            setTitle(airportName.toUpperCase() + " Airport Traffic Control");
        } catch (IOException e) {
            JOptionPane.showMessageDialog(null, "Failed to load airport: " + e.getMessage(), "Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    /*
     * EFFECTS: saves the airport to file
     */
    private void saveAirport() {
        try {
            jsonWriter.open();
            jsonWriter.write(airport);
            jsonWriter.close();
            String airportName = airport.getName();
            JOptionPane.showMessageDialog(null, "Airport «" + airportName + "» saved successfully!");

        } catch (IOException e) {
            JOptionPane.showMessageDialog(null, "Failed to save the airport: " + e.getMessage(), "Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    /*
     * EFFECTS: Shows the window to add a flight
     */
    private void showAddFlightWindow() {
        JPanel addFlightPanel = createAddFlightPanel();
        addComponentsToAddFlightPanel(addFlightPanel);

        JFrame addFlightFrame = new JFrame("New Flight");
        addFlightFrame.setSize(400, 400);
        addFlightFrame.setContentPane(addFlightPanel);
        addFlightFrame.setLocationRelativeTo(null);
        addFlightFrame.setVisible(true);
    }

    /*
     * EFFECTS: Creates a panel for adding the flight
     */
    private JPanel createAddFlightPanel() {
        JPanel panel = new JPanel();
        Border padding = BorderFactory.createEmptyBorder(10, 10, 10, 10);
        panel.setBorder(padding);
        panel.setLayout(new GridLayout(7, 2));
        return panel;
    }

    /*
     * REQUIRES: addFlightPanel must not be null
     * MODIFIES: addFlightPanel by adding UI components related to flight details
     * EFFECTS: sets up and displays the components for adding a flight, including
     * labels, radio buttons, text fields, combo box, and a button, and attaches
     * an action listener for adding a flight
     */
    private void addComponentsToAddFlightPanel(JPanel addFlightPanel) {
        JLabel flightTypeLabel = new JLabel("Flight Type:");
        JRadioButton arrivalRadioButton = new JRadioButton("Arrival", true);
        JRadioButton departureRadioButton = new JRadioButton("Departure");
        ButtonGroup flightTypeGroup = new ButtonGroup();
        flightTypeGroup.add(arrivalRadioButton);
        flightTypeGroup.add(departureRadioButton);

        JLabel airplaneNameLabel = new JLabel("Airplane Name:");
        JTextField airplaneNameField = new JTextField();

        JLabel classificationLabel = new JLabel("Classification:");
        JComboBox<String> classificationComboBox = new JComboBox<>(new String[] {
                "Commercial", "Passenger", "Cargo", "Private", "Other" });

        JLabel originLabel = new JLabel("Origin:");
        JTextField originField = new JTextField();

        JLabel destinationLabel = new JLabel("Destination:");
        JTextField destinationField = new JTextField();

        JButton addButton = new JButton("Add Flight");
        addButton.addActionListener(e -> handleAddFlightAction(
                addFlightPanel, arrivalRadioButton, airplaneNameField, classificationComboBox,
                originField, destinationField, flightTypeGroup));

        layoutComponents(addFlightPanel, flightTypeLabel, arrivalRadioButton, departureRadioButton,
                airplaneNameLabel, airplaneNameField, classificationLabel, classificationComboBox,
                originLabel, originField, destinationLabel, destinationField, addButton);
    }

    /*
     * MODIFIES: this
     * REQUIRES: frame, flightTypeLabel, arrivalRadioButton, departureRadioButton,
     * airplaneNameLabel, airplaneNameField, classificationLabel,
     * classificationComboBox,
     * originLabel, originField, destinationLabel, destinationField, addButton must
     * not be null
     * EFFECTS: positions and lays out the components for adding a flight on the
     * frame
     */
    private void layoutComponents(JPanel panel, JLabel flightTypeLabel, JRadioButton arrivalRadioButton,
            JRadioButton departureRadioButton, JLabel airplaneNameLabel, JTextField airplaneNameField,
            JLabel classificationLabel, JComboBox<String> classificationComboBox, JLabel originLabel,
            JTextField originField, JLabel destinationLabel, JTextField destinationField, JButton addButton) {
        panel.add(flightTypeLabel);
        panel.add(arrivalRadioButton);
        panel.add(new JLabel()); // Spacer
        panel.add(departureRadioButton);
        panel.add(airplaneNameLabel);
        panel.add(airplaneNameField);
        panel.add(classificationLabel);
        panel.add(classificationComboBox);
        panel.add(originLabel);
        panel.add(originField);
        panel.add(destinationLabel);
        panel.add(destinationField);
        panel.add(new JLabel()); // Spacer
        panel.add(addButton);
    }

    /*
     * MODIFIES: this
     * REQUIRES: addFlightFrame must not be null, and all input fields must contain
     * valid data
     * EFFECTS: adds a flight to the airport if the fields are valid, displays a
     * confirmation message, and clears the input fields for the next entry
     */
    private void handleAddFlightAction(JPanel panel, JRadioButton arrivalRadioButton,
            JTextField airplaneNameField, JComboBox<String> classificationComboBox,
            JTextField originField, JTextField destinationField, ButtonGroup flightTypeGroup) {
        int flightType = arrivalRadioButton.isSelected() ? Flight.TYPE_ARIVAL : Flight.TYPE_DEPARTURE;
        String airplaneName = airplaneNameField.getText();
        String classification = (String) classificationComboBox.getSelectedItem();
        String origin = originField.getText();
        String destination = destinationField.getText();

        if (!airplaneName.isEmpty() && !origin.isEmpty() && !destination.isEmpty()) {
            Flight flight = new Flight(airplaneName, classification, flightType);
            flight.setOrigin(origin);
            flight.setDestination(destination);
            airport.addFlight(flight);

            JOptionPane.showMessageDialog(panel,
                    String.format("Flight \"%s\" with classification \"%s\" is %s the airport.",
                            airplaneName, classification, flightType == 1 ? "Arriving at" : "Departing from"),
                    "Flight Added", JOptionPane.INFORMATION_MESSAGE);

            airplaneNameField.setText("");
            originField.setText("");
            destinationField.setText("");
            flightTypeGroup.clearSelection();
            arrivalRadioButton.setSelected(true);
        } else {
            JOptionPane.showMessageDialog(panel, "All fields must be filled out.",
                    "Input Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    /*
     * EFFECTS: Starts the simulation process with a delay and animated dots
     */
    private void startSimulation() {
        simulationArea.setText(""); // Clear previous content
        SimulationWorker worker = new SimulationWorker(simulationArea, airport);
        worker.execute();
    }

    /*
     * EFFECTS: Search for the specific flight in the flight list. It prints the
     * results in a table or notifies the user that such flight does not exist.
     * search is not case sensitive and it shows the results that contains the input
     */
    private void searchAirplane() {
        String airplaneName = JOptionPane.showInputDialog(null, "Enter the airplane name:", "Search Flight",
                JOptionPane.QUESTION_MESSAGE);

        if (airplaneName != null && !airplaneName.trim().isEmpty()) {
            List<Flight> searchResults = airport.getFlightsQueue().stream()
                    .filter(f -> f.getAirplaneName().toLowerCase().contains(airplaneName.toLowerCase()))
                    .collect(Collectors.toList());
            List<Flight> allFlights = airport.getFlightsQueue();
            List<Flight> searchResult = new ArrayList<>();
            for (Flight f : allFlights) {
                if (f.getAirplaneName().toLowerCase().contains(airplaneName.toLowerCase())) {
                    searchResult.add(f);
                }
            }

            if (!searchResults.isEmpty()) {
                displaySearchResults(searchResults);
            } else {
                JOptionPane.showMessageDialog(null,
                        "No flights found for airplane: " + airplaneName,
                        "Search Results",
                        JOptionPane.INFORMATION_MESSAGE);
            }
        }
    }

    /*
     * EFFECTS: Creates and displays a table showing flight information in a new
     * window
     */
    private void displayFlightTable(List<Flight> flights, String title) {
        String[] columnNames = { "Flight #", "Type", "Airplane", "Origin", "Destination", "Class", "Status",
                "Completed?", "Fuel" };
        DefaultTableModel tableModel = new DefaultTableModel(columnNames, 0);
        for (Flight f : flights) {
            Object[] row = { f.getFlightNumber(),
                    f.getType() == Flight.TYPE_ARIVAL ? "Arrival" : "Departure",
                    f.getAirplaneName(),
                    f.getOrigin(),
                    f.getDestination(),
                    f.getClassification(),
                    f.getPrintableStatus(),
                    f.isComplete() ? "Yes" : "No",
                    f.getAirplaneFuel()
            };
            tableModel.addRow(row);
        }
        JTable flightTable = new JTable(tableModel);
        JScrollPane scrollPane = new JScrollPane(flightTable);

        JFrame flightTableFrame = new JFrame(title);
        flightTableFrame.setSize(800, 400);
        flightTableFrame.add(scrollPane);
        flightTableFrame.setLocationRelativeTo(null);
        flightTableFrame.setVisible(true);
    }

    /**
     * EFFECTS: Shows all flights in the current airport schedule
     */
    private void viewAllFlights() {
        displayFlightTable(airport.getFlightsQueue(), "All Flights");
    }

    /**
     * EFFECTS: Shows all flights in the current airport schedule
     */
    private void displaySearchResults(List<Flight> flights) {
        displayFlightTable(flights, "Search Results");
    }


}
