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
    USER_NOT_FOUND("User not found"),
    ROLE_NOT_FOUND("Role not found"),
    SYSTEM_USERNAME_NOT_ALLOWED("System username cannot be used"),
    ROLE_NAME_ALREADY_EXISTS("Role name already exists"),
    USER_ID_CANT_BE_NULL("User ID can't be null"),
    NOT_AUTHORIZED_TO_PERFORM("User not authorized to perform this action");

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
