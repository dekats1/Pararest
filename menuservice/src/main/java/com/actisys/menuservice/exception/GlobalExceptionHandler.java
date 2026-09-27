package com.actisys.menuservice.exception;

import com.actisys.dto.ErrorResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalExceptionHandler {

  @ExceptionHandler(CategoryNotFoundException.class)
  public ResponseEntity<ErrorResponse> handleCategoryNotFoundException(
          CategoryNotFoundException e) {
    ErrorResponse error = new ErrorResponse("CATEGORY NOT FOUND", e.getMessage());
    return ResponseEntity.status(HttpStatus.CONFLICT).body(error);
  }

  @ExceptionHandler(MenuItemNotFoundException.class)
  public ResponseEntity<ErrorResponse> handleMenuItemNotFoundException(
          MenuItemNotFoundException e) {
    ErrorResponse error = new ErrorResponse("MENU ITEM NOT FOUND", e.getMessage());
    return ResponseEntity.status(HttpStatus.CONFLICT).body(error);
  }

  @ExceptionHandler(CategoryAlreadyExistsException.class)
  public ResponseEntity<ErrorResponse> handleCategoryAlreadyExistsException(
          CategoryAlreadyExistsException e) {
    ErrorResponse error = new ErrorResponse("CATEGORY ALREADY EXISTS", e.getMessage());
    return ResponseEntity.status(HttpStatus.CONFLICT).body(error);
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

