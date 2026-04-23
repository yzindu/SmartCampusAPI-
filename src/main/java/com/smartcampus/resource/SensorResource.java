package com.smartcampus.resource;

import com.smartcampus.exception.LinkedResourceNotFoundException;
import com.smartcampus.model.Room;
import com.smartcampus.model.Sensor;
import com.smartcampus.store.DataStore;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Path("/sensors")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class SensorResource {

    private Map<String, Sensor> sensors = DataStore.getInstance().getSensors();
    private Map<String, Room> rooms = DataStore.getInstance().getRooms();

    @POST
    public Response createSensor(Sensor sensor) {
        if (sensor == null || sensor.getId() == null || sensor.getId().trim().isEmpty()) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Sensor must have a valid ID");
            return Response.status(Response.Status.BAD_REQUEST).entity(error).build();
        }

        if (sensors.containsKey(sensor.getId())) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Sensor with ID " + sensor.getId() + " already exists");
            return Response.status(Response.Status.CONFLICT).entity(error).build();
        }

        // Verify roomId exists
        String targetRoomId = sensor.getRoomId();
        if (targetRoomId == null || !rooms.containsKey(targetRoomId)) {
            throw new LinkedResourceNotFoundException("Cannot create sensor: Room ID '" + targetRoomId + "' does not exist in the system.");
        }

        // Link the sensor to the room
        rooms.get(targetRoomId).getSensorIds().add(sensor.getId());
        
        sensors.put(sensor.getId(), sensor);
        return Response.status(Response.Status.CREATED).entity(sensor).build();
    }

    @GET
    public Response getSensors(@QueryParam("type") String type) {
        List<Sensor> result = new ArrayList<>(sensors.values());
        
        if (type != null && !type.trim().isEmpty()) {
            result = result.stream()
                           .filter(s -> type.equalsIgnoreCase(s.getType()))
                           .collect(Collectors.toList());
        }
        
        return Response.ok(result).build();
    }

    @Path("/{sensorId}/readings")
    public SensorReadingResource getSensorReadingResource(@PathParam("sensorId") String sensorId) {
        Sensor sensor = sensors.get(sensorId);
        if (sensor == null) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Sensor not found");
            throw new WebApplicationException(Response.status(Response.Status.NOT_FOUND)
                    .entity(error)
                    .type(MediaType.APPLICATION_JSON)
                    .build());
        }
        return new SensorReadingResource(sensor);
    }
}
