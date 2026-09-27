package com.actisys.authservice.exception;

import com.actisys.dto.ErrorResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.bind.support.WebExchangeBindException;
import reactor.core.publisher.Mono;

import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalExceptionHandler {

  @ExceptionHandler(UserNotFoundException.class)
  public ResponseEntity<ErrorResponse> handleUserNotFoundException(UserNotFoundException e) {
    ErrorResponse error = new ErrorResponse("USER NOT FOUND", e.getMessage());
    return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
  }

  @ExceptionHandler(EmailAlreadyExistsException.class)
  public ResponseEntity<ErrorResponse> handleEmailAlreadyExistsException(
      EmailAlreadyExistsException e) {
    ErrorResponse error = new ErrorResponse("EMAIL ALREADY EXISTS", e.getMessage());
    return ResponseEntity.status(HttpStatus.CONFLICT).body(error);
  }

  @ExceptionHandler(UserAlreadyExistsException.class)
  public ResponseEntity<ErrorResponse> handleUserAlreadyExistsException(
          UserAlreadyExistsException e) {
    ErrorResponse error = new ErrorResponse("USER ALREADY EXISTS", e.getMessage());
    return ResponseEntity.status(HttpStatus.CONFLICT).body(error);
  }

  @ExceptionHandler(WebExchangeBindException.class)
  public Mono<ResponseEntity<ErrorResponse>> handleWebExchangeBind(WebExchangeBindException ex) {
    String errorMessage = ex.getBindingResult().getFieldErrors().stream()
            .map(error -> error.getField() + ": " + error.getDefaultMessage())
            .collect(Collectors.joining(", "));
    ErrorResponse error = new ErrorResponse("VALIDATION_ERROR", errorMessage);
    return Mono.just(ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error));
  }

  @ExceptionHandler(MethodArgumentNotValidException.class)
  public ResponseEntity<ErrorResponse> handleValidation(MethodArgumentNotValidException ex) {
    String errorMessage = ex.getBindingResult().getFieldErrors().stream()
        .map(error -> error.getField() + ": " + error.getDefaultMessage())
        .collect(Collectors.joining(", "));

    ErrorResponse error = new ErrorResponse("VALIDATION_ERROR", errorMessage);
    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
  }

  @ExceptionHandler(InvalidCredentialsException.class)
  public ResponseEntity<ErrorResponse> handleInvalidCredentialsException(
          InvalidCredentialsException e) {
    ErrorResponse error = new ErrorResponse("INVALID_CREDENTIALS", e.getMessage());
    return ResponseEntity.status(HttpStatus.CONFLICT).body(error);
  }
}

