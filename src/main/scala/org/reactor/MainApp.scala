package org.reactor

import com.sun.faces.config.FacesInitializer
import jakarta.faces.webapp.FacesServlet
import jakarta.servlet.ServletContext
import org.eclipse.jetty.cdi.{CdiDecoratingListener, CdiServletContainerInitializer}
import org.eclipse.jetty.server.Server
import org.eclipse.jetty.servlet.ServletContextHandler
import org.eclipse.jetty.websocket.jakarta.server.config.JakartaWebSocketServletContainerInitializer
import org.jboss.weld.environment.servlet.EnhancedListener
import org.eclipse.jetty.util.resource.Resource
import org.eclipse.jetty.webapp.WebAppContext
import org.glassfish.jersey.server.ServerProperties
import org.glassfish.jersey.servlet.ServletContainer
import org.reactor.test.{HelloComponent, HelloServlet}
import org.slf4j.bridge.SLF4JBridgeHandler

import java.net.URI
import java.util
import java.util.Collections

object MainApp {

  // Wire up java.util.logging (used by weld) to slf4j.
  SLF4JBridgeHandler.removeHandlersForRootLogger()
  SLF4JBridgeHandler.install()

  def main(args:Array[String]):Unit = {

    val beans = "/META-INF/beans.xml"
    val webRootLocation = this.getClass.getResource(beans);
    if (webRootLocation == null) throw new IllegalStateException("Unable to determine webroot URL location");

    val beansUri = webRootLocation.toURI.toASCIIString
    val webRootUri = URI.create(beansUri.substring(0, beansUri.length + 1 - beans.length))

    val context = new ServletContextHandler(ServletContextHandler.SESSIONS)
    context.setBaseResource(Resource.newResource(webRootUri))
    context.setContextPath("/")

    // Expect:INFO: WELD-ENV-001212: Jetty CdiDecoratingListener support detected, CDI injection will be available in Listeners, Servlets and Filters
    context.setInitParameter(CdiServletContainerInitializer.CDI_INTEGRATION_ATTRIBUTE, CdiDecoratingListener.MODE)
    context.addServletContainerInitializer(new CdiServletContainerInitializer)
    context.addServletContainerInitializer(new EnhancedListener) // weld initializer
    context.addServletContainerInitializer(new FacesInitializer{
      override def onStartup(classes: util.Set[Class[_]], servletContext: ServletContext): Unit = {
        // register jsf classes
        FacesInitializer.addAnnotatedClasses(Collections.singleton(classOf[HelloComponent]), servletContext)
        super.onStartup(classes, servletContext)
      }
    }) // mojarra initializer

    // jersey servlet
    val jersey = context.addServlet(classOf[ServletContainer], "/api/*")
    jersey.setInitParameter(ServerProperties.PROVIDER_PACKAGES, this.getClass.getPackageName)
    jersey.setInitParameter(ServerProperties.WADL_FEATURE_DISABLE, "true") // disable warning: JAX-B API not found . WADL feature is disabled.
    jersey.setInitOrder(1)

    // mojarra servlet
    val mojarra = context.addServlet(classOf[FacesServlet], "*.xhtml")
    mojarra.setInitOrder(2)

    // Initialize jakarta.websocket layer
    JakartaWebSocketServletContainerInitializer.configure(context, null)

    // hello servlet
    context.addServlet(classOf[HelloServlet], "/hello")

    val server = new Server(8080)
    server.setHandler(context)
    server.start()
  }

}
