package com.actisys.orderservice.exception;

import com.actisys.dto.ErrorResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalExceptionHandler {

  @ExceptionHandler(OrderNotFoundException.class)
  public ResponseEntity<ErrorResponse> handleOrderNotFoundException(OrderNotFoundException e) {
    ErrorResponse error = new ErrorResponse("ORDER NOT FOUND", e.getMessage());
    return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
  }

  @ExceptionHandler(CanNotCancelOrderNowException.class)
  public ResponseEntity<ErrorResponse> handleOrderCanNotBeCancelledException(CanNotCancelOrderNowException e) {
    ErrorResponse error = new ErrorResponse("CAN NOT CANCELLED ORDER NOW", e.getMessage());
    return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
  }

  @ExceptionHandler(OrderInfoNotFoundException.class)
  public ResponseEntity<ErrorResponse> handleOrderInfoNotFoundException(OrderInfoNotFoundException e) {
    ErrorResponse error = new ErrorResponse("ORDER INFO NOT FOUND", e.getMessage());
    return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
  }

  @ExceptionHandler(MethodArgumentNotValidException.class)
  public ResponseEntity<ErrorResponse> handleValidation(MethodArgumentNotValidException ex) {
    String errorMessage = ex.getBindingResult().getFieldErrors().stream()
        .map(error -> error.getField() + ": " + error.getDefaultMessage())
        .collect(Collectors.joining(", "));

    ErrorResponse error = new ErrorResponse("VALIDATION_ERROR", errorMessage);
    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
  }
}

