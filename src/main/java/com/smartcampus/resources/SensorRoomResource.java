/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.smartcampus.resources;

import com.campus.models.DataStore;
import com.campus.models.Room;
import com.smartcampus.exceptions.RoomNotEmptyException;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.*;
import java.util.ArrayList;
import java.util.List;

@Path("/rooms")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class SensorRoomResource {

    @GET // GET /api/v1/rooms
    public List<Room> getAllRooms() {
        return new ArrayList<>(DataStore.rooms.values());
    }

    @POST // POST /api/v1/rooms
    public Response createRoom(Room room) {
        DataStore.rooms.put(room.getId(), room);
        return Response.status(Response.Status.CREATED).entity(room).build();
    }

    @GET
    @Path("/{roomId}") // GET /api/v1/rooms/{roomId}
    public Response getRoom(@PathParam("roomId") String roomId) {
        Room room = DataStore.rooms.get(roomId);
        if (room == null) return Response.status(Response.Status.NOT_FOUND).build();
        return Response.ok(room).build();
    }

    @DELETE
    @Path("/{roomId}") // DELETE /api/v1/rooms/{roomId}
    public Response deleteRoom(@PathParam("roomId") String roomId) {
        Room room = DataStore.rooms.get(roomId);
        // Business Logic Constraint: Block if sensors are present
        if (room != null && !room.getSensorIds().isEmpty()) {
            throw new RoomNotEmptyException("Cannot delete room with active sensors.");
        }
        DataStore.rooms.remove(roomId);
        return Response.noContent().build();
    }
}
