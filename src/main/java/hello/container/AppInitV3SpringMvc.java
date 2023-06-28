package hello.container;

import hello.spring.HelloConfig;
import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletException;
import org.springframework.web.WebApplicationInitializer;
import org.springframework.web.context.support.AnnotationConfigWebApplicationContext;
import org.springframework.web.servlet.DispatcherServlet;


public class AppInitV3SpringMvc implements WebApplicationInitializer {
    @Override
    public void onStartup(ServletContext servletContext) throws ServletException {
        System.out.println("AppInitV3SpringMvc.onStartup");

        // Create Spring Container
        AnnotationConfigWebApplicationContext appContext = new AnnotationConfigWebApplicationContext();
        appContext.register(HelloConfig.class);

        // Create Spring MVC Dispatcher Servlet
        DispatcherServlet dispatcher = new DispatcherServlet(appContext);

        // Add Dispatcher Servlet to Spring Container
        // 모든 요청
        servletContext
                .addServlet("dispatcherV3", dispatcher)
                .addMapping("/");
    }
}
