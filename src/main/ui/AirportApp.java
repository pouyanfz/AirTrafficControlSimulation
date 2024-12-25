package ui;

import persistence.JsonReader;
import persistence.JsonWriter;

import java.io.FileNotFoundException;
import java.io.IOException;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import model.Airport;
import model.Flight;

/**
 * Represents the console-based version of the Airport application.
 * Responsible for handling menus and user inputs.
 */
public class AirportApp {
    private Airport airport;
    private Scanner input;
    private static final String JSON_STORE = "./data/airport.json";
    private JsonWriter jsonWriter;
    private JsonReader jsonReader;

    /*
     * Constructor
     * MODIFIES: this
     * EFFECTS: Instantiate the scanner object and run the airport application
     */
    public AirportApp() throws FileNotFoundException {
        input = new Scanner(System.in);
        jsonWriter = new JsonWriter(JSON_STORE);
        jsonReader = new JsonReader(JSON_STORE);
        runAirport();
    }

    /*
     * MODIFIES: this
     * EFFECTS: Shows the user input menu and aks the user to choose an option
     * choosing 0 will exit the menue
     */
    private void runAirport() {
        boolean airportCreated = false;

        while (!airportCreated) {
            airportCreated = createAirport();
        }

        boolean keepGoing = true;
        while (keepGoing) {
            displayFlightMenu();
            int command = input.nextInt();
            input.nextLine();
            keepGoing = handleCommand(command);
        }
        System.out.println("\nProgram is terminating....");
    }

    /*
     * MODIFIES: this
     * EFFECTS: Performs the corresponding action based on the menu command number
     * returns true if should keep going and false if should terminate the program
     */
    private boolean handleCommand(int command) {
        if (command == 0) {
            return false;
        } else if (command == 1) {
            addFlight();
        } else if (command == 2) {
            printFlight();
        } else if (command == 3) {
            startSimulation();
        } else if (command == 4) {
            specificPlane();
        } else if (command == 5) {
            saveAirport();
        } else if (command == 6) {
            loadAirport();
        } else {
            System.err.println("\nInvalid input! Please select a valid option.");
        }
        return true;
    }

    // MODIFIES: this
    // EFFECTS: loads airport from file
    private void loadAirport() {
        try {
            airport = jsonReader.read();
            System.out.println("\nLoaded " + airport.getName() + " from " + JSON_STORE);
        } catch (IOException e) {
            System.out.println("\nUnable to read from file: " + JSON_STORE);
        }
    }

    // EFFECTS: saves the airport to file
    private void saveAirport() {
        try {
            jsonWriter.open();
            jsonWriter.write(airport);
            jsonWriter.close();
            System.out.println("\nSaved " + airport.getName() + " to " + JSON_STORE);
        } catch (FileNotFoundException e) {
            System.out.println("\nUnable to write to file: " + JSON_STORE);
        }
    }

    /*
     * MODIFIES: this
     * EFFECTS: find the specific plane that user enters and prints the report
     */
    private void specificPlane() {
        String airplaneName;
        System.out.print("\n\tEnter the airplane name: ");
        airplaneName = input.nextLine();
        List<Flight> flightsToPrint = new ArrayList<>();
        for (Flight f : this.airport.getFlightsQueue()) {
            if (f.getAirplaneName().toLowerCase().equals(airplaneName.toLowerCase())) {
                flightsToPrint.add(f);
            }
        }
        if (!flightsToPrint.isEmpty()) {
            printFlight(flightsToPrint);
        } else {
            System.out.println("\nSorry...no flights were found for that airplane");
        }
    }

    /*
     * EFFECTS: Goes through all the flights that was added to the airport and
     * prints them
     */
    private void printFlight() {
        System.out.printf("\n\t%-12s%-12s%-15s%-15s%-15s%-15s%-15s%-15s%-10s\n",
                "Flight #", "Type", "Airplane", "Origin", "Destination", "Class", "Status", "Completed?", "Fuel");
        System.out.print("\t----------------------------------------------------------------");
        System.out.println("------------------------------------------------------");
        for (Flight f : this.airport.getFlightsQueue()) {
            System.out.printf("\t%-12d%-12s%-15s%-15s%-15s%-15s%-15s%-15s%-5d\n",
                    f.getFlightNumber(),
                    f.getType() == Flight.TYPE_ARIVAL ? "Arrival" : "Departure",
                    f.getAirplaneName(),
                    f.getOrigin(),
                    f.getDestination(),
                    f.getClassification(),
                    f.getPrintableStatus(),
                    f.isComplete() ? "Yes" : "No",
                    f.getAirplaneFuel());
        }
    }

    /*
     * EFFECTS: Prints the list of flights that is passed in as the parameter
     */
    private void printFlight(List<Flight> flights) {
        System.out.printf("\n\t%-12s%-12s%-15s%-15s%-15s%-15s%-15s%-15s%-10s\n",
                "Flight #", "Type", "Airplane", "Origin", "Destination", "Class", "Status", "Completed?", "Fuel");
        System.out.print("\t----------------------------------------------------------------");
        System.out.println("------------------------------------------------------");
        for (Flight f : flights) {
            System.out.printf("\t%-12d%-12s%-15s%-15s%-15s%-15s%-15s%-15s%-5d\n",
                    f.getFlightNumber(),
                    f.getType() == Flight.TYPE_ARIVAL ? "Arrival" : "Departure",
                    f.getAirplaneName(),
                    f.getOrigin(),
                    f.getDestination(),
                    f.getClassification(),
                    f.getPrintableStatus(),
                    f.isComplete() ? "Yes" : "No",
                    f.getAirplaneFuel());
        }
    }

    /*
     * EFFECTS: Display the flight addition and simulation menu
     */
    private void displayFlightMenu() {
        System.out.println("\nSelect from:");
        System.out.println("\t1 -> Add a flight");
        System.out.println("\t2 -> See the list of all flights");
        System.out.println("\t3 -> Start simulation");
        System.out.println("\t4 -> See the specific plane information");
        System.out.println("\t5 -> Save the airport and its flights to file");
        System.out.println("\t6 -> Load the airport from file");
        System.out.println("\t0 -> Quit");
        System.out.print("\tYour input: ");
    }

    /*
     * REQUIRES: input must not be null
     * MODIFIES: this
     * EFFECTS: Creates a new airport instance
     */
    private boolean createAirport() {
        System.out.println("\n\n---------- Welcome to Air Traffic Controller Simulation ----------");
        System.out.print("\n\tEnter the Airport Name: ");
        String airportName = input.nextLine();
        System.out.print("\tEnter the Airport capacity: ");
        int airportCapacity = input.nextInt();
        System.out.print("\tEnter the number of runways: ");
        int numRunways = input.nextInt();
        input.nextLine();

        airport = new Airport(airportName, airportCapacity, numRunways);
        System.out.printf("\nAirport \"%s\" with capacity \"%d\" and \"%d\" runway(s) created!\n", airportName,
                airportCapacity, numRunways);
        return true;
    }

    /*
     * REQUIRES: input must not be null
     * MODIFIES: this
     * EFFECTS: adds a flight to the airport
     */
    private void addFlight() {
        String origin;
        String destination;
        System.out.print("\n\tEnter the flight type \"A\" for Arrival and \"D\" for Departure: ");
        String flightTypeInput = input.nextLine();
        int flightType = flightTypeInput.toLowerCase().equals("a") ? 1 : -1;
        System.out.print("\tEnter the airplane name: ");
        String airplaneName = input.nextLine();
        System.out.print("\tEnter the classification (e.g. Commercial, Cargo, etc.): ");
        String classification = input.nextLine();
        System.out.print("\tEnter the origin of the flight: ");
        origin = input.nextLine();
        System.out.print("\tEnter the destination of the flight: ");
        destination = input.nextLine();
        Flight flight = new Flight(airplaneName, classification, flightType);
        flight.setOrigin(origin);
        flight.setDestination(destination);
        airport.addFlight(flight);
        System.out.printf("\nFlight \"%s\" with classification \"%s\" is %s the airport.\n",
                airplaneName,
                classification,
                flightType == 1 ? "Arriving at" : "Departing from");
    }

    /*
     * EFFECTS: Starts the simulation process with a delay and animated dots
     */
    private void startSimulation() {
        System.out.print("\nStarting simulation");
        try {
            for (int i = 0; i < 5; i++) {
                System.out.print(".");
                Thread.sleep(500);
            }
            System.out.println();
        } catch (InterruptedException e) {
            System.err.println("\nAn error occurred during the simulation startup.");
        }
        while (!airport.isDone()) {
            airport.processFlightsOneRound();
        }
        printLog();
    }

    /*
     * EFFECTS: Prints the log of the simulation
     */
    private void printLog() {
        System.out.printf("\n---------- The report of %s airport ----------\n", airport.getName());
        try {
            for (String log : airport.flushLogEntries()) {
                Thread.sleep(500);
                System.out.println(log);
            }
        } catch (InterruptedException e) {
            System.err.println("\nAn error occurred while printing the log.");
        }
    }
}
