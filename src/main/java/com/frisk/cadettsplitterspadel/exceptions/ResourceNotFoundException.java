package com.frisk.cadettsplitterspadel.exceptions;

public class ResourceNotFoundException extends RuntimeException{

    public ResourceNotFoundException(String message) { super(message);
    }

    public ResourceNotFoundException(String resource, Object id) {
        super(resource + " with id " + id + " not found");
    }

    public ResourceNotFoundException(String resource, String field, Object value) {
        super(resource + " with " + field + " " + value + " not found");
    }
}
