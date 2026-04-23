package com.smartcampus;

import javax.ws.rs.ApplicationPath;
import javax.ws.rs.core.Application;
import java.util.Set;
import java.util.HashSet;
import com.smartcampus.resource.DiscoveryResource;

@ApplicationPath("/api/v1")
public class SmartCampusApplication extends Application {
    @Override
    public Set<Class<?>> getClasses() {
        Set<Class<?>> classes = new HashSet<>();
        // Register REST resources
        classes.add(DiscoveryResource.class);
        classes.add(com.smartcampus.resource.SensorRoomResource.class);
        classes.add(com.smartcampus.resource.SensorResource.class);
        
        // Register Exception Mappers
        classes.add(com.smartcampus.exception.RoomNotEmptyExceptionMapper.class);
        classes.add(com.smartcampus.exception.LinkedResourceNotFoundExceptionMapper.class);
        classes.add(com.smartcampus.exception.SensorUnavailableExceptionMapper.class);
        classes.add(com.smartcampus.exception.GenericExceptionMapper.class);
        
        // Register Filters
        classes.add(com.smartcampus.filter.LoggingFilter.class);
        
        return classes;
    }
}
