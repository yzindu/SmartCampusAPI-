# Smart Campus Sensor & Room Management API

A robust, scalable, and highly available RESTful API to manage rooms and sensors for the university's "Smart Campus" initiative. Built purely with JAX-RS (Jersey) and Grizzly HTTP Server.

## How to Build & Run

### Prerequisites
- Java 17 or higher
- Maven 3.6+

### Instructions
1. **Clone the repository** (or extract the folder).
2. **Navigate to the project directory**:
   ```bash
   cd SmartCampusAPI
   ```
3. **Build the project using Maven**:
   ```bash
   mvn clean compile
   ```
4. **Run the server**:
   ```bash
   mvn exec:java
   ```
   *Alternatively, you can open the project in an IDE like IntelliJ IDEA or Eclipse and run the `Main.java` file directly.*
5. The API will be available at `http://localhost:8081/api/v1`.

## Sample cURL Commands

Here are 5 sample `curl` commands to test the API:

1. **Discovery Endpoint**
   ```bash
   curl -X GET http://localhost:8081/api/v1
   ```

2. **Create a Room**
   ```bash
   curl -X POST http://localhost:8081/api/v1/rooms \
   -H "Content-Type: application/json" \
   -d '{"id": "LIB-301", "name": "Library Quiet Study", "capacity": 50}'
   ```

3. **Register a Sensor**
   ```bash
   curl -X POST http://localhost:8081/api/v1/sensors \
   -H "Content-Type: application/json" \
   -d '{"id": "CO2-001", "type": "CO2", "status": "ACTIVE", "roomId": "LIB-301"}'
   ```

4. **Add a Sensor Reading (Sub-Resource)**
   ```bash
   curl -X POST http://localhost:8081/api/v1/sensors/CO2-001/readings \
   -H "Content-Type: application/json" \
   -d '{"id": "READING-1", "timestamp": 1700000000000, "value": 412.5}'
   ```

5. **Filter Sensors by Type**
   ```bash
   curl -X GET "http://localhost:8081/api/v1/sensors?type=CO2"
   ```

---

## Conceptual Report Answers

### Part 1: Service Architecture & Setup
**Q: Explain the default lifecycle of a JAX-RS Resource class. Is a new instance instantiated for every incoming request, or does the runtime treat it as a singleton? Elaborate on how this architectural decision impacts the way you manage and synchronize your in-memory data structures.**
By default, JAX-RS resource classes are request-scoped, meaning a new instance is instantiated for every incoming HTTP request. This architectural decision ensures that requests do not share state inside the resource class itself, isolating them. However, because our in-memory data structures (Maps/Lists) must persist across multiple requests, we cannot store them as simple instance variables inside the resource. Instead, we must use a Singleton pattern (like our `DataStore` class) or static variables. Furthermore, because multiple request threads might access the Singleton concurrently, we must ensure these data structures are thread-safe to prevent race conditions and data corruption.

**Q: Why is the provision of "Hypermedia" (links and navigation within responses) considered a hallmark of advanced RESTful design (HATEOAS)? How does this approach benefit client developers compared to static documentation?**
Hypermedia as the Engine of Application State (HATEOAS) is a core constraint of advanced RESTful architectures. By including hypermedia links in API responses, the server dynamically guides the client on what actions are possible next, based on the resource's current state. This benefits client developers because they don't need to hardcode API URLs or rely entirely on static documentation. If the server's URL structure changes, the client will automatically adapt as long as it follows the provided links, making the system highly decoupled.

### Part 2: Room Management
**Q: When returning a list of rooms, what are the implications of returning only IDs versus returning the full room objects? Consider network bandwidth and client side processing.**
Returning only IDs significantly reduces network bandwidth consumption because the JSON payload is much smaller. However, it negatively impacts client-side processing, as the client will likely need to make subsequent individual GET requests for each ID to fetch the full details (the N+1 query problem). Conversely, returning full room objects increases bandwidth usage but allows the client to immediately render the data without making further network requests, improving perceived client performance.

