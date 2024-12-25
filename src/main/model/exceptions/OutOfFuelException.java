package model.exceptions;

public class OutOfFuelException extends Exception {
    public OutOfFuelException(String flightNumber) {
        super(flightNumber);
    }
    
}
