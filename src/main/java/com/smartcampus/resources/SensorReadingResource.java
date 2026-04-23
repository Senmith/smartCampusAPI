/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.smartcampus.resources;

import com.campus.models.DataStore;
import com.campus.models.Sensor;
import com.campus.models.SensorReading;
import com.smartcampus.exceptions.SensorUnavailableException;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.*;
import java.util.ArrayList;
import java.util.List;

public class SensorReadingResource {
    private String sensorId;

    public SensorReadingResource() {
    }

    public SensorReadingResource(String sensorId) {
        this.sensorId = sensorId;
    }

    public void setSensorId(String sensorId) {
        this.sensorId = sensorId;
    }

    @GET // GET /api/v1/sensors/{id}/readings
    @Produces(MediaType.APPLICATION_JSON)
    public Response getHistory() {
        Sensor sensor = DataStore.sensors.get(sensorId);
        if (sensor == null) {
            return Response.status(Response.Status.NOT_FOUND)
                    .type(MediaType.APPLICATION_JSON)
                    .entity("{\"error\":\"Sensor not found.\"}")
                    .build();
        }

        List<SensorReading> history = DataStore.readings.getOrDefault(sensorId, new ArrayList<>());
        return Response.ok(history).build();
    }

    @POST // POST /api/v1/sensors/{id}/readings
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response addReading(SensorReading reading) {
        Sensor sensor = DataStore.sensors.get(sensorId);
        if (sensor == null) {
            return Response.status(Response.Status.NOT_FOUND)
                    .type(MediaType.APPLICATION_JSON)
                    .entity("{\"error\":\"Sensor not found.\"}")
                    .build();
        }
        if ("MAINTENANCE".equalsIgnoreCase(sensor.getStatus())) {
            throw new SensorUnavailableException("Sensor is in MAINTENANCE mode and cannot accept readings.");
        }

        // Save history
        DataStore.readings.computeIfAbsent(sensorId, k -> new ArrayList<>()).add(reading);
        
        // Update parent sensor's currentValue
        sensor.setCurrentValue(reading.getValue());
        
        return Response.status(Response.Status.CREATED).entity(reading).build();
    }
}
