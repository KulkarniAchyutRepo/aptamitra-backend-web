package com.app.TwoFactorAuthentication.exceptions;

import com.app.TwoFactorAuthentication.exceptions.authExceptions.InvalidOtpException;
import com.app.TwoFactorAuthentication.exceptions.authExceptions.InvalidPasswordException;
import com.app.TwoFactorAuthentication.exceptions.authExceptions.UserAlreadyExistsException;
import com.app.TwoFactorAuthentication.exceptions.authExceptions.UserNotFoundException;
import com.app.TwoFactorAuthentication.exceptions.securityExceptions.ForbiddenAction;
import com.app.TwoFactorAuthentication.exceptions.securityExceptions.ResourceNotFoundException;
import com.app.TwoFactorAuthentication.exceptions.securityExceptions.RoleNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.time.LocalDateTime;

@ControllerAdvice
public class ExceptionsControllerAdvise {

    @ExceptionHandler
    public ResponseEntity<CustomErrorResponse> getErrorResponse(RoleNotFoundException e){
        return buildErrorResponse(e, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler
    public ResponseEntity<CustomErrorResponse> getErrorResponse(ResourceNotFoundException e){
        return buildErrorResponse(e, HttpStatus.BAD_REQUEST);
    }
    @ExceptionHandler
    public ResponseEntity<CustomErrorResponse> getErrorResponse(InvalidOtpException e){
        return buildErrorResponse(e, HttpStatus.BAD_REQUEST);
    }
    @ExceptionHandler
    public ResponseEntity<CustomErrorResponse> getErrorResponse(UserAlreadyExistsException e){
        return buildErrorResponse(e, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler
    public ResponseEntity<CustomErrorResponse> getErrorResponse(UserNotFoundException e){
        return buildErrorResponse(e, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler
    public ResponseEntity<CustomErrorResponse> getErrorResponse(InvalidPasswordException e){
        return buildErrorResponse(e, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler
    public ResponseEntity<CustomErrorResponse> getErrorResponse(ForbiddenAction e){
        return buildErrorResponse(e, HttpStatus.FORBIDDEN);
    }

    @ExceptionHandler
    public ResponseEntity<CustomErrorResponse> getErrorResponse(Exception e) {
        System.out.println("Something went wrong..."+ e);
        return buildErrorResponse(e, HttpStatus.INTERNAL_SERVER_ERROR);
    }


    private ResponseEntity<CustomErrorResponse> buildErrorResponse(Exception e, HttpStatus status) {
        CustomErrorResponse errorResponse = new CustomErrorResponse();
        errorResponse.setSuccess(false);
        errorResponse.setStatus(status.value());
        errorResponse.setMessage(e.getMessage());
        errorResponse.setTimestamp(LocalDateTime.now());
        return new ResponseEntity<>(errorResponse, status);
    }

}
