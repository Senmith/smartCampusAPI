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

    // Sub-Resource Locator for Readings
    @Path("/{sensorId}/readings")
    public SensorReadingResource getReadingResource(@PathParam("sensorId") String sensorId) {
        return new SensorReadingResource(sensorId);
    }
}
