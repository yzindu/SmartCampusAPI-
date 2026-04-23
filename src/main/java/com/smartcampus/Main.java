package com.smartcampus;

import org.glassfish.grizzly.http.server.HttpServer;
import org.glassfish.jersey.grizzly2.httpserver.GrizzlyHttpServerFactory;
import org.glassfish.jersey.server.ResourceConfig;

import javax.ws.rs.ApplicationPath;
import java.net.URI;
import java.io.IOException;

public class Main {
    public static final String HOST = "http://localhost:8081";

    public static HttpServer startServer() {
        // Instantiate our Application class
        SmartCampusApplication app = new SmartCampusApplication();
        
        // Read the @ApplicationPath annotation to construct the base URI dynamically
        ApplicationPath appPath = app.getClass().getAnnotation(ApplicationPath.class);
        String path = appPath != null ? appPath.value() : "/";
        
        if (!path.startsWith("/")) {
            path = "/" + path;
        }
        if (!path.endsWith("/")) {
            path = path + "/";
        }
        
        String baseUri = HOST + path;
        
        // ResourceConfig handles the deployment of the application classes
        final ResourceConfig rc = ResourceConfig.forApplication(app);
        
        // Start and return the Grizzly server
        return GrizzlyHttpServerFactory.createHttpServer(URI.create(baseUri), rc);
    }

    public static void main(String[] args) throws IOException {
        final HttpServer server = startServer();
        System.out.println("Jersey app started.");
        System.out.println("Discovery Endpoint: " + HOST + "/api/v1");
        System.out.println("Hit Ctrl-C to stop it...");
        System.in.read();
        server.stop();
    }
}
