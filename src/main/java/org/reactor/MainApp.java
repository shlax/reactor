package org.reactor;

import com.sun.faces.config.FacesInitializer;
import jakarta.faces.webapp.FacesServlet;
import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletException;
import org.eclipse.jetty.cdi.CdiDecoratingListener;
import org.eclipse.jetty.cdi.CdiServletContainerInitializer;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.jboss.weld.environment.servlet.EnhancedListener;
import org.eclipse.jetty.util.resource.Resource;
import org.slf4j.bridge.SLF4JBridgeHandler;

import java.net.URI;
import java.util.Set;

public class MainApp {

    static {
        // Wire up java.util.logging (used by weld) to slf4j.
        SLF4JBridgeHandler.removeHandlersForRootLogger();
        SLF4JBridgeHandler.install();
    }


    public static void main(String[] args) throws Exception{

        var beans = "/META-INF/beans.xml";
        var webRootLocation = MainApp.class.getResource(beans);
        if (webRootLocation == null) throw new IllegalStateException("Unable to determine webroot URL location");

        var beansUri = webRootLocation.toURI().toASCIIString();
        var webRootUri = URI.create(beansUri.substring(0, beansUri.length() + 1 - beans.length()));

        var context = new ServletContextHandler(ServletContextHandler.SESSIONS);
        context.setBaseResource(Resource.newResource(webRootUri));
        context.setContextPath("/");

        // context.setInitParameter(PushContext.ENABLE_WEBSOCKET_ENDPOINT_PARAM_NAME, "true")
        context.setClassLoader(MainApp.class.getClassLoader());

        // Expect:INFO: WELD-ENV-001212: Jetty CdiDecoratingListener support detected, CDI injection will be available in Listeners, Servlets and Filters
        context.setInitParameter(CdiServletContainerInitializer.CDI_INTEGRATION_ATTRIBUTE, CdiDecoratingListener.MODE);
        context.addServletContainerInitializer(new CdiServletContainerInitializer());
        context.addServletContainerInitializer(new EnhancedListener()); // weld initializer
        context.addServletContainerInitializer(new FacesInitializer() { // mojarra initializer
            @Override
            public void onStartup(Set<Class<?>> classes, ServletContext servletContext) throws ServletException {
                // register jsf classes
                super.onStartup(classes, servletContext);
            }
        });

        // mojarra servlet
        var mojarra = context.addServlet(FacesServlet.class, "*.xhtml");
        mojarra.setInitOrder(1);

        // context.addServlet(classOf[DefaultServlet], "/static/*")

        var server = new Server(8080);
        server.setHandler(context);
        server.start();
    }

}
