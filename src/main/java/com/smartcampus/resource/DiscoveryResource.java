package com.smartcampus.resource;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import java.util.HashMap;
import java.util.Map;

@Path("/")
public class DiscoveryResource {

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Map<String, Object> getDiscoveryInfo() {
        Map<String, Object> metadata = new HashMap<>();
        metadata.put("version", "1.0");
        metadata.put("name", "Smart Campus Sensor & Room Management API");
        metadata.put("contact", "admin@smartcampus.ac.uk");
        
        Map<String, String> resources = new HashMap<>();
        resources.put("rooms", "/api/v1/rooms");
        resources.put("sensors", "/api/v1/sensors");
        
        metadata.put("resources", resources);
        return metadata;
    }
}
