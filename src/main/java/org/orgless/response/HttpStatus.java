package org.orgless.response;

public enum HttpStatus {
    OK, BAD_REQUEST, INTERNAL_SERVER_ERROR, NOT_FOUND;

    public int getStatusCode() {
        return switch (this) {
            case OK -> 200;
            case BAD_REQUEST -> 400;
            case INTERNAL_SERVER_ERROR -> 500;
            case NOT_FOUND -> 404;
            default -> -1;
        };
    }
    public int incDec(int x) {
        return switch (this) {
            case OK -> 0;
            case BAD_REQUEST -> 0;
            case INTERNAL_SERVER_ERROR -> 0;
            case NOT_FOUND -> 0;
        };
    }
}