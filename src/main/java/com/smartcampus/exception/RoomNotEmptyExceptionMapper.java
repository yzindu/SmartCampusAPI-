package com.smartcampus.exception;

import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;
import java.util.HashMap;
import java.util.Map;

@Provider
public class RoomNotEmptyExceptionMapper implements ExceptionMapper<RoomNotEmptyException> {
    @Override
    public Response toResponse(RoomNotEmptyException exception) {
        Map<String, String> error = new HashMap<>();
        error.put("error", exception.getMessage());
        return Response.status(Response.Status.CONFLICT)
                .entity(error)
                .type("application/json")
                .build();
    }
}
