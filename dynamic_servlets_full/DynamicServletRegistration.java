
import java.io.IOException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import jakarta.servlet.Servlet;
import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRegistration;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public class DynamicServletRegistration {

    private static final Map<String, ServletRegistration.Dynamic> registeredServlets = new HashMap<>();

    public static void registerDynamicServlets(ServletContext servletContext) {
        ServletConfigJson config = DatabaseUtil.getServletConfigurationFromJson();

        if (config != null && !registeredServlets.containsKey(config.getServletName())) {
            registerServlet(servletContext, config);
        }
    }

    public static void reloadServlets(ServletContext servletContext) {
        ServletConfigJson newConfig = DatabaseUtil.getServletConfigurationFromJson();

        if (newConfig != null) {
            ServletRegistration.Dynamic existingServlet = registeredServlets.get(newConfig.getServletName());

            if (existingServlet == null || isConfigChanged(existingServlet, newConfig)) {
                if (existingServlet != null) {
                    unregisterServlet(servletContext, newConfig.getServletName());
                }
                registerServlet(servletContext, newConfig);
            }
        }
    }

    private static void registerServlet(ServletContext servletContext, ServletConfigJson config) {
        Servlet dynamicServlet = createDynamicServlet(config);
        ServletRegistration.Dynamic dynamicServletReg = servletContext.addServlet(config.getServletName(), dynamicServlet);
        dynamicServletReg.addMapping(config.getUrlPattern());
        registeredServlets.put(config.getServletName(), dynamicServletReg);
        System.out.println("Servlet [" + config.getServletName() + "] registered at: " + config.getUrlPattern());
    }

    private static void unregisterServlet(ServletContext servletContext, String servletName) {
        ServletRegistration.Dynamic dynamicServletReg = registeredServlets.remove(servletName);
        if (dynamicServletReg != null) {
            dynamicServletReg.setLoadOnStartup(-1);
            servletContext.removeAttribute(servletName);
            System.out.println("Servlet [" + servletName + "] unregistered.");
        }
    }

    private static Servlet createDynamicServlet(ServletConfigJson config) {
        return new HttpServlet() {
            @Override
            protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
                try {
                    invokeDynamicMethodWithParams(config.getClassName(), config.getMethodName(), req, resp);
                } catch (Exception e) {
                    resp.getWriter().write("Error: " + e.getMessage());
                }
            }

            @Override
            protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
                doGet(req, resp);
            }
        };
    }

    private static void invokeDynamicMethodWithParams(String className, String methodName, HttpServletRequest req, HttpServletResponse resp)
            throws Exception {
        Class<?> clazz = Class.forName(className);
        Object instance = clazz.getDeclaredConstructor().newInstance();
        Method method = clazz.getMethod(methodName, HttpServletRequest.class, HttpServletResponse.class);
        method.invoke(instance, req, resp);
    }

    private static boolean isConfigChanged(ServletRegistration.Dynamic existingServlet, ServletConfigJson newConfig) {
        return !existingServlet.getMappings().contains(newConfig.getUrlPattern());
    }
}
