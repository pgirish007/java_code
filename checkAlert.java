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

public static void broadcastAlert(String message) {
    String alertHtml = """
        <style>
            #alertBanner {
                display: none;
                background-color: #f8d7da;
                color: #721c24;
                border: 1px solid #f5c6cb;
                padding: 15px;
                margin: 10px;
                border-radius: 5px;
                position: fixed;
                top: 20px;
                left: 20%;
                right: 20%;
                z-index: 1000;
                font-family: Arial, sans-serif;
            }

            #alertBanner img {
                vertical-align: middle;
                margin-right: 10px;
                height: 24px;
            }

            #alertBanner button {
                float: right;
                background-color: #f5c6cb;
                border: none;
                padding: 5px 10px;
                cursor: pointer;
            }
        </style>

        <div id='alertBanner'>
            <img src='images/alert.gif' alt='Alert'>
            <span>%s</span>
            <button onclick='hideAlert()'>OK</button>
        </div>
    """.formatted(message);

    for (AsyncContext ctx : waitingClients) {
        try {
            HttpServletResponse res = (HttpServletResponse) ctx.getResponse();
            res.setContentType("text/html");
            res.getWriter().write(alertHtml);
            ctx.complete();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    waitingClients.clear();
}

}


<div id="alertContainer"></div>
<script>
    window.onload = function () {
        startLongPolling();
    };

    function startLongPolling() {
        fetch('/checkAlert')
            .then(response => response.text())
            .then(alertHtml => {
                if (alertHtml && alertHtml.trim()) {
                    document.getElementById("alertContainer").innerHTML = alertHtml;
                    document.getElementById("alertBanner").style.display = "block";
                }
                startLongPolling();
            })
            .catch(err => {
                console.error("Polling error:", err);
                setTimeout(startLongPolling, 5000);
            });
    }

    function hideAlert() {
        const banner = document.getElementById("alertBanner");
        if (banner) banner.style.display = "none";
    }
</script>

