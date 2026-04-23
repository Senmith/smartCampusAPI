/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.smartcampus.resources;

import com.campus.models.DataStore;
import com.campus.models.Sensor;
import com.smartcampus.exceptions.LinkedResourceNotFoundException;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.*;
import java.util.List;
import java.util.stream.Collectors;
import java.util.ArrayList;

@Path("/sensors")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class SensorResource {

    @POST // POST /api/v1/sensors
    public Response addSensor(Sensor sensor) {
        // Integrity check: Verify roomId exists
        if (!DataStore.rooms.containsKey(sensor.getRoomId())) {
            throw new LinkedResourceNotFoundException("Room ID does not exist.");
        }
        DataStore.sensors.put(sensor.getId(), sensor);
        // Link sensor to the room
        DataStore.rooms.get(sensor.getRoomId()).getSensorIds().add(sensor.getId());
        return Response.status(Response.Status.CREATED).entity(sensor).build();
    }

    @GET // GET /api/v1/sensors with filtering
    public List<Sensor> getSensors(@QueryParam("type") String type) {
        if (type == null) return new ArrayList<>(DataStore.sensors.values());
        return DataStore.sensors.values().stream()
                .filter(s -> s.getType().equalsIgnoreCase(type))
                .collect(Collectors.toList());
    }

    @GET
    @Path("/{sensorId}")
    public Response getSensor(@PathParam("sensorId") String sensorId) {
        Sensor sensor = DataStore.sensors.get(sensorId);
        if (sensor == null) {
            return Response.status(Response.Status.NOT_FOUND)
                    .type(MediaType.APPLICATION_JSON)
                    .entity("{\"error\":\"Sensor not found.\"}")
                    .build();
        }
        return Response.ok(sensor).build();
    }

    @PUT
    @Path("/{sensorId}")
    public Response updateSensor(@PathParam("sensorId") String sensorId, Sensor updatedSensor) {
        Sensor existingSensor = DataStore.sensors.get(sensorId);
        if (existingSensor == null) {
            return Response.status(Response.Status.NOT_FOUND)
                    .type(MediaType.APPLICATION_JSON)
                    .entity("{\"error\":\"Sensor not found.\"}")
                    .build();
        }

        if (updatedSensor == null) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("{\"error\":\"Request body is required.\"}")
                    .build();
        }

        String targetRoomId = updatedSensor.getRoomId() != null
            ? updatedSensor.getRoomId()
            : existingSensor.getRoomId();
        if (targetRoomId != null && !DataStore.rooms.containsKey(targetRoomId)) {
            throw new LinkedResourceNotFoundException("Room ID does not exist.");
        }

        // Keep room-to-sensor links consistent when room assignment changes.
        String oldRoomId = existingSensor.getRoomId();
        if (oldRoomId != null && targetRoomId != null && !oldRoomId.equals(targetRoomId)
                && DataStore.rooms.containsKey(oldRoomId)) {
            DataStore.rooms.get(oldRoomId).getSensorIds().remove(existingSensor.getId());
            DataStore.rooms.get(targetRoomId).getSensorIds().add(existingSensor.getId());
        }

        if (updatedSensor.getType() != null) {
            existingSensor.setType(updatedSensor.getType());
        }
        if (updatedSensor.getStatus() != null) {
            existingSensor.setStatus(updatedSensor.getStatus());
        }
        existingSensor.setRoomId(targetRoomId);

        // Keep the path parameter as the source of truth for resource identity.
        existingSensor.setId(sensorId);

        // Explicitly persist the merged sensor state.
        DataStore.sensors.put(sensorId, existingSensor);

        return Response.ok(existingSensor).build();
    }

    // Sub-Resource Locator for Readings
    @Path("/{sensorId}/readings")
    public SensorReadingResource getReadingResource(@PathParam("sensorId") String sensorId) {
        SensorReadingResource resource = new SensorReadingResource();
        resource.setSensorId(sensorId);
        return resource;
    }
}
