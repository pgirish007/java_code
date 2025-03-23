
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

public class MyHandler {

    public void handleRequest(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String name = req.getParameter("name");
        if (name == null || name.isEmpty()) {
            name = "Guest";
        }
        resp.getWriter().write("Hello, " + name + "! This is a dynamic servlet response.");
    }
}
