package com.zeonskills.config;

import org.springframework.ui.Model;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.ArrayList;
import java.util.List;

/**
 * Stand-in for Flask's flash(message, category) / get_flashed_messages().
 * Every template's flash-message block iterates the "flashMessages" model
 * attribute the same way the originals iterate get_flashed_messages().
 */
public record FlashMessage(String message, String category) {

    @SuppressWarnings("unchecked")
    public static void add(Model model, String message, String category) {
        List<FlashMessage> messages = (List<FlashMessage>) model.asMap().get("flashMessages");
        if (messages == null) {
            messages = new ArrayList<>();
            model.addAttribute("flashMessages", messages);
        }
        messages.add(new FlashMessage(message, category));
    }

    public static void addRedirect(RedirectAttributes redirect, String message, String category) {
        List<FlashMessage> messages = new ArrayList<>();
        messages.add(new FlashMessage(message, category));
        redirect.addFlashAttribute("flashMessages", messages);
    }
}
