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
5. The API will be available at `http://localhost:8080/api/v1`.

## Sample cURL Commands

Here are 5 sample `curl` commands to test the API:

1. **Discovery Endpoint**
   ```bash
   curl -X GET http://localhost:8080/api/v1
   ```

2. **Create a Room**
   ```bash
   curl -X POST http://localhost:8080/api/v1/rooms \
   -H "Content-Type: application/json" \
   -d '{"id": "LIB-301", "name": "Library Quiet Study", "capacity": 50}'
   ```

3. **Register a Sensor**
   ```bash
   curl -X POST http://localhost:8080/api/v1/sensors \
   -H "Content-Type: application/json" \
   -d '{"id": "CO2-001", "type": "CO2", "status": "ACTIVE", "roomId": "LIB-301"}'
   ```

4. **Add a Sensor Reading (Sub-Resource)**
   ```bash
   curl -X POST http://localhost:8080/api/v1/sensors/CO2-001/readings \
   -H "Content-Type: application/json" \
   -d '{"id": "READING-1", "timestamp": 1700000000000, "value": 412.5}'
   ```

5. **Filter Sensors by Type**
   ```bash
   curl -X GET "http://localhost:8080/api/v1/sensors?type=CO2"
   ```

---

## Conceptual Report Answers

### Part 1: Service Architecture & Setup
**Q: Explain the default lifecycle of a JAX-RS Resource class. Is a new instance instantiated for every incoming request, or does the runtime treat it as a singleton? Elaborate on how this architectural decision impacts the way you manage and synchronize your in-memory data structures.**

By default, JAX-RS resource classes are request-scoped, i.e., each incoming HTTP request is associated with a new instance. This architectural design will make sure that requests will not share state within the resource class itself, they are isolated. But since our in-memory data structures (Maps/Lists) are required to be remembered over many requests, we cannot keep them as simple instance variables within the resource. Rather, we have to employ a Singleton pattern (such as our DataStore class) or use of static variables. Moreover, due to the possibility of having several request threads accessing the Singleton at the same time, we need to make sure that these data structures are thread-safe to avoid race conditions and corruption of data.

**Q: Why is the provision of "Hypermedia" (links and navigation within responses) considered a hallmark of advanced RESTful design (HATEOAS)? How does this approach benefit client developers compared to static documentation?**

Hypermedia as the Engine of Application State (HATEOAS) is a fundamental requirement of sophisticated RESTful designs. The API responses can be dynamically configured to include hypermedia links which direct the client on what they can do next, depending on the state of the resource. This is beneficial to client developers as they do not have to hard-code API URLs or do not have to use fully-static documentation. When the URL structure of the server alters, then the client will automatically adjust itself provided that it uses the given links and this makes the system very decoupled.

### Part 2: Room Management
**Q: When returning a list of rooms, what are the implications of returning only IDs versus returning the full room objects? Consider network bandwidth and client side processing.**

Sending back only IDs saves a lot of network bandwidth since the size of the JSON payload is significantly smaller. But this has a negative effect on client-side processing, since the client will probably have to do subsequent individual GET requests of each ID to retrieve the complete information (the N+1 query problem). On the other hand, sending full room objects consumes more bandwidth, but enables the client to render the data immediately without subsequent network requests, enhancing perceived client performance.

**Q: Is the DELETE operation idempotent in your implementation? Provide a detailed justification by describing what happens if a client mistakenly sends the exact same DELETE request for a room multiple times.**

Yes, the DELETE operation is idempotent. Idempotency implies that repeated identical requests with the same effect on the server state as one request. In case a client sends the same DELETE request on the same room more than once, the initial request will delete the room and a 204 No Content will be returned. Further DELETEs of the same room ID will give a 404 Not Found due to the nonexisting room. Once the initial request has been received the state of the server does not vary any more, strictly following the idempotency constraint.

### Part 3: Sensor Operations & Linking
**Q: We explicitly use the @Consumes(MediaType.APPLICATION_JSON) annotation on the POST method. Explain the technical consequences if a client attempts to send data in a different format, such as text/plain or application/xml. How does JAX-RS handle this mismatch?**

 The @Consumes(MediaType.Application_JSON) annotation serves as a binding contract, informing JAX-RS runtime that the method can only accept incoming requests with a Content-Type of application/json. When a client tries to send a data in text/plain or application/xml, JAX-RS will automatically intercept request and send it to the resource method. It manages the mismatch immediately sending an HTTP 415 Unsupported Media Type response to the client so that the application logic is not exposed to invalid payloads.

**Q: You implemented this filtering using @QueryParam. Contrast this with an alternative design where the type is part of the URL path (e.g., /api/vl/sensors/type/CO2). Why is the query parameter approach generally considered superior for filtering and searching collections?**

Path parameters are structured to refer to a particular resource or have a strict hierarchy, but query parameters are structured to alter, filter, or sort a larger collection. The best approach to use as a filter is to use @QueryParam as it is very flexible and optional. It is easy to add and add filters (e.g. ?type=CO2 and status=ACTIVE) without having very deep and confusing URLs. It is semantically consistent with the standards of HTTP: the collection of sensors is the resource, and query string is simply a filtering applied to the collection.

### Part 4: Deep Nesting with Sub-Resources
**Q: Discuss the architectural benefits of the Sub-Resource Locator pattern. How does delegating logic to separate classes help manage complexity in large APIs compared to defining every nested path in one massive controller class?**

The Sub-Resource Locator design encourages modularity and separation of concerns. Defining all of the nested paths in one giant controller class in large APIs results in god classes that are hard to read, maintain and test. In assigning logic to classes separate, we put the operations of readings in their domain context. It also supports dynamic instantiating the sub-resource, depending on the locator being able to pass contextual information (such as the parent Sensor object) directly into the sub-resource class constructor, so the nested logic can be significantly cleaner and context-sensitive.

### Part 5: Advanced Error Handling, Exception Mapping & Logging
**Q: Why is HTTP 422 often considered more semantically accurate than a standard 404 when the issue is a missing reference inside a valid JSON payload?**

The implication of HTTP 404 Not Found is that the endpoint of the routing engine was unable to identify the target URI. In case the URI is correct and the syntax of the JSON is correct, but a foreign key within the payload (such as a missing roomid) is not, then HTTP 422 Unprocessable Entity is a far better choice. It clearly informs the client that the server knows the content type of the request entity, and the syntax is correct, but it failed to execute the instructions contained in it because of semantic validation errors.

**Q: From a cybersecurity standpoint, explain the risks associated with exposing internal Java stack traces to external API consumers. What specific information could an attacker gather from such a trace?**

Internal Java stack trace disclosure is a major security vulnerability called Information Disclosure. A stack trace can provide an attacker with vital intelligence such as library exact versions, frameworks, internal file paths and database driver information. It can enable an attacker to single out the backend architecture and look into the known CVEs (Common Vulnerabilities and Exposures) of those particular dependencies, significantly reducing the effort needed to make a successful exploit.


**Q: Why is it advantageous to use JAX-RS filters for cross-cutting concerns like logging, rather than manually inserting Logger.info() statements inside every single resource method?**

Aspect-Oriented Programming (AOP) paradigm, which separates cross-cutting issues with business logic, is achieved by using JAX-RS filters. The moment you start to add Logger.info() manually within each and every method, you are faced with the massive code duplication, cluttering of the resource classes, and it would be very easy to forget to log a certain endpoint. The filters capture requests on a global basis at the framework level to ensure each and every incoming request and outgoing response is uniformly logged without polluting the business logic code.
