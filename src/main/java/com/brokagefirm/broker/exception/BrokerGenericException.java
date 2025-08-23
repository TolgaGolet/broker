package com.brokagefirm.broker.exception;

public class BrokerGenericException extends Exception {
    public BrokerGenericException(String errorMessage, Throwable err) {
        super(errorMessage, err);
    }

    public BrokerGenericException(String errorMessage) {
        super(errorMessage);
    }
}
