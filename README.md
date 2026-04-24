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

Questions & Answers

Question 1 -----------------------------------------------------------------------------------
In your report, explain the default lifecycle of a JAX-RS Resource class. Is a new instance 
instantiated for every incoming request, or does the runtime treat it as a singleton? Elaborate on 
how this architectural decision impacts the way you manage and synchronize your in-memory 
data structures (maps/lists) to prevent data loss or race conditions. 

Answer - JAX-RS by default uses per-request lifecycles, where a new resource object is created 
for each HTTP request and destroyed after the processing of the request. Because of the lifecycle 
time of objects is very short, I used static variables in the DataStore to avoid any data loss. I also 
used ConcurrentHashMap in place of a normal HashMap to avoid race conditions as the Tomcat 
server executes multiple requests on separate threads. 

Question 2 -----------------------------------------------------------------------------------
Why is the provision of ”Hypermedia” (links and navigation within responses) considered a 
hallmark of advanced RESTful design (HATEOAS)? How does this approach benefit client 
developers compared to static documentation? 

Answer - Using HATEOAS is the optimal approach to creating RESTful APIs since it creates an 
automated navigation through the hypermedia links provided by the Server to the Client detailing 
all the actions that can be performed. This represents a shift away from Client developers having 
to hardcode fragile URIs or perform complex state-transition logic manually, such as evaluating 
whether an order may be cancelled based on the current status of that order. 
Instead, the presence or absence of a link signifies the next state of an action that can be 
performed. Therefore, HATEOAS reduces reliance on static documentation which will quickly 
become stale. The approach allows for adaptation on the Client based on changes made on the 
Server side or updated workflows without requiring code changes.  

Question 3 ------------------------------------------------------------------------------------
When returning a list of rooms, what are the implications of returning only IDs versus returning 
the full room objects? Consider network bandwidth and client side processing. 

Answer – Bandwidth and time is reduced when a server returns IDs instead of entire objects. 
However, placing those IDs on the client adds complexity (N+1 problem) and latency  since for 
multiple follow up requests are required to build the complete object. 
By returning entire objects, round trips can be minimized and rendering of objects are simplified 
on the client but run the risk of over-fetching and producing large payloads that will be slow to 
transmit on slower networks. Most optimal designs are determined by balancing two or more 
values by returning summary objects containing only the key fields needed to create a balance 
between speed and useful data. 

Question 4 -------------------------------------------------------------------------------------
Is the DELETE operation idempotent in your implementation?  Provide a detailed justification 
by describing what happens if a client mistakenly sends the exactsame DELETE request for a 
room multiple times 

Answer - Yes, the delete operation is idempotent in this implementation. When a client sends the 
first request, the server locates the room by its ID, removes it from the database, and typically 
returns a 204 No Content or 200 OK status. If the client sends the exact same request again 
mistakenly, the server returns a 404 Not Found or continues to return a success code since that 
state has already been achieved. Because on the server side, for example the room remains 
deleted regardless of how many times the request is repeated, therefore the operation satisfies the 
definition of idempotency. 

Question 5 --------------------------------------------------------------------------------------
We explicitly use the @Consumes (MediaType.APPLICATION_JSON) annotation on the POST 
method. Explain the technical consequences if a client attempts to send data in a different format, 
such as text/plain or application/xml. How does JAX-RS handle this mismatch? 

Answer - When a client sends data in an unsupported format, such as text/plain or 
application/xml, the JAX-RS runtime sees a mismatch between the request's Content-Type 
header and the @Consumes annotation. The server rejects the request during the provider 
selection phase, before it reaches your method logic. JAX-RS automatically handles this by 
stopping the execution and returning an HTTP 415 Unsupported Media Type error to the client. 
This ensures type safety and stops the application from trying to parse incompatible data 
structures, keeping a clear agreement between the client and the API. 

Question 6 --------------------------------------------------------------------------------------
You implemented this filtering using @QueryParam. Contrast this with an alternative design 
where the type is part of the URL path (e.g., /api/vl/sensors/type/CO2). Why is the query 
parameter approach generally considered superior for filtering and searching collections? 

Answer – Using query parameters are better because it enables us to maintain a clean, 
hierarchical resource structure. In this structure the identity of the collection is represented by the 
URL and the modifiers are represented by the parameters. Using path variables for every filter 
makes the API difficult to scale as more criteria are being added. @QueryParam enables flexible, 
optional and combinable search filters without altering the resource path. 

Question 7 ---------------------------------------------------------------------------------------
Discuss the architectural benefits of the Sub-Resource Locator pattern. How does delegating 
logic to separate classes help manage complexity in large APIs compared to defining every 
nested path (e.g., sensors/{id}/readings/{rid}) in one massive con troller class?  

Answer – Separation of concerns and modular maintainability is promoted by the Sub Resource 
Locator pattern enhancing the API architecture. By assigning nested logic to specific sub
resource classes, you avoid the "God Controller" problem. This issue arises when a single class 
turns into an overwhelming block of code that manages different business rules. Delegating tasks 
lets each class concentrate on its particular area, like Readings. This makes unit testing easier and 
makes the code clearer. It also allows for resource reuse since a sub-resource class can connect to 
several parent paths without repeating logic. Instead of hardcoding complex URI patterns, the 
parent controller serves as a router. This results in cleaner, more scalable code that is simpler for 
teams to work with and update. 

Question 8 ----------------------------------------------------------------------------------------
Why is HTTP 422 often  considered more semantically accurate than a standard 404 when the 
issue is a missing reference inside a valid JSON payload? 

Answer - TTP 422 Unprocessable Entity is more accurate than 404 Not Found because it 
differentiates between a missing endpoint and a missing referenced entity. A 404 error usually 
means that the URL does not exist, which can lead a developer to believe they have the wrong 
address. On the other hand, a 422 error shows that the server understood the request and the 
JSON syntax is correct. However, the business logic failed because an ID in the payload, like a 
roomID, points to a record that does not exist. This gives a clear indication: the communication 
is working, but the data is logically invalid. 

Question 9 -----------------------------------------------------------------------------------------
From a cybersecurity standpoint, explain the risks associated with exposing internal Java stack 
traces to external API consumers. What specific information could an attacker gather from such a 
trace? 

Answer - Exposing Java stack traces comes with a significant risk of information leaks. It offers 
attackers a detailed view of your server's internal setup. A stack trace can reveal specific library 
versions, such as Spring or Hibernate. This allows hackers to focus on known vulnerabilities. It 
also shows internal file paths, class names, and database schema details. This information turns 
your application from a "black box" into a clear target. It helps attackers outline the attack 
surface, find weak spots in input validation, and create targeted exploits like SQL injection or 
remote code execution. 

Question 10 ----------------------------------------------------------------------------------------
Why is it advantageous to use JAX-RS filters for cross-cutting concerns like logging, rather than 
manually inserting Logger.info() statements inside every single re source method? 

Answer - Using JAX-RS filters for cross-cutting issues like logging is helpful because it follows 
the Don't Repeat Yourself (DRY) principle. This approach centralizes logic that could otherwise 
be spread out across many resource methods. Manually adding logger statements to every 
method causes extra code and makes the API harder to maintain. If you need to change the 
logging format, you'd have to update each endpoint individually. By using a filter, you create one 
point that handles requests and responses globally. This ensures consistency, as no developer can 
forget to log an endpoint. It also results in a cleaner codebase, allowing resource methods to 
focus on their business logic instead of infrastructure issues.