package com.zeonskills.controller;

import com.zeonskills.config.FlashMessage;
import com.zeonskills.config.SessionKeys;
import com.zeonskills.entity.User;
import com.zeonskills.service.AuthService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Optional;

@Controller
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    /** Mirrors: @app.route('/', methods=['GET', 'POST']) def login() */
    @GetMapping("/")
    public String loginPage(HttpSession session) {
        if (session.getAttribute(SessionKeys.USERNAME) != null) {
            return "admin".equals(session.getAttribute(SessionKeys.ROLE))
                    ? "redirect:/admin_dashboard" : "redirect:/home";
        }
        return "login";
    }

    @PostMapping("/")
    public String login(@RequestParam("username") String email,
                         @RequestParam("password") String password,
                         HttpSession session,
                         RedirectAttributes redirect,
                         Model model) {
        Optional<User> userOpt = authService.authenticate(email, password);
        if (userOpt.isPresent()) {
            User user = userOpt.get();
            session.setAttribute(SessionKeys.USERNAME, user.getName());
            session.setAttribute(SessionKeys.EMAIL, user.getEmail());
            session.setAttribute(SessionKeys.ROLE, user.getRole());
            session.setAttribute(SessionKeys.USER_ID, user.getId());
            FlashMessage.addRedirect(redirect, "Welcome back, " + user.getName() + "!", "success");
            return "admin".equals(user.getRole()) ? "redirect:/admin_dashboard" : "redirect:/home";
        }
        FlashMessage.add(model, "Invalid email or password", "danger");
        return "login";
    }

    /** Mirrors: @app.route('/registration', methods=['GET', 'POST']) */
    @GetMapping("/registration")
    public String registrationPage() {
        return "registration";
    }

    @PostMapping("/registration")
    public String register(@RequestParam String name,
                            @RequestParam String email,
                            @RequestParam String password,
                            RedirectAttributes redirect) {
        // Mirrors: if User.query.filter_by(email=email).first(): flash(...); return redirect(url_for('registration'))
        if (authService.emailAlreadyRegistered(email)) {
            FlashMessage.addRedirect(redirect, "Email already registered", "danger");
            return "redirect:/registration";
        }
        authService.register(name, email, password);
        FlashMessage.addRedirect(redirect, "Successfully Registered!", "success");
        return "redirect:/";
    }

    /** Mirrors: @app.route('/logout') */
    @GetMapping("/logout")
    public String logout(HttpSession session, RedirectAttributes redirect) {
        session.invalidate();
        FlashMessage.addRedirect(redirect, "You have been logged out.", "info");
        return "redirect:/";
    }
}
