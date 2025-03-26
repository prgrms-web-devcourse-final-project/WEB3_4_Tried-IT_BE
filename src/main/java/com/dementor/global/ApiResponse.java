package com.dementor.global;

public record ApiResponse<T>(
    boolean success,
    String message,
    T data,
    ErrorResponse error
) {
    public static <T> ApiResponse<T> success(String message, T data) {
        return new ApiResponse<>(true, message, data, null);
    }

    public static <T> ApiResponse<T> failure(String message, ErrorResponse error) {
        return new ApiResponse<>(false, message, null, error);
    }
}

record ErrorResponse(
    String code,
    String details
) {
    public static ErrorResponse notFound(String details) {
        return new ErrorResponse("NOT_FOUND", details);
    }

    public static ErrorResponse validationError(String details) {
        return new ErrorResponse("VALIDATION_ERROR", details);
    }

    public static ErrorResponse serverError(String details) {
        return new ErrorResponse("SERVER_ERROR", details);
    }
} 