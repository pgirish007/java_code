
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

@WebServlet("/admin/reload")
public class AdminReloadServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        try {
            DynamicServletRegistration.reloadServlets(getServletContext());
            resp.getWriter().write("Configuration reloaded successfully.");
        } catch (Exception e) {
            resp.getWriter().write("Error reloading configuration: " + e.getMessage());
        }
    }
}
