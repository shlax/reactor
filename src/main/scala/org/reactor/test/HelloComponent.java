package org.reactor.test;

import jakarta.faces.component.FacesComponent;
import jakarta.faces.component.UIComponentBase;
import jakarta.faces.context.FacesContext;
import jakarta.faces.context.ResponseWriter;

import java.io.IOException;

@FacesComponent(createTag = true, tagName = "helloComponent", namespace = "hello.components")
public class HelloComponent extends UIComponentBase {

    public HelloComponent(){
        System.out.println("create:HelloComponent");
    }
    
    @Override
    public String getFamily() {
        return "hello.components";
    }

    @Override
    public void encodeAll(FacesContext context) throws IOException {
        String txt = (String) getAttributes().get("value");
        ResponseWriter writer = context.getResponseWriter();
        writer.startElement("div", this);
        writer.writeText(txt, null);
        writer.endElement("div");
    }
}
