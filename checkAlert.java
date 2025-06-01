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
            position: fixed;
            top: -100px;
            left: 50%%;
            transform: translate(-50%, -100%);
            width: 50%%;
            background-color: #f8d7da;
            color: #721c24;
            border: 1px solid #f5c6cb;
            padding: 15px 20px;
            font-family: Arial, sans-serif;
            font-size: 16px;
            z-index: 9999;
            box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
            transition: transform 0.5s ease;
            border-radius: 8px;
        }

        #alertBanner.show {
            transform: translate(-50%, 0);
        }

        #alertBanner img {
            vertical-align: middle;
            margin-right: 10px;
            height: 24px;
        }

        #alertBanner button {
            float: right;
            background-color: transparent;
            border: none;
            font-size: 16px;
            color: #721c24;
            cursor: pointer;
        }

        #alertBanner button:hover {
            text-decoration: underline;
        }
    </style>

    <div id='alertBanner'>
        <img src='images/alert.gif' alt='Alert'>
        <span>%s</span>
        <button onclick='hideAlert()'>Dismiss</button>
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

