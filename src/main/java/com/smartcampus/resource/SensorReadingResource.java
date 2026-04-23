package com.smartcampus.resource;

import com.smartcampus.exception.SensorUnavailableException;
import com.smartcampus.model.Sensor;
import com.smartcampus.model.SensorReading;
import com.smartcampus.store.DataStore;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

// Notice: No @Path here because this is instantiated dynamically by the sub-resource locator
public class SensorReadingResource {

    private Sensor parentSensor;
    private Map<String, List<SensorReading>> sensorReadings = DataStore.getInstance().getSensorReadings();

    public SensorReadingResource(Sensor parentSensor) {
        this.parentSensor = parentSensor;
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public List<SensorReading> getHistory() {
        return sensorReadings.getOrDefault(parentSensor.getId(), new ArrayList<>());
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response addReading(SensorReading reading) {
        if ("MAINTENANCE".equalsIgnoreCase(parentSensor.getStatus())) {
            throw new SensorUnavailableException("Sensor " + parentSensor.getId() + " is in MAINTENANCE mode and cannot accept new readings.");
        }

        if (reading == null || reading.getId() == null) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("{\"error\": \"Invalid reading data\"}")
                    .build();
        }

        // Add reading
        List<SensorReading> history = sensorReadings.computeIfAbsent(parentSensor.getId(), k -> new ArrayList<>());
        history.add(reading);

        // Side effect: update parent sensor current value
        parentSensor.setCurrentValue(reading.getValue());

        return Response.status(Response.Status.CREATED).entity(reading).build();
    }
}
