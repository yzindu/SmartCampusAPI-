package com.smartcampus.exception;

import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;
import java.util.HashMap;
import java.util.Map;

@Provider
public class SensorUnavailableExceptionMapper implements ExceptionMapper<SensorUnavailableException> {
    @Override
    public Response toResponse(SensorUnavailableException exception) {
        Map<String, String> error = new HashMap<>();
        error.put("error", exception.getMessage());
        return Response.status(Response.Status.FORBIDDEN)
                .entity(error)
                .type("application/json")
                .build();
    }
}
