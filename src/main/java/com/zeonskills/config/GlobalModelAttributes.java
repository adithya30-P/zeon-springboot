package com.zeonskills.config;

import jakarta.servlet.http.HttpSession;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

/**
 * The original templates read the logged-in user's name directly from the
 * Flask session (e.g. {{ session['username'] }} in home.html, testresult.html).
 * This advice exposes the same value to every Thymeleaf template as
 * "sessionUsername" / "sessionRole", so templates use
 * th:text="${sessionUsername}" instead of a Flask-only session lookup.
 */
@ControllerAdvice
public class GlobalModelAttributes {

    @ModelAttribute("sessionUsername")
    public String sessionUsername(HttpSession session) {
        Object v = session.getAttribute(SessionKeys.USERNAME);
        return v != null ? v.toString() : null;
    }

    @ModelAttribute("sessionRole")
    public String sessionRole(HttpSession session) {
        Object v = session.getAttribute(SessionKeys.ROLE);
        return v != null ? v.toString() : null;
    }
}
