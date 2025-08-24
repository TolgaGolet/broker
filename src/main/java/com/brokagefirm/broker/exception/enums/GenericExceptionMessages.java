package com.brokagefirm.broker.exception.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/*
 * Returned HTTP status codes for exceptions are managed from here
 * @com.brokagefirm.broker.exception.BrokerExceptionHandler.handleException
 */
@Getter
@AllArgsConstructor
public enum GenericExceptionMessages {
    AUTHORIZATION_HEADER_MISSING("Authorization header is missing"),
    REFRESH_TOKEN_MISSING("Refresh token is missing"),
    JWT_SUBJECT_MISSING("Token subject is missing"),
    JWT_NOT_VALID("Token is not valid"),
    JWT_EXPIRED("Token is expired"),
    BAD_USERNAME_OR_PASSWORD("Username or password is wrong"),
    WRONG_PASSWORD("Wrong password"),
    USERNAME_ALREADY_EXISTS("Username is already taken. Please choose another username"),
    CUSTOMER_NOT_FOUND("Customer not found"),
    ROLE_NOT_FOUND("Role not found"),
    SYSTEM_USERNAME_NOT_ALLOWED("System username cannot be used"),
    ROLE_NAME_ALREADY_EXISTS("Role name already exists"),
    CUSTOMER_ID_CANT_BE_NULL("Customer ID can't be null"),
    NOT_AUTHORIZED_TO_PERFORM("User not authorized to perform this action"),
    ORDER_NOT_FOUND("Order not found"),
    ORDER_NOT_PENDING("Order is not in PENDING state"),
    ASSET_NOT_FOUND("Asset not found"),
    CANT_CREATE_ORDER_FOR_TRY_ASSET("Can't create order for TRY asset. ADMIN users can create TRY asset via Asset API"),
    TRY_ASSET_NOT_FOUND("TRY asset not found. ADMIN users can create TRY asset via Asset API"),
    NOT_ENOUGH_TRY_USABLE_SIZE("Not enough TRY asset usable size"),
    NOT_ENOUGH_ASSET_USABLE_SIZE("Not enough asset usable size");

    private final String message;

    public static GenericExceptionMessages fromMessage(String message) {
        for (GenericExceptionMessages value : values()) {
            if (value.getMessage().equals(message)) {
                return value;
            }
        }
        return null;
    }
}
