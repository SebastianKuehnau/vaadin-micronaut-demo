package com.example;

import com.vaadin.flow.server.VaadinServlet;
import com.vaadin.flow.server.startup.LookupServletContainerInitializer;
import io.micronaut.context.event.BeanCreatedEvent;
import io.micronaut.context.event.BeanCreatedEventListener;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.handler.ContextHandlerCollection;
import org.eclipse.jetty.ee10.servlet.ServletContextHandler;
import org.eclipse.jetty.ee10.servlet.ServletHolder;
import jakarta.inject.Singleton;
import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletException;
import java.util.Set;

@Singleton
public class VaadinServletConfiguration implements BeanCreatedEventListener<Server> {

    @Override
    public Server onCreated(BeanCreatedEvent<Server> event) {
        Server server = event.getBean();

        try {
            Handler handler = server.getHandler();
            ServletContextHandler servletContext = null;

            // Find ServletContextHandler from the server's handler hierarchy
            if (handler instanceof ServletContextHandler) {
                servletContext = (ServletContextHandler) handler;
            } else if (handler instanceof ContextHandlerCollection collection) {
                for (Handler h : collection.getHandlers()) {
                    if (h instanceof ServletContextHandler) {
                        servletContext = (ServletContextHandler) h;
                        break;
                    }
                }
            }

            if (servletContext != null) {
                // Set the class loader for the ServletContext
                servletContext.setClassLoader(Thread.currentThread().getContextClassLoader());

                // Ensure SessionHandler is configured (required for Vaadin)
                if (servletContext.getSessionHandler() == null) {
                    org.eclipse.jetty.ee10.servlet.SessionHandler sessionHandler =
                        new org.eclipse.jetty.ee10.servlet.SessionHandler();
                    servletContext.setSessionHandler(sessionHandler);
                }

                // Enable classpath scanning for Vaadin
                servletContext.setAttribute("org.eclipse.jetty.server.webapp.ContainerIncludeJarPattern", ".*");

                // Manually initialize Vaadin's LookupServletContainerInitializer
                try {
                    ServletContext ctx = servletContext.getServletContext();
                    LookupServletContainerInitializer initializer = new LookupServletContainerInitializer();
                    // Pass the LookupInitializer class that Vaadin requires
                    Set<Class<?>> classes = Set.of(Class.forName("com.vaadin.flow.di.LookupInitializer"));
                    initializer.onStartup(classes, ctx);
                } catch (ServletException | ClassNotFoundException e) {
                    throw new RuntimeException("Failed to initialize Vaadin LookupServletContainerInitializer", e);
                }

                // Register Vaadin servlet
                ServletHolder vaadinServlet = new ServletHolder("VaadinServlet", VaadinServlet.class);
                vaadinServlet.setInitOrder(1);
                vaadinServlet.setAsyncSupported(true);

                // Configure package scanning for Vaadin views
                vaadinServlet.setInitParameter("vaadin.allowed-packages", "com.example");

                servletContext.addServlet(vaadinServlet, "/*");
            } else {
                throw new IllegalStateException("Could not find ServletContextHandler in Jetty server");
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to configure Vaadin servlet", e);
        }

        return server;
    }
}
