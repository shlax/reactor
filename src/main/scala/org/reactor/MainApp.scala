package org.reactor

import com.sun.faces.config.FacesInitializer
import jakarta.faces.webapp.FacesServlet
import org.eclipse.jetty.cdi.{CdiDecoratingListener, CdiServletContainerInitializer}
import org.eclipse.jetty.server.Server
import org.eclipse.jetty.servlet.ServletContextHandler
import org.eclipse.jetty.websocket.jakarta.server.config.JakartaWebSocketServletContainerInitializer
import org.jboss.weld.environment.servlet.EnhancedListener
import org.eclipse.jetty.util.resource.Resource
import org.eclipse.jetty.webapp.WebAppContext
import org.glassfish.jersey.server.ServerProperties
import org.glassfish.jersey.servlet.ServletContainer
import org.reactor.test.HelloServlet
import org.slf4j.bridge.SLF4JBridgeHandler

import java.net.URI

object MainApp {

  // Wire up java.util.logging (used by weld) to slf4j.
  SLF4JBridgeHandler.removeHandlersForRootLogger()
  SLF4JBridgeHandler.install()

  def main(args:Array[String]):Unit = {

    val server = new Server(8080)

    val beans = "/META-INF/beans.xml"
    val webRootLocation = this.getClass.getResource(beans);
    if (webRootLocation == null) throw new IllegalStateException("Unable to determine webroot URL location");

    val beansUri = webRootLocation.toURI.toASCIIString
    val webRootUri = URI.create(beansUri.substring(0, beansUri.length - beans.length))

    val context = new ServletContextHandler(ServletContextHandler.SESSIONS)
    context.setBaseResource(Resource.newResource(webRootUri))
    context.setContextPath("/")

    // Expect:INFO: WELD-ENV-001212: Jetty CdiDecoratingListener support detected, CDI injection will be available in Listeners, Servlets and Filters
    context.setInitParameter(CdiServletContainerInitializer.CDI_INTEGRATION_ATTRIBUTE, CdiDecoratingListener.MODE)
    context.addServletContainerInitializer(new CdiServletContainerInitializer())
    context.addServletContainerInitializer(new EnhancedListener())
    context.addServletContainerInitializer(new FacesInitializer)

    // jersey servlet
    val jersey = context.addServlet(classOf[ServletContainer], "/api/*")
    jersey.setInitParameter(ServerProperties.PROVIDER_PACKAGES, this.getClass.getPackageName)
    jersey.setInitOrder(1)

    // mojarra servlet
    context.addServlet(classOf[FacesServlet], "*.xhtml")

    // Initialize jakarta.websocket layer
    JakartaWebSocketServletContainerInitializer.configure(context, null)

    // hello servlet
    context.addServlet(classOf[HelloServlet], "/hello")

    server.setHandler(context)
    server.start()
  }

}
