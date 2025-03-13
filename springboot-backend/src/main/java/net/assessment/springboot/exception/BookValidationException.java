package net.assessment.springboot.exception;

public class BookValidationException extends RuntimeException {
    public BookValidationException(String message) {
        super(message);
    }
}
