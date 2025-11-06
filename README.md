# Vaadin-Micronaut Demo

This project demonstrates an **experimental integration** of Vaadin Flow 24.9.4 with Micronaut Framework 4.10.1, using Jetty 12 as the embedded servlet container.

> **Note:** This is **not an official solution** from Vaadin or Micronaut. This integration was created with heavy support of AI and represents an experimental approach to combining these frameworks. Use at your own risk in production environments.

## Overview

Vaadin typically requires Spring Boot or Jakarta EE for automatic servlet initialization and route discovery. This project implements a custom integration to make Vaadin work with Micronaut's lightweight dependency injection framework.

## Key Integration Changes

This project includes custom components to bridge Vaadin and Micronaut:

### 1. VaadinServletConfiguration
**Location**: `src/main/java/com/example/VaadinServletConfiguration.java`

Implements `BeanCreatedEventListener<Server>` to intercept Jetty server creation and manually configure:
- VaadinServlet registration
- ServletContext ClassLoader setup
- Vaadin's LookupServletContainerInitializer initialization
- SessionHandler configuration (required for HTTP session management)
- Classpath scanning configuration

### 2. ManualRouteRegistrar
**Location**: `src/main/java/com/example/ManualRouteRegistrar.java`

Implements `VaadinServiceInitListener` to manually register Vaadin routes, since automatic `@Route` annotation discovery doesn't work when the servlet is registered after container startup.

### 3. ServiceLoader Registration
**Location**: `src/main/resources/META-INF/services/com.vaadin.flow.server.VaadinServiceInitListener`

Contains:
```
com.example.ManualRouteRegistrar
```

This enables Vaadin to discover and load our custom route registrar via Java's ServiceLoader mechanism.

### 4. Maven Dependencies
Added `micronaut-servlet-engine` dependency to enable servlet support in Micronaut.

## Prerequisites

- Java 21 or later
- Maven 3.8 or later

## Building and Running

To build and run the application:

```bash
mvn clean mn:run
```

For subsequent runs during development:
```bash
mvn mn:run
```

The application will start on `http://localhost:8080`

## Adding New Views

To add a new Vaadin view:

1. Create your view class extending a Vaadin layout component (e.g., `VerticalLayout`)
2. Add the `@Route` annotation (for documentation purposes)
3. Register the route in `ManualRouteRegistrar.java`

**Example**:

```java
// Create your view
package com.example;

import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.Route;

@Route("hello")
public class HelloView extends VerticalLayout {
    public HelloView() {
        add("Hello from HelloView!");
    }
}
```

```java
// Register it in ManualRouteRegistrar.java
@Override
public void serviceInit(ServiceInitEvent event) {
    RouteConfiguration routeConfiguration = RouteConfiguration.forApplicationScope();

    routeConfiguration.setRoute("", MyView.class);
    routeConfiguration.setRoute("hello", HelloView.class); // Add this line
}
```

## Technical Details

### Why Manual Integration is Required

1. **ServletContainerInitializers**: Vaadin relies on `ServletContainerInitializer` hooks that run during standard servlet container startup. Micronaut's servlet support doesn't automatically trigger these initializers.

2. **Route Discovery**: Vaadin normally scans for `@Route` annotations during startup. Since we register the servlet after startup via `BeanCreatedEventListener`, this scanning phase has already passed.

3. **Dependency Injection**: Vaadin's `LookupServletContainerInitializer` must be manually initialized with the correct classes to set up Vaadin's dependency injection system.

### Components Configured

- **VaadinServlet**: Mapped to `/*` with async support enabled
- **SessionHandler**: Configured for HTTP session management
- **ClassLoader**: Set to current thread's context ClassLoader for proper resource loading
- **Package Scanning**: Configured to scan `com.example` package for Vaadin components

## Known Limitations

- **Manual Route Registration**: Unlike Spring Boot integration, routes must be manually registered in `ManualRouteRegistrar`
- **Experimental Status**: This integration is not officially supported by Vaadin or Micronaut
- **Testing**: Additional configuration may be needed for testing frameworks

## Project Structure

```
src/main/java/com/example/
├── Application.java              # Micronaut application entry point
├── VaadinServletConfiguration.java  # Custom Jetty/Vaadin configuration
├── ManualRouteRegistrar.java     # Manual route registration
└── MyView.java                   # Example Vaadin view

src/main/resources/
├── META-INF/services/
│   └── com.vaadin.flow.server.VaadinServiceInitListener
├── application.properties
└── logback.xml
```

## Technologies Used

- **Micronaut Framework** 4.10.1 - Lightweight JVM framework
- **Vaadin Flow** 24.9.4 - Java web framework with server-side components
- **Jetty** 12.1.2 (EE10) - Embedded servlet container
- **Jakarta Servlet API** 6.1.0 - Modern servlet specification

---

## Micronaut Documentation

- [User Guide](https://docs.micronaut.io/4.10.1/guide/index.html)
- [API Reference](https://docs.micronaut.io/4.10.1/api/index.html)
- [Configuration Reference](https://docs.micronaut.io/4.10.1/guide/configurationreference.html)
- [Micronaut Guides](https://guides.micronaut.io/index.html)
- [Micronaut Maven Plugin documentation](https://micronaut-projects.github.io/micronaut-maven-plugin/latest/)

## Feature Documentation

### serialization-jackson
- [Micronaut Serialization Jackson Core documentation](https://micronaut-projects.github.io/micronaut-serialization/latest/guide/)

### micronaut-aot
- [Micronaut AOT documentation](https://micronaut-projects.github.io/micronaut-aot/latest/guide/)

### jetty-server
- [Micronaut Jetty Server documentation](https://micronaut-projects.github.io/micronaut-servlet/latest/guide/index.html#jetty)

### maven-enforcer-plugin
- [https://maven.apache.org/enforcer/maven-enforcer-plugin/](https://maven.apache.org/enforcer/maven-enforcer-plugin/)
