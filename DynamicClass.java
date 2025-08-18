import org.springframework.boot.web.servlet.ServletContextInitializer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.servlet.Servlet;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.ServletRegistration;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.util.Set;

import org.reflections.Reflections;

@Configuration
public class DynamicServletRegistrar {

    @Bean
    public ServletContextInitializer servletInitializer() {
        return servletContext -> {
            // Scan for @WebServlet in all packages
            Reflections reflections = new Reflections("com.mycompany"); // base package(s)
            Set<Class<?>> servletClasses = reflections.getTypesAnnotatedWith(WebServlet.class);

            for (Class<?> clazz : servletClasses) {
                if (Servlet.class.isAssignableFrom(clazz)) {
                    WebServlet annotation = clazz.getAnnotation(WebServlet.class);
                    String[] urlPatterns = annotation.urlPatterns();
                    Servlet servletInstance = (Servlet) clazz.getDeclaredConstructor().newInstance();

                    ServletRegistration.Dynamic registration = servletContext.addServlet(clazz.getSimpleName(), servletInstance);
                    registration.addMapping(urlPatterns);
                    registration.setLoadOnStartup(annotation.loadOnStartup());
                }
            }
        };
    }
}
