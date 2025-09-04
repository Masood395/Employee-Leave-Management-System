package com.project.leavemanagement.exception;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import com.fasterxml.jackson.databind.exc.InvalidFormatException;
import com.project.leavemanagement.dto.ApiResponse;

@RestControllerAdvice
public class GlobalExceptionHandler {

	private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

	@ExceptionHandler(ResourceNotFoundException.class)
	public ResponseEntity<ApiResponse<?>> handleResourceNotFound(ResourceNotFoundException ex) {
		log.error(ex.getMessage());
		ApiResponse<?> errordetails = new ApiResponse<>(false, ex.getMessage());
		return new ResponseEntity<>(errordetails, HttpStatus.NOT_FOUND);
	}
	@ExceptionHandler(IllegalArgumentException.class)
	public ResponseEntity<ApiResponse<?>> handleIllegalArgument(IllegalArgumentException ex) {
		log.error(ex.getMessage());
		ApiResponse<?> errordetails = new ApiResponse<>(false, ex.getMessage());
		return new ResponseEntity<>(errordetails, HttpStatus.OK);
	}

	@ExceptionHandler(HttpMessageNotReadableException.class)
	public ResponseEntity<?> handleHttpMessageNotReadable(HttpMessageNotReadableException ex, WebRequest request) {
		log.error(ex.getMessage());
		String errorMessage = "Invalid request body";
        if (ex.getCause() instanceof InvalidFormatException) {
            InvalidFormatException ife = (InvalidFormatException) ex.getCause();
            
            if (ife.getTargetType() != null && ife.getTargetType().isEnum()) {
                String enumValues = Arrays.toString(ife.getTargetType().getEnumConstants());
                errorMessage = String.format("Invalid enum value. Acceptable values are: %s", 
                    enumValues);
            }
            else if (LocalDate.class.equals(ife.getTargetType())) {
                errorMessage = "Invalid date format. Please use ISO format (yyyy-MM-dd)";
            }
        }
        else if (ex.getCause() instanceof DateTimeParseException) {
            errorMessage = "Invalid date format. Please use ISO format (yyyy-MM-dd)";
        }
        else if (ex.getMessage() != null && ex.getMessage().startsWith("JSON parse error")) {
            errorMessage = "Malformed JSON request";
        }
		ApiResponse<?> errorDetails = new ApiResponse<>(false,errorMessage);

		
//		String allowedStatuses = Arrays.stream(Role.values()).map(Enum::name).collect(Collectors.joining(", "));
//		ApiResponse<?> errorDetails = new ApiResponse(false,
//				"Invalid status. Allowed: " + allowedStatuses/* +ex.getMessage() */);
		return new ResponseEntity<>(errorDetails, HttpStatus.BAD_REQUEST);
	}
	@ExceptionHandler(MethodArgumentTypeMismatchException.class)
	public ResponseEntity<?> handleMethodArgumentTypeMismatch(MethodArgumentTypeMismatchException ex) {
		log.error("Parameter type mismatch: {}", ex.getMessage());
		String message;
		if (ex.getRequiredType() != null && ex.getRequiredType().isEnum()) {
			Object[] enumConstants = ex.getRequiredType().getEnumConstants();
			message = String.format("Invalid value '%s' for parameter '%s'. Allowed values are: %s", ex.getValue(),
					ex.getName(), Arrays.toString(enumConstants));
		} else {
			message = String.format("Invalid value '%s' for parameter '%s'. Expected type: %s", ex.getValue(),
					ex.getName(), ex.getRequiredType() != null ? ex.getRequiredType().getSimpleName() : "Unknown");
		}
		ApiResponse<?> errorDetails = new ApiResponse<>(false, message);
		return new ResponseEntity<>(errorDetails, HttpStatus.BAD_REQUEST);
	}

	@ExceptionHandler(MethodArgumentNotValidException.class)
	public ResponseEntity<?> handleValidationException(MethodArgumentNotValidException ex) {
		Map<String, String> errors = ex.getBindingResult().getFieldErrors().stream().collect(Collectors
				.toMap(err -> err.getField(), err -> err.getDefaultMessage(), (existing, replacement) -> existing));

		ApiResponse<?> errordetails = new ApiResponse<>(false, errors);
		return new ResponseEntity<>(errordetails, HttpStatus.BAD_REQUEST);
	}

	@ExceptionHandler(Exception.class)
	public ResponseEntity<?> handleGlobalExc(Exception e) {
		log.error(e.getMessage());
		ApiResponse<?> errordetails = new ApiResponse<>(false, e.getMessage());
		return new ResponseEntity<>(errordetails, HttpStatus.INTERNAL_SERVER_ERROR);
	}
}
