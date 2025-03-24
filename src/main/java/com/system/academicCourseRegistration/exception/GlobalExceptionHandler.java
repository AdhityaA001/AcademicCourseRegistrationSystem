package com.system.academicCourseRegistration.exception;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class GlobalExceptionHandler {
	
	@ExceptionHandler(MethodArgumentNotValidException.class)
	protected ResponseEntity<Object> handleMethodArgumentNotValid(MethodArgumentNotValidException ex) {
		    Map<String, Object> body = new HashMap<>();
		    
		    List<String> errors = ex.getBindingResult()
		        .getFieldErrors()
		        .stream()
		        .map(DefaultMessageSourceResolvable::getDefaultMessage)
		        .collect(Collectors.toList());
		    
		    body.put("Status", "Failed");
		    body.put("Error", "Validation Error");
		    body.put("Details", errors);
		    
		    return new ResponseEntity<>(body, HttpStatus.BAD_REQUEST);
		  }
	
	@ExceptionHandler({IllegalArgumentException.class, ValidationException.class})
	protected ResponseEntity<Object> handleMethodArgumentNotValid(Exception ex) {
		    Map<String, String> body = new HashMap<>();
		    
		    body.put("Status", "Failed");
		    body.put("Error", "Validation Error");
		    body.put("Details", ex.getMessage());
		    
		    return new ResponseEntity<>(body, HttpStatus.BAD_REQUEST);
		  }
	
	@ExceptionHandler(ApplicationException.class)
	protected ResponseEntity<Object> handleMethodArgumentNotValid(ApplicationException ex) {
		    Map<String, String> body = new HashMap<>();
		    
		    body.put("Status", "Failed");
		    body.put("Error", "Application Error");
		    body.put("Details", ex.getMessage());
		    
		    return new ResponseEntity<>(body, HttpStatus.INTERNAL_SERVER_ERROR);
		  }

}
