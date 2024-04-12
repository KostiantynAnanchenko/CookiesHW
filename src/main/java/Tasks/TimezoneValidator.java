package Tasks;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.io.PrintWriter;
import java.time.ZoneId;
import java.time.ZoneOffset;

@WebFilter("/times")
public class TimezoneValidator extends HttpFilter {
    private static final Logger log = LogManager.getLogger(TimezoneValidator.class);

    @Override
    protected void doFilter(HttpServletRequest req, HttpServletResponse res, FilterChain chain) throws IOException, ServletException {
        String parameter = req.getParameter("timezone");
        log.info("Received timezone parameter: {}", parameter);

        if (parameter != null && !parameter.trim().isEmpty()) {
            validateAndSetTimezone(req, res, parameter);
        }

        chain.doFilter(req, res);
    }

    private void validateAndSetTimezone(HttpServletRequest req, HttpServletResponse res, String parameter) throws IOException {
        String[] parts = parameter.split("\\s+");
        String timezoneId = parts[0];
        int offset = 0;

        if (parts.length > 1) {
            try {
                offset = Integer.parseInt(parts[1]);
            } catch (NumberFormatException e) {
                sendInvalidTimezoneError(res);
                return;
            }
        }

        if (offset < -18 || offset > 18) {
            sendInvalidTimezoneError(res);
            return;
        }

        ZoneId zoneId;
        if (timezoneId.startsWith("UTC") || timezoneId.startsWith("GMT")) {
            ZoneOffset zoneOffset = ZoneOffset.ofHours(offset);
            zoneId = ZoneId.ofOffset(timezoneId, zoneOffset);
        } else {
            try {
                zoneId = ZoneId.of(timezoneId);
            } catch (Exception e) {
                sendInvalidTimezoneError(res);
                return;
            }
        }

        req.setAttribute("zoneId", zoneId);
        log.info("Timezone successfully validated and set: {}", zoneId);
    }

    private void sendInvalidTimezoneError(HttpServletResponse res) throws IOException {
        res.setStatus(HttpServletResponse.SC_BAD_REQUEST);
        res.setContentType("text/html");
        try (PrintWriter out = res.getWriter()) {
            out.println("<html><head><title>Error</title></head><body>");
            out.println("<h1>Invalid timezone offset</h1>");
            out.println("</body></html>");
        }
    }
}