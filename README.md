# Smart Campus Sensor and Room Management API

## Overview
This project implements a JAX-RS RESTful API for a Smart Campus system that manages:
- Rooms
- Sensors linked to rooms
- Historical sensor readings (nested under sensors)

The API is versioned under `/api/v1` and uses in-memory data structures (`ConcurrentHashMap` and `ArrayList`)

## Technology Stack
- Java 17
- Maven
- Jakarta REST (JAX-RS)
- Jersey (Servlet container integration)

## Build and Run
1. Install Java 17 and Maven.
2. Build the WAR:
```bash
mvn clean package
```
3. Deploy `target/smartCampusAPI-1.0-SNAPSHOT.war` to a Jakarta-compatible servlet container (for example Tomcat 10+).
4. Access API base path:
```text
http://localhost:8080/smartCampusAPI/api/v1
```

## Sample curl Commands
1. Discovery endpoint:
```bash
curl -X GET "http://localhost:8080/smartCampusAPI/api/v1"
```

2. Create room:
```bash
curl -X POST "http://localhost:8080/smartCampusAPI/api/v1/rooms" \
  -H "Content-Type: application/json" \
  -d '{"id":"ENG-101","name":"Engineering Lab","capacity":40,"sensorIds":[]}'
```

3. Get all rooms:
```bash
curl -X GET "http://localhost:8080/smartCampusAPI/api/v1/rooms"
```

4. Register sensor (linked to existing room):
```bash
curl -X POST "http://localhost:8080/smartCampusAPI/api/v1/sensors" \
  -H "Content-Type: application/json" \
  -d '{"id":"CO2-001","type":"CO2","status":"ACTIVE","currentValue":0.0,"roomId":"ENG-101"}'
```

5. Filter sensors by type:
```bash
curl -X GET "http://localhost:8080/smartCampusAPI/api/v1/sensors?type=CO2"
```

6. Add sensor reading:
```bash
curl -X POST "http://localhost:8080/smartCampusAPI/api/v1/sensors/CO2-001/readings" \
  -H "Content-Type: application/json" \
  -d '{"id":"r-001","timestamp":1713500000000,"value":415.2}'
```

7. Get sensor reading history:
```bash
curl -X GET "http://localhost:8080/smartCampusAPI/api/v1/sensors/CO2-001/readings"
```

## API Design Summary
- `GET /api/v1` discovery metadata and entry links.
- `GET /rooms`, `POST /rooms`, `GET /rooms/{roomId}`, `DELETE /rooms/{roomId}`.
- `POST /sensors`, `GET /sensors`, optional `type` filter with `@QueryParam`.
- Sub-resource locator: `/sensors/{sensorId}/readings` delegated to `SensorReadingResource`.

## Error Handling and Logging
Custom exception handling is implemented using JAX-RS `ExceptionMapper`:
- `RoomNotEmptyException` -> `409 Conflict`
- `LinkedResourceNotFoundException` -> `422 Unprocessable Entity`
- `SensorUnavailableException` -> `403 Forbidden`
- Global fallback (`Throwable`) -> `500 Internal Server Error` with safe JSON message

A logging filter implements both `ContainerRequestFilter` and `ContainerResponseFilter` to log:
- Incoming request method and URI
- Outgoing response status code
