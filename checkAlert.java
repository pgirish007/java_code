@WebServlet(asyncSupported = true, urlPatterns = "/checkAlert")
public class AlertServlet extends HttpServlet {

    // Store AsyncContext for all connected clients
    private static final List<AsyncContext> waitingClients = new CopyOnWriteArrayList<>();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        AsyncContext asyncContext = req.startAsync();
        asyncContext.setTimeout(30000); // hold for 30 seconds max
        waitingClients.add(asyncContext);

        // On timeout, clean up
        asyncContext.addListener(new AsyncListener() {
            public void onTimeout(AsyncEvent event) {
                waitingClients.remove(asyncContext);
                try {
                    HttpServletResponse res = (HttpServletResponse) event.getAsyncContext().getResponse();
                    res.setContentType("application/json");
                    res.getWriter().write("{\"message\": null}"); // no alert
                    asyncContext.complete();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            public void onComplete(AsyncEvent event) {}
            public void onError(AsyncEvent event) {}
            public void onStartAsync(AsyncEvent event) {}
        });
    }

    // Call this method from admin action to broadcast alert
    public static void broadcastAlert(String alertMessage) {
        for (AsyncContext ctx : waitingClients) {
            try {
                HttpServletResponse res = (HttpServletResponse) ctx.getResponse();
                res.setContentType("application/json");
                res.getWriter().write("{\"message\": \"" + alertMessage + "\"}");
                ctx.complete();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        waitingClients.clear();
    }
}


<script>
window.onload = function() {
    console.log("Page fully loaded");
    startLongPolling();
};

function startLongPolling() {
    fetch('/checkAlert')
        .then(response => response.json())
        .then(data => {
            if (data && data.message) {
                alert("Admin Alert: " + data.message);
            }
            // Restart the long poll
            startLongPolling();
        })
        .catch(err => {
            console.error("Error in long polling", err);
            // Retry after a delay
            setTimeout(startLongPolling, 5000);
        });
}
</script>
