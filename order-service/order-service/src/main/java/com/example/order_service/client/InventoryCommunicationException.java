package com.example.order_service.client;

public class InventoryCommunicationException extends RuntimeException {
    private final int httpStatus;

    public InventoryCommunicationException(String message, int httpStatus) {
        super(message);
        this.httpStatus = httpStatus;
    }

    public InventoryCommunicationException(String message, int httpStatus, Throwable cause) {
        super(message, cause);
        this.httpStatus = httpStatus;
    }

    public int getHttpStatus() {
        return httpStatus;
    }
}
