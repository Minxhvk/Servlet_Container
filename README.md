# Servlet Container ( 스프링 부트 - 핵심 원리와 활용 김영한님 강의 내용)

<br/>

## JAR & WAR

**JAR ( Java Archive )**

- JAVA는 `여러 클래스와 리소스를 묶어서 JAR라는 압축 파일`을 만들 수 있다.
- JVM 위에서 직접 실행되거나 또는 다른 곳에서 사용하는 라이브러리로 제공된다.
- 직접 실행하는 경우 main() 메서드가 필요하고, MANIFEST.MF 파일에 실행할 메인 메서드가 있는 클래스를 지정해야 한다.
<br/>

**WAR ( Web Application Archive )**

- 이름 그대로 WAS에 배포할 때 사용하는 파일
- JAR는 JVM위에서 실행된다면, WAR는 WAS 위에서 실행된다.
<br/>

**WAR 구조**

![image](https://github.com/Minxhvk/Servlet_Container/assets/116830944/539ae7fc-aed2-4041-81b8-9e9c8dbd9a54)


- WEB-INF
    - classes : 실행 클래스 모음
    - lib : 라이브러리 모음
    - web.xml : 웹 서버 배치 설정 파일 ( 생략 가능 )
    - index.html : 정적 리소스

> WEB-INF 폴더 하위는 클래스, 라이브러리, 설정 정보가 들어가는 곳이다. 
제외한 영역은 정적 리소스 (HTML, CSS)가 사용되는 영역
> 
<br/>

**톰켓에 직접 배포 방법**

- ./gradlew build로 war 파일 빌드
- 톰켓폴더/webapps 에서 ROOT.war 파일이름으로 넣으면 된다.

*⇒ 해당 부분은 IDE를 통해 관리할 수 있음*
<br/><br/>

## Servlet 초기화 및 등록

- WAS를 실행하는 시점에 서비스에 필요한 필터와 서블릿을 등록해야 한다.
- 스프링을 사용한다면 `스프링 컨테이너`를 만들고, 서블릿과 스프링을 연결하는 `Dispatcher Servlet`도 등록해야 한다.
<br/>

**서블릿 컨테이너 초기화**

- Servlet은 `ServletContainerInitailizer`라는 초기화 인터페이스를 제공한다.
    - 서블릿 컨테이너를 초기화 하는 기능을 제공한다.
    - `onStartup()` 메서드를 실행해 애플리케이션에 필요한 기능들을 초기화 하거나 등록할 수 있다.

```java
public interface ServletContainerInitializer {
	public void onStartup(Set<Class<?>> c, ServletContext ctx) throws 
ServletException;
}
```

- Set<Class<?>> c : 유연한 초기화 기능을 제공한다. `@HandlesTypes` 어노테이션과 함께 사용한다.
- ServletContext ctx : 서블릿 컨테이너 자체의 기능을 제공한다. 필터나 서블릿을 등록할 수 있다.

- `resources/META-INF/services/jakarta.servlet.ServletContainerInitializer` 파일에
    
    hello.container.MyContainerInitV1 와 같이 클래스를 지정해주면, 
    
    WAS 실행 시 해당 클래스를 초기화 클래스로 인식하고 로딩 시점에 실행한다.
    

![image](https://github.com/Minxhvk/Servlet_Container/assets/116830944/462436ac-c680-46bb-81d3-e7f547fd03f5)

<br/><br/>

**서블릿을 등록하는 방법**

<br/>

**@WebServlet**

```java
@WebServlet(urlPatterns = "/test")
public class TestServlet extends HttpServlet {
    @Override
    protected void service(HttpServletRequest req, HttpServletResponse resp) 
throws ServletException, IOException {

        System.out.println("TestServlet.service");
        resp.getWriter().println("test");
    }
}
```
<br/>

**프로그래밍 방식**

```java
// HelloServlet 생성

public class HelloServlet extends HttpServlet {

    @Override
    protected void service(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        System.out.println("HelloServlet.service");
        resp.getWriter().println("hello servlet!");
    }
}
```
<br/>

**애플리케이션 초기화**

- 애플리케이션 초기화를 진행하려면 인터페이스를 만들어야 한다. ( 내용과 형식은 상관없다. )

```java
public interface AppInit {
    void onStartup(ServletContext servletContext);
}
```
<br/>

**AppInit 구현**

```java
public class AppInitV1Servlet implements AppInit{

    @Override
    public void onStartup(ServletContext servletContext) {
        System.out.println("AppInitV1Servlet.onStartup");

        // 순수 Servlet Code 등록
        // '/hello-servlet'을 호출하면, helloServlet이 실행된다.
        servletContext.addServlet("helloServlet", new HelloServlet())
                      .addMapping("/hello-servlet");
    }
}
```

- HelloServlet을 서블릿 컨테이너에 직접 등록한다.
- HTTP로 /hello-servlet을 호출하면, HelloServlet이 실행된다.

> @WebServlet을 사용하면 어노테이션 하나로 서블릿을 편리하게 등록할 수 있다.
하지만, 유연하게 변경하는 것이 어렵다.

프로그래밍 방식은 특정 조건에 따라 if문으로 분기해서 서블릿을 등록하거나 뺄 수 있다.
또한, 서블릿을 직접 생성하기 때문에 생성자에 필요한 정보를 넘길 수 있다.
> 
<br/>

**AppInit을 초기화 하는 방법**

```java
@HandlesTypes(AppInit.class)
public class MyContainerInitV2 implements ServletContainerInitializer {

    @Override
    public void onStartup(Set<Class<?>> c, ServletContext ctx) throws ServletException {
        System.out.println("MyContainerInitV2.onStartup");
        System.out.println("c = " + c);
        System.out.println("ctx = " + ctx);

        // class hello.container.AppInitV1Servlet
        for (Class<?> appInitClass : c) {
            try {
                // Reflection 을 통해 객체 생성
                AppInit appInit = (AppInit) appInitClass.getDeclaredConstructor()
																											  .newInstance();
                appInit.onStartup(ctx);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
```

- `@HandlesTypes` 어노테이션에 어플리케이션 초기화 인터페이스를 지정한다.
- `ServeltContainerInitializer`는 파라미터로 넘어오는 Set<Class<?>> c 에 `인터페이스의 구현체`들을 모두 찾아서 `클래스 정보`로 전달한다.
- 리플렉션을 사용해 객체를 생성한 뒤, 초기화 코드를 직접 실행하며 서블릿 컨테이너 정보가 담긴 ctx도 함께 전달한다.

*실행 로그*

> *1. 서블릿 컨테이너 초기화 실행*
MyContainerInitV2.onStartup
MyContainerInitV2 c = [class hello.container.AppInitV1Servlet]
MyContainerInitV2 container =
org.apache.catalina.core.ApplicationContextFacade@38dd0980

*2. 애플리케이션 초기화 실행*
AppInitV1Servlet.onStartup
> 

![image](https://github.com/Minxhvk/Servlet_Container/assets/116830944/d3aaa474-ceff-4deb-8a0f-16eadde74fa1)


애플리케이션 초기화의 목적

- 서블릿 컨테이너에 상관없이 원하는 모양으로 인터페이스를 만들 수 있다. ( `의존성 제거` )
<br/><br/>

## 스프링 컨테이너 등록

![image](https://github.com/Minxhvk/Servlet_Container/assets/116830944/28ab49dd-603e-493f-a36f-22193f804bed)

```java
public class AppInitV2Spring implements AppInit {

    @Override
    public void onStartup(ServletContext servletContext) {
        System.out.println("AppInitV2Spring.onStartup");

        // 스프링 컨테이너 생성
        AnnotationConfigWebApplicationContext appContext = 
													new AnnotationConfigWebApplicationContext();
        appContext.register(HelloConfig.class);

        // Spring MVC Dispatcher Servlet 생성

        DispatcherServlet dispatcher = new DispatcherServlet(appContext);

        // 스프링 컨테이너에 Dispatcher Servlet 등록.
        servletContext
                .addServlet("dispatcherV2", dispatcher)
                .addMapping("/spring/*");
    }
}
```

- Dispatcher Servlet에 HTTP 요청이 오면, 해당 스프링 컨테이너에 들어있는 `컨트롤러 빈들을 호출`한다.
<br/><br/>

## WebApplicationInitializer

- 서블릿 컨테이너 초기화 과정을 개발자가 아닌 스프링 MVC가 알아서 해준다.
- AppInit과 같이 어플리케이션을 초기화 하는 Interface가 `WebApplicationInitializer` 이다.
- 단순하게 AppInit을 `WebApplicationInitializer` 로 바꿔주면 된다.
<br/>

**동작방식**

spring-web 라이브러리를 열어보면, 위에서 했던 방법과 동일하게 `jakarta 파일에 컨테이너를 등록`하는 

과정을 거친다.

![image](https://github.com/Minxhvk/Servlet_Container/assets/116830944/a9e53bd6-7e1b-40cb-a435-17c8662332b5)

등록된 클래스인 `SpringServeltContainerInitializer`를 보면, 

`@HandlesTypes` 를 통해 어플리케이션을 초기화하는 것을 볼 수 있다.

![image](https://github.com/Minxhvk/Servlet_Container/assets/116830944/28c2101f-e79d-400c-beb6-023e92bbb17f)

<br/><br/>

### 최종

![image](https://github.com/Minxhvk/Servlet_Container/assets/116830944/bd87ed66-aa36-4983-86d5-b63f328fc125)
