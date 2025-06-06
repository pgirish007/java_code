@WebServlet(asyncSupported = true, urlPatterns = "/checkAlert")
public class AlertServlet extends HttpServlet {

    // Track active users by sessionId and their associated employeeId and AsyncContext
    private static class ClientInfo {
        String employeeId;
        AsyncContext context;

        ClientInfo(String employeeId, AsyncContext context) {
            this.employeeId = employeeId;
            this.context = context;
        }
    }

    private static final Map<String, ClientInfo> waitingClients = new ConcurrentHashMap<>();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        HttpSession session = req.getSession(true);
        String sessionId = session.getId();
        String employeeId = req.getParameter("employeeId");

        if (employeeId == null || employeeId.trim().isEmpty()) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            resp.getWriter().write("Missing employeeId");
            return;
        }

        AsyncContext asyncContext = req.startAsync();
        asyncContext.setTimeout(30000); // hold for 30 seconds max

        waitingClients.put(sessionId, new ClientInfo(employeeId, asyncContext));

        asyncContext.addListener(new AsyncListener() {
            public void onTimeout(AsyncEvent event) {
                waitingClients.remove(sessionId);
                try {
                    HttpServletResponse res = (HttpServletResponse) event.getAsyncContext().getResponse();
                    res.setContentType("application/json");
                    res.getWriter().write("{\"message\": null}");
                    asyncContext.complete();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            public void onComplete(AsyncEvent event) {
                waitingClients.remove(sessionId);
            }

            public void onError(AsyncEvent event) {
                waitingClients.remove(sessionId);
            }

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
                "    #alertBanner.show {\n" +
                "        transform: translate(-50%, 0);\n" +
                "    }\n" +
                "    #alertBanner img {\n" +
                "        vertical-align: middle;\n" +
                "        margin-right: 10px;\n" +
                "        height: 24px;\n" +
                "    }\n" +
                "    #alertBanner button {\n" +
                "        float: right;\n" +
                "        background-color: transparent;\n" +
                "        border: none;\n" +
                "        font-size: 16px;\n" +
                "        color: #721c24;\n" +
                "        cursor: pointer;\n" +
                "    }\n" +
                "    #alertBanner button:hover {\n" +
                "        text-decoration: underline;\n" +
                "    }\n" +
                "</style>\n" +
                "<div id='alertBanner'>\n" +
                "    <img src='images/alert.gif' alt='Alert'>\n" +
                "    <span>" + message + "</span>\n" +
                "    <button onclick='hideAlert()'>Dismiss</button>\n" +
                "</div>";

        for (Map.Entry<String, ClientInfo> entry : waitingClients.entrySet()) {
            AsyncContext ctx = entry.getValue().context;
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

    public static int getActiveUserCount() {
        return waitingClients.size();
    }

    public static Map<String, String> getActiveUsers() {
        Map<String, String> activeUsers = new HashMap<>();
        for (Map.Entry<String, ClientInfo> entry : waitingClients.entrySet()) {
            activeUsers.put(entry.getKey(), entry.getValue().employeeId);
        }
        return activeUsers;
    }
}

<div id="alertContainer"></div>
<script>
  (function () {
    const employeeId = getEmployeeIdFromMeta(); // or another method

    function getEmployeeIdFromMeta() {
        const meta = document.querySelector("meta[name='employeeId']");
        return meta ? meta.getAttribute("content") : null;
    }

let shouldPoll = true;
let currentController = null;

function startLongPolling() {
    const employeeIdMeta = document.querySelector("meta[name='employeeId']");
    const employeeId = employeeIdMeta ? employeeIdMeta.getAttribute("content") : null;

    if (!employeeId || !shouldPoll || document.visibilityState !== "visible") {
        return;
    }

    // Abort any previous controller
    if (currentController) {
        currentController.abort();
    }

    currentController = new AbortController();

    fetch(`/checkAlert?employeeId=${encodeURIComponent(employeeId)}`, {
        signal: currentController.signal
    })
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

        if (shouldPoll && document.visibilityState === "visible") {
            startLongPolling(); // Only restart if visible and allowed
        }
    })
    .catch(err => {
        if (err.name === 'AbortError') {
            // Silently ignore aborted fetch
            return;
        }
        console.warn("Polling error (safe catch):", err);
        if (shouldPoll) {
            setTimeout(startLongPolling, 5000);
        }
    });
}

// Cancel polling if tab is not visible or page is unloading
document.addEventListener("visibilitychange", () => {
    if (document.visibilityState === "visible") {
        startLongPolling();
    } else if (currentController) {
        currentController.abort();
    }
});

window.addEventListener("beforeunload", () => {
    shouldPoll = false;
    if (currentController) {
        currentController.abort();
    }
});

    function hideAlert() {
        const banner = document.getElementById("alertBanner");
        if (banner) {
            banner.classList.remove("show");
            setTimeout(() => {
                banner.style.display = "none";
            }, 500);
        }
    }

    // Expose hideAlert globally for inline button handler
    window.hideAlert = hideAlert;

    // Start polling immediately
    startLongPolling();
})();

</script>

