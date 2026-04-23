package com.smartcampus.exception;

import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;
import java.util.HashMap;
import java.util.Map;

@Provider
public class LinkedResourceNotFoundExceptionMapper implements ExceptionMapper<LinkedResourceNotFoundException> {
    @Override
    public Response toResponse(LinkedResourceNotFoundException exception) {
        Map<String, String> error = new HashMap<>();
        error.put("error", exception.getMessage());
        
        // Return HTTP 422 Unprocessable Entity
        return Response.status(422)
                .entity(error)
                .type("application/json")
                .build();
    }
}
