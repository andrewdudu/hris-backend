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
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.*;

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

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(CommandValidationException.class)
    public Response<Object> commandValidationException(CommandValidationException e) {
        Response<Object> response = new Response<>();
        response.setCode(HttpStatus.BAD_REQUEST.value());
        response.setStatus(HttpStatus.BAD_REQUEST.name());
        response.setErrors(Errors.from(e.getConstraintViolations()));
        return response;
    }

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(IllegalArgumentException.class)
    public Response<Object> badRequest(IllegalArgumentException e) {
        Response<Object> response = new Response<>();
        response.setCode(HttpStatus.BAD_REQUEST.value());
        response.setStatus(HttpStatus.BAD_REQUEST.name());
        response.setErrors(setMessage(e.getMessage()));
        return response;
    }

    private Map<String, List<String>> setMessage(String rawMessages) {
        Map<String, List<String>> messages = new HashMap<>();
        String[] message = rawMessages.split(";");
        for (String msg : message) {
            String[] part = msg.split("=");
            String key = part[0];

            List<String> values = new ArrayList<>();
            values.addAll(Arrays.asList(part[1].split(",")));

            messages.put(key, values);
        }
        return messages;
    }

    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    @ExceptionHandler(RuntimeException.class)
    public Response<Object> InternalError(RuntimeException e) {
        Response<Object> response = new Response<>();

        response.setCode(HttpStatus.INTERNAL_SERVER_ERROR.value());
        response.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.name());
        response.setErrors(setMessage(e.getMessage()));

        return response;
    }

    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    @ExceptionHandler(AccessDeniedException.class)
    public Response<Object> credentialException(AccessDeniedException e) {
        Response<Object> response = new Response<>();
        response.setCode(HttpStatus.UNAUTHORIZED.value());
        response.setStatus(HttpStatus.UNAUTHORIZED.name());

        return response;
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
