package com.smartcampus.exception;

import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

@Provider
public class GenericExceptionMapper implements ExceptionMapper<Throwable> {
    
    private static final Logger LOGGER = Logger.getLogger(GenericExceptionMapper.class.getName());

    @Override
    public Response toResponse(Throwable exception) {
        // Log the actual exception for internal debugging to avoid exposing traces to clients
        LOGGER.log(Level.SEVERE, "Unexpected internal server error", exception);

        // Return a generic error message to the client
        Map<String, String> error = new HashMap<>();
        error.put("error", "An unexpected internal server error occurred. Please try again later.");
        
        return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                .entity(error)
                .type("application/json")
                .build();
    }
}
