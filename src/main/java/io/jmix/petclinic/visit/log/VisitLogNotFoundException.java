package io.jmix.petclinic.visit.log;

public class VisitLogNotFoundException extends RuntimeException {
    public VisitLogNotFoundException(String id) {
        super("Visit Log with ID " + id + " not found");
    }
}
