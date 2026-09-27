package com.actisys.inventoryservice.exception;

import com.actisys.dto.ErrorResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

  @ExceptionHandler(IngredientNotFoundException.class)
  public ResponseEntity<ErrorResponse> handleIngredientNotFoundException(
          IngredientNotFoundException e) {
    ErrorResponse error = new ErrorResponse("INVENTORY NOT FOUND", e.getMessage());
    return ResponseEntity.status(HttpStatus.CONFLICT).body(error);
  }

  @ExceptionHandler(SupplyNotFoundException.class)
  public ResponseEntity<ErrorResponse> handleSupplyNotFoundException(
          SupplyNotFoundException e) {
    ErrorResponse error = new ErrorResponse("SUPPLY NOT FOUND", e.getMessage());
    return ResponseEntity.status(HttpStatus.CONFLICT).body(error);
  }

  @ExceptionHandler(SupplierNotFoundException.class)
  public ResponseEntity<ErrorResponse> handleSupplierNotFoundException(
          SupplierNotFoundException e) {
    ErrorResponse error = new ErrorResponse("SUPPLIER NOT FOUND", e.getMessage());
    return ResponseEntity.status(HttpStatus.CONFLICT).body(error);
  }


  @ExceptionHandler(RecipeNotFoundException.class)
  public ResponseEntity<ErrorResponse> handleRecipeNotFoundException(
          RecipeNotFoundException e) {
    ErrorResponse error = new ErrorResponse("RECIPE NOT FOUND", e.getMessage());
    return ResponseEntity.status(HttpStatus.CONFLICT).body(error);
  }

}

