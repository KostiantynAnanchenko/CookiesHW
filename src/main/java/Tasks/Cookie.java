package Tasks;


import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Arrays;
import java.util.Optional;

public class Cookie {
    private static final String COOKIE_NAME = "lastTimezone";
    private static final Logger log = LogManager.getLogger(Cookie.class);

    private Cookie() {
    }

    public static Optional<String> getLastTimezone(HttpServletRequest req) {
        jakarta.servlet.http.Cookie[] cookies = req.getCookies();
        if (cookies != null) {
            Optional<jakarta.servlet.http.Cookie> cookie = Arrays.stream(cookies)
                    .filter(c -> c.getName().equals(COOKIE_NAME))
                    .findFirst();
            if (cookie.isPresent()) {
                String timezone = cookie.get().getValue();
                log.info("Received timezone from cookie: {}", timezone);
                return Optional.of(timezone);
            }
        }
        log.info("No timezone found in cookies");
        return Optional.empty();
    }
    public static void setLastTimezone(HttpServletResponse resp, String timezone) {
        jakarta.servlet.http.Cookie cookie = new jakarta.servlet.http.Cookie(COOKIE_NAME, timezone);
        int oneDayInSeconds = 24 * 60 * 60;
        cookie.setMaxAge(oneDayInSeconds);
        resp.addCookie(cookie);
        log.info("Set timezone in cookie: {}", timezone);
    }
}
