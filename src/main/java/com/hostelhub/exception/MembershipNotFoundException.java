package com.hostelhub.exception;

public class MembershipNotFoundException extends RuntimeException {
    public MembershipNotFoundException(String message) {
        super(message);
    }
}
