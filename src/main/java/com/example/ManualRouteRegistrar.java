package com.example;

import com.vaadin.flow.server.ServiceInitEvent;
import com.vaadin.flow.server.VaadinServiceInitListener;
import com.vaadin.flow.router.RouteConfiguration;

public class ManualRouteRegistrar implements VaadinServiceInitListener {

    @Override
    public void serviceInit(ServiceInitEvent event) {
        // Use application-scoped route configuration for service-level registration
        RouteConfiguration routeConfiguration = RouteConfiguration.forApplicationScope();

        // Manually register all routes
        routeConfiguration.setRoute("", MyView.class);

        System.out.println("=== Manually registered route: '' -> MyView ===");
    }
}
