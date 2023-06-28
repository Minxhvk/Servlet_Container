package hello.container;

import hello.spring.HelloConfig;
import jakarta.servlet.ServletContext;
import org.springframework.web.context.support.AnnotationConfigWebApplicationContext;
import org.springframework.web.servlet.DispatcherServlet;

public class AppInitV2Spring implements AppInit {

    @Override
    public void onStartup(ServletContext servletContext) {
        System.out.println("AppInitV2Spring.onStartup");

        // Create Spring Container
        AnnotationConfigWebApplicationContext appContext = new AnnotationConfigWebApplicationContext();
        appContext.register(HelloConfig.class);

        // Create Spring MVC Dispatcher Servlet
        DispatcherServlet dispatcher = new DispatcherServlet(appContext);

        // Add Dispatcher Servlet to Spring Container
        servletContext
                .addServlet("dispatcherV2", dispatcher)
                .addMapping("/spring/*");
    }
}
