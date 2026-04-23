package com.smartcampus.store;

import com.smartcampus.model.Room;
import com.smartcampus.model.Sensor;
import com.smartcampus.model.SensorReading;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * In-memory datastore for the Smart Campus application.
 * Uses a singleton pattern to ensure data persists across requests.
 */
public class DataStore {
    private static DataStore instance;

    private Map<String, Room> rooms = new HashMap<>();
    private Map<String, Sensor> sensors = new HashMap<>();
    private Map<String, List<SensorReading>> sensorReadings = new HashMap<>();

    private DataStore() {}

    public static synchronized DataStore getInstance() {
        if (instance == null) {
            instance = new DataStore();
        }
        return instance;
    }

    public Map<String, Room> getRooms() { return rooms; }
    public Map<String, Sensor> getSensors() { return sensors; }
    public Map<String, List<SensorReading>> getSensorReadings() { return sensorReadings; }
}
