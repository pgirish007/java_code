
import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;
import jakarta.servlet.annotation.WebListener;
import java.util.Timer;
import java.util.TimerTask;

@WebListener
public class AppInitializer implements ServletContextListener {

    private Timer timer;

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        System.out.println("Initializing dynamic servlets...");
        registerServlets(sce.getServletContext());
        startConfigPollingTask(sce.getServletContext());
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        System.out.println("Application shutting down...");
        if (timer != null) {
            timer.cancel();
        }
    }

    private void registerServlets(ServletContext servletContext) {
        DynamicServletRegistration.registerDynamicServlets(servletContext);
    }

    private void startConfigPollingTask(ServletContext servletContext) {
        timer = new Timer(true);
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                System.out.println("Checking for servlet configuration updates...");
                DynamicServletRegistration.reloadServlets(servletContext);
            }
        }, 0, 10000);
    }
}
