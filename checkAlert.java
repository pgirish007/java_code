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
String alertHtml = "<style>\n" +
"    #alertBanner {\n" +
"        position: fixed;\n" +
"        top: -100px;\n" +
"        left: 50%;\n" +
"        transform: translate(-50%, -100%);\n" +
"        width: 50%;\n" +
"        background-color: #f8d7da;\n" +
"        color: #721c24;\n" +
"        border: 1px solid #f5c6cb;\n" +
"        padding: 15px 20px;\n" +
"        font-family: Arial, sans-serif;\n" +
"        font-size: 16px;\n" +
"        z-index: 9999;\n" +
"        box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);\n" +
"        transition: transform 0.5s ease;\n" +
"        border-radius: 8px;\n" +
"    }\n" +
"\n" +
"    #alertBanner.show {\n" +
"        transform: translate(-50%, 0);\n" +
"    }\n" +
"\n" +
"    #alertBanner img {\n" +
"        vertical-align: middle;\n" +
"        margin-right: 10px;\n" +
"        height: 24px;\n" +
"    }\n" +
"\n" +
"    #alertBanner button {\n" +
"        float: right;\n" +
"        background-color: transparent;\n" +
"        border: none;\n" +
"        font-size: 16px;\n" +
"        color: #721c24;\n" +
"        cursor: pointer;\n" +
"    }\n" +
"\n" +
"    #alertBanner button:hover {\n" +
"        text-decoration: underline;\n" +
"    }\n" +
"</style>\n" +
"\n" +
"<div id='alertBanner'>\n" +
"    <img src='images/alert.gif' alt='Alert'>\n" +
"    <span>" + message + "</span>\n" +
"    <button onclick='hideAlert()'>Dismiss</button>\n" +
"</div>";




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
            const trimmed = alertHtml.trim();
            if (trimmed && trimmed.includes("alertBanner")) {
                const container = document.getElementById("alertContainer");
                container.innerHTML = trimmed;

                const banner = document.getElementById("alertBanner");
                setTimeout(() => {
                    banner.classList.add("show");
                }, 50);
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
    if (banner) {
        banner.classList.remove("show");
        setTimeout(() => {
            banner.style.display = "none";
        }, 500);
    }
}
</script>

