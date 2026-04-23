package com.smartcampus.resources;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import java.util.LinkedHashMap;
import java.util.Map;

@Path("")
@Produces(MediaType.APPLICATION_JSON)
public class DiscoveryResource {

    @GET
    public Map<String, Object> getDiscovery() {
        Map<String, Object> root = new LinkedHashMap<>();
        root.put("apiName", "Smart Campus Sensor and Room Management API");
        root.put("version", "v1");

        Map<String, String> admin = new LinkedHashMap<>();
        admin.put("name", "Senmith Sahajeewa");
        admin.put("email","senmith.20240756@iit.ac.lk");
        root.put("contact", admin);

        Map<String, String> resources = new LinkedHashMap<>();
        resources.put("discovery", "/api/v1");
        resources.put("rooms", "/api/v1/rooms");
        resources.put("sensors", "/api/v1/sensors");
        resources.put("sensorReadingsTemplate", "/api/v1/sensors/{sensorId}/readings");
        root.put("resources", resources);

        return root;
    }
}
