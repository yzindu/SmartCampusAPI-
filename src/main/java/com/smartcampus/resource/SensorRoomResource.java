package com.smartcampus.resource;

import com.smartcampus.exception.RoomNotEmptyException;
import com.smartcampus.model.Room;
import com.smartcampus.store.DataStore;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Path("/rooms")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class SensorRoomResource {

    private Map<String, Room> rooms = DataStore.getInstance().getRooms();

    @GET
    public List<Room> getAllRooms() {
        return new ArrayList<>(rooms.values());
    }

    @POST
    public Response createRoom(Room room) {
        if (room == null || room.getId() == null || room.getId().trim().isEmpty()) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Room must have a valid ID");
            return Response.status(Response.Status.BAD_REQUEST).entity(error).build();
        }
        
        if (rooms.containsKey(room.getId())) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Room with ID " + room.getId() + " already exists");
            return Response.status(Response.Status.CONFLICT).entity(error).build();
        }

        rooms.put(room.getId(), room);
        return Response.status(Response.Status.CREATED).entity(room).build();
    }

    @GET
    @Path("/{roomId}")
    public Response getRoom(@PathParam("roomId") String roomId) {
        Room room = rooms.get(roomId);
        if (room == null) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Room not found");
            return Response.status(Response.Status.NOT_FOUND).entity(error).build();
        }
        return Response.ok(room).build();
    }

    @DELETE
    @Path("/{roomId}")
    public Response deleteRoom(@PathParam("roomId") String roomId) {
        Room room = rooms.get(roomId);
        if (room == null) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Room not found");
            return Response.status(Response.Status.NOT_FOUND).entity(error).build();
        }

        if (!room.getSensorIds().isEmpty()) {
            throw new RoomNotEmptyException("Room " + roomId + " cannot be deleted because it contains active sensors.");
        }

        rooms.remove(roomId);
        return Response.noContent().build();
    }
}
