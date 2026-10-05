package com.hinal.taskmanagementapi.exception;

import java.util.Map;

public record ValidationErrorResponse(
        int status,
        String error,
        String message,
        Map<String, String> fieldErrors) {
}
