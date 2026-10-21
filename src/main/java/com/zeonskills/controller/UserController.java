package com.zeonskills.controller;

import com.zeonskills.config.FlashMessage;
import com.zeonskills.config.SessionKeys;
import com.zeonskills.entity.Skill;
import com.zeonskills.service.QuizService;
import com.zeonskills.service.SkillService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.HashMap;
import java.util.Map;

@Controller
@RequiredArgsConstructor
public class UserController {

    private final SkillService skillService;
    private final QuizService quizService;

    /** Mirrors: @app.route('/home') */
    @GetMapping("/home")
    public String home(HttpSession session, Model model) {
        if (session.getAttribute(SessionKeys.USERNAME) == null
                || !"user".equals(session.getAttribute(SessionKeys.ROLE))) {
            return "redirect:/";
        }
        model.addAttribute("header", true);
        model.addAttribute("footer", true);
        return "home";
    }

    /** Mirrors: @app.route('/popularquiz') */
    @GetMapping("/popularquiz")
    public String popularQuiz(Model model) {
        Map<String, java.util.List<Skill>> skillsByCategory = skillService.groupByCategory();
        model.addAttribute("skillsByCategory", skillsByCategory);
        model.addAttribute("categories", skillsByCategory.keySet());
        model.addAttribute("header", true);
        model.addAttribute("footer", true);
        return "popularquiz";
    }

    /** Mirrors: @app.route('/testqn/<int:skill_id>') */
    @GetMapping("/testqn/{skillId}")
    public String testQuestions(@PathVariable Long skillId, HttpSession session, Model model, RedirectAttributes redirect) {
        if (session.getAttribute(SessionKeys.USERNAME) == null) {
            FlashMessage.addRedirect(redirect, "Please log in to start a quiz.", "warning");
            return "redirect:/";
        }
        Skill skill = skillService.getOrThrow(skillId);
        if (skill.getQuestions().isEmpty()) {
            FlashMessage.addRedirect(redirect, "This skill has no questions yet.", "warning");
            return "redirect:/popularquiz";
        }
        model.addAttribute("skill", skill);
        model.addAttribute("shuffledQuestions", quizService.shuffledQuestions(skill));
        model.addAttribute("footer", true);
        return "testqn";
    }

    /** Mirrors: @app.route('/test/<int:skill_id>', methods=['POST']) */
    @PostMapping("/test/{skillId}")
    public String submitTest(@PathVariable Long skillId, HttpServletRequest request,
                              HttpSession session, Model model, RedirectAttributes redirect) {
        if (session.getAttribute(SessionKeys.USERNAME) == null) {
            FlashMessage.addRedirect(redirect, "Please log in.", "warning");
            return "redirect:/";
        }
        Long userId = (Long) session.getAttribute(SessionKeys.USER_ID);
        Skill skill = skillService.getOrThrow(skillId);

        // Form fields are named "question_<question.id>" -> selected answer id (mirrors request.form.get)
        Map<Long, Long> answersByQuestionId = new HashMap<>();
        for (var question : skill.getQuestions()) {
            String raw = request.getParameter("question_" + question.getId());
            if (raw != null && !raw.isBlank()) {
                answersByQuestionId.put(question.getId(), Long.valueOf(raw));
            }
        }

        QuizService.GradedResult result = quizService.gradeAndSave(userId, skill, answersByQuestionId);

        model.addAttribute("skill", skill);
        model.addAttribute("total", result.total());
        model.addAttribute("score", result.score());
        model.addAttribute("passed", result.passed());
        model.addAttribute("results", result.results());
        return "testresult";
    }

    /** Mirrors: @app.route('/quizview') */
    @GetMapping("/quizview")
    public String quizView(Model model) {
        model.addAttribute("footer", true);
        return "quizview";
    }

    /** Mirrors: @app.route('/testresult') -- no data passed in the original either. */
    @GetMapping("/testresult")
    public String testResult(Model model) {
        model.addAttribute("footer", true);
        return "testresult";
    }

    /** Mirrors: @app.route('/viewresult') -- fully static page in the original. */
    @GetMapping("/viewresult")
    public String viewResult(Model model) {
        model.addAttribute("footer", true);
        return "viewresult";
    }
}
