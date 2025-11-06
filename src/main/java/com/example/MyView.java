package com.example;

import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.Route;

@Route("")
public class MyView extends VerticalLayout {

    public MyView() {
        add("Hello World");
    }
}
