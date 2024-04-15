package com.siac.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.context.request.WebRequest;

import com.siac.exception.StorageException;


@ControllerAdvice
public class StorageExceptionHandler {


    @ExceptionHandler(StorageException.class)
    @ResponseBody
    public ResponseEntity<Object> handleStorageException(StorageException ex, WebRequest request) {
        ErrorResponse errorResponse = new ErrorResponse("Failed to store file", ex.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
    }

    static class ErrorResponse {
        private String errorTitle;
        private String errorMessage;

        public ErrorResponse(String errorTitle, String errorMessage) {
            this.errorTitle = errorTitle;
            this.errorMessage = errorMessage;
        }

        public String getErrorTitle() {
            return errorTitle;
        }

        public void setErrorTitle(String errorTitle) {
            this.errorTitle = errorTitle;
        }

        public String getErrorMessage() {
            return errorMessage;
        }

        public void setErrorMessage(String errorMessage) {
            this.errorMessage = errorMessage;
        }
    }
}
