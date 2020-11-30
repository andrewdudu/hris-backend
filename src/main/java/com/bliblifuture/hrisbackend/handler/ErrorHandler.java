package com.bliblifuture.hrisbackend.handler;

import com.blibli.oss.command.exception.CommandValidationException;
import com.blibli.oss.common.error.ErrorWebFluxControllerHandler;
import com.blibli.oss.common.error.Errors;
import com.blibli.oss.common.response.Response;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.springframework.context.MessageSource;
import org.springframework.context.MessageSourceAware;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@RestControllerAdvice
public class ErrorHandler implements ErrorWebFluxControllerHandler, MessageSourceAware {

    @Getter
    @Setter
    private MessageSource messageSource;

    @Override
    public Logger getLogger() {
        return log;
    }

//    public Map<String, List<String>> returnError(){
//
//    }

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(CommandValidationException.class)
    public Response<Object> commandValidationException(CommandValidationException e) {
        this.getLogger().warn(CommandValidationException.class.getName(), e);

        Response<Object> response = new Response<>();
        response.setCode(HttpStatus.BAD_REQUEST.value());
        response.setStatus(HttpStatus.BAD_REQUEST.name());
        response.setErrors(Errors.from(e.getConstraintViolations()));
        return response;
    }

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(IOException.class)
    public Response<Object> IOException(IOException e) {
        Response<Object> response = new Response<>();

        response.setCode(HttpStatus.BAD_REQUEST.value());
        response.setStatus(HttpStatus.BAD_REQUEST.name());

        return getErrorMessage(response, "credential", e.getMessage(), e);
    }

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(SecurityException.class)
    public Response<Object> credentialException(SecurityException e) {
        Response<Object> response = new Response<>();

        response.setCode(HttpStatus.BAD_REQUEST.value());
        response.setStatus(HttpStatus.BAD_REQUEST.name());

        return getErrorMessage(response, "credential", e.getMessage(), e);
    }

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(IllegalArgumentException.class)
    public Response<Object> notAcceptableRequest(IllegalArgumentException e) {
        Response<Object> response = new Response<>();

        response.setCode(HttpStatus.NOT_ACCEPTABLE.value());
        response.setStatus(HttpStatus.NOT_ACCEPTABLE.name());

        return getErrorMessage(response, "message", e.getMessage(), e);
    }

    @ResponseStatus(HttpStatus.NOT_FOUND)
    @ExceptionHandler(NullPointerException.class)
    public Response<Object> nullPointerException(NullPointerException e) {
        Response<Object> response = new Response<>();
        response.setCode(HttpStatus.NOT_FOUND.value());
        response.setStatus(HttpStatus.NOT_FOUND.name());

        return getErrorMessage(response, "message", e.getMessage(), e);
    }

    private Response<Object> getErrorMessage(Response<Object> response, String key, String message, Exception e) {
        Map<String, List<String>> errors = new HashMap<>();
        List<String> messages = new ArrayList<>();
        messages.add(message);

        errors.put(key, messages);
        response.setErrors(errors);

        return response;
    }
}
