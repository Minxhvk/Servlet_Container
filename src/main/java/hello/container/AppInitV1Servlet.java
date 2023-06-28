package hello.container;

import hello.servlet.HelloServlet;
import jakarta.servlet.ServletContext;

public class AppInitV1Servlet implements AppInit{

    @Override
    public void onStartup(ServletContext servletContext) {
        System.out.println("AppInitV1Servlet.onStartup");

        // 순수 Servlet Code 등록
        // '/hello-servlet' 을 호출하면, helloServlet 이 실행된다.
        servletContext.addServlet("helloServlet", new HelloServlet())
                      .addMapping("/hello-servlet");
    }
}