**Q: Is the DELETE operation idempotent in your implementation? Provide a detailed justification by describing what happens if a client mistakenly sends the exact same DELETE request for a room multiple times.**
Yes, the DELETE operation is idempotent. Idempotency means that making multiple identical requests has the same effect on the server state as making a single request. If a client mistakenly sends the same DELETE request for a room multiple times, the first request successfully deletes the room and returns a `204 No Content`. Subsequent DELETE requests for that same room ID will result in a `404 Not Found` because the room no longer exists. The server's state does not change any further after the first request, strictly adhering to the idempotency constraint.

### Part 3: Sensor Operations & Linking
**Q: We explicitly use the @Consumes(MediaType.APPLICATION_JSON) annotation on the POST method. Explain the technical consequences if a client attempts to send data in a different format, such as text/plain or application/xml. How does JAX-RS handle this mismatch?**
The `@Consumes(MediaType.APPLICATION_JSON)` annotation acts as a strict contract, telling the JAX-RS runtime that the method can only process incoming requests with a `Content-Type` of `application/json`. If a client attempts to send data in `text/plain` or `application/xml`, JAX-RS will automatically intercept the request before it reaches the resource method. It handles the mismatch by instantly returning an `HTTP 415 Unsupported Media Type` error response to the client, ensuring the application logic is protected from invalid payloads.

**Q: You implemented this filtering using @QueryParam. Contrast this with an alternative design where the type is part of the URL path (e.g., /api/vl/sensors/type/CO2). Why is the query parameter approach generally considered superior for filtering and searching collections?**
Path parameters are designed to identify specific resources or establish a strict hierarchy, whereas query parameters are designed to modify, filter, or sort a broader collection. Using `@QueryParam` is superior for filtering because it is highly flexible and optional. You can easily stack multiple filters (e.g., `?type=CO2&status=ACTIVE`) without creating deeply nested or confusing URL structures. It semantically aligns with HTTP standards: the resource is the "collection of sensors", and the query string is merely a filter applied to that collection.

### Part 4: Deep Nesting with Sub-Resources
**Q: Discuss the architectural benefits of the Sub-Resource Locator pattern. How does delegating logic to separate classes help manage complexity in large APIs compared to defining every nested path in one massive controller class?**
The Sub-Resource Locator pattern promotes modularity and separation of concerns. In large APIs, defining every nested path in a single massive controller class leads to "God classes" that are difficult to read, maintain, and test. By delegating logic to separate classes, we encapsulate the operations specific to readings within their own domain context. It also allows dynamic instantiation of the sub-resource, where the locator can pass contextual data (like the parent `Sensor` object) directly into the constructor of the sub-resource, making the nested logic much cleaner and context-aware.

### Part 5: Advanced Error Handling, Exception Mapping & Logging
**Q: Why is HTTP 422 often considered more semantically accurate than a standard 404 when the issue is a missing reference inside a valid JSON payload?**
`HTTP 404 Not Found` implies that the target URI endpoint itself could not be found by the routing engine. When the URI is correct and the JSON syntax is valid, but a foreign key inside the payload (like a missing `roomId`) is invalid, `HTTP 422 Unprocessable Entity` is much more semantically accurate. It explicitly tells the client that the server understands the content type of the request entity, and the syntax is correct, but it was unable to process the contained instructions due to semantic validation errors.

**Q: From a cybersecurity standpoint, explain the risks associated with exposing internal Java stack traces to external API consumers. What specific information could an attacker gather from such a trace?**
Exposing internal Java stack traces is a significant security vulnerability known as "Information Disclosure." An attacker can gather critical intel from a stack trace, including exact library versions, framework names, internal file paths, and database driver details. This information allows an attacker to map the backend architecture and search for known CVEs (Common Vulnerabilities and Exposures) associated with those specific dependencies, dramatically lowering the effort required to launch a successful exploit.

**Q: Why is it advantageous to use JAX-RS filters for cross-cutting concerns like logging, rather than manually inserting Logger.info() statements inside every single resource method?**
Using JAX-RS filters implements the Aspect-Oriented Programming (AOP) paradigm, separating cross-cutting concerns from core business logic. Manually inserting `Logger.info()` inside every method leads to massive code duplication, clutters the resource classes, and makes it incredibly easy to accidentally forget to log a specific endpoint. Filters intercept requests globally at the framework level, guaranteeing that every single incoming request and outgoing response is uniformly logged without contaminating the business logic code.
