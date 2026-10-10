package com.hostelhub.exception;

public class GroupAlreadyExistsException extends RuntimeException {
    public GroupAlreadyExistsException(String message) {
        super(message);
    }
}
