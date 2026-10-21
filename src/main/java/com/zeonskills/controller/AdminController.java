package com.zeonskills.controller;

import com.zeonskills.config.FlashMessage;
import com.zeonskills.config.SessionKeys;
import com.zeonskills.entity.Skill;
import com.zeonskills.entity.UserScore;
import com.zeonskills.repository.UserScoreRepository;
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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequiredArgsConstructor
public class AdminController {

    private final SkillService skillService;
    private final UserScoreRepository userScoreRepository;

    /** All admin_* routes start with the same guard in the original. */
    private boolean isAdmin(HttpSession session) {
        return "admin".equals(session.getAttribute(SessionKeys.ROLE));
    }

    /**
     * Mirrors: @app.route('/admin_dashboard')
     *
     * NOTE (flagged): the original route only ever passes username, footer,
     * sidebar, header to this template - it never computes total_categories,
     * total_skills, attempted_skills, total_users or recent_skills, even
     * though admin_dashboard.html references them. In the original app
     * those simply render blank (Jinja treats them as undefined -> falsy).
     * This controller reproduces that behavior exactly rather than
     * inventing the missing stats. Say the word and I'll wire up real
     * counts (skillRepository.count(), userRepository.count(), etc.).
     */
    @GetMapping("/admin_dashboard")
    public String dashboard(HttpSession session, Model model) {
        if (!isAdmin(session)) return "redirect:/";
        model.addAttribute("username", session.getAttribute(SessionKeys.USERNAME));
        model.addAttribute("footer", true);
        model.addAttribute("sidebar", true);
        model.addAttribute("header", true);
        return "admin_dashboard";
    }

    /** Mirrors: @app.route('/admin_createskill', methods=['GET', 'POST']) */
    @GetMapping("/admin_createskill")
    public String createSkillPage(HttpSession session, Model model) {
        if (!isAdmin(session)) return "redirect:/";
        model.addAttribute("footer", true);
        model.addAttribute("sidebar", true);
        model.addAttribute("header", true);
        return "admin_createskill";
    }

    @PostMapping("/admin_createskill")
    public String createSkill(HttpServletRequest request, HttpSession session, RedirectAttributes redirect) {
        if (!isAdmin(session)) return "redirect:/";

        String skillName = request.getParameter("skill_name");
        String description = request.getParameter("description");
        String category = request.getParameter("category");
        String level = request.getParameter("level");

        // Mirrors the Flask while-loop over questions[<idx>][text] / [correct_answer] / [answers][]
        List<SkillService.NewQuestion> questions = new ArrayList<>();
        int qIdx = 0;
        while (request.getParameter("questions[" + qIdx + "][text]") != null) {
            String qText = request.getParameter("questions[" + qIdx + "][text]");
            String correctIdx = request.getParameter("questions[" + qIdx + "][correct_answer]");
            String[] answers = request.getParameterValues("questions[" + qIdx + "][answers][]");
            questions.add(new SkillService.NewQuestion(
                    qText, correctIdx, answers != null ? List.of(answers) : List.of()));
            qIdx++;
        }

        skillService.createSkillWithQuestions(skillName, description, category, level, questions);
        FlashMessage.addRedirect(redirect, "New skill with questions added successfully!", "success");
        return "redirect:/admin_allskill";
    }

    /** Mirrors: @app.route('/admin_editskill/<int:skill_id>', methods=['GET', 'POST']) */
    @GetMapping("/admin_editskill/{skillId}")
    public String editSkillPage(@PathVariable Long skillId, HttpSession session, Model model) {
        if (!isAdmin(session)) return "redirect:/";
        model.addAttribute("skill", skillService.getOrThrow(skillId));
        model.addAttribute("footer", true);
        model.addAttribute("sidebar", true);
        model.addAttribute("header", true);
        return "admin_editskill";
    }

    @PostMapping("/admin_editskill/{skillId}")
    public String editSkill(@PathVariable Long skillId, HttpServletRequest request,
                             HttpSession session, RedirectAttributes redirect) {
        if (!isAdmin(session)) return "redirect:/";
        skillService.updateSkillDetails(skillId,
                request.getParameter("skill_name"),
                request.getParameter("description"),
                request.getParameter("category"),
                request.getParameter("level"));
        FlashMessage.addRedirect(redirect, "Skill details updated successfully!", "success");
        return "redirect:/admin_editskill/" + skillId;
    }

    /** Mirrors: @app.route('/admin_add_question/<int:skill_id>', methods=['POST']) */
    @PostMapping("/admin_add_question/{skillId}")
    public String addQuestion(@PathVariable Long skillId, HttpServletRequest request,
                               HttpSession session, RedirectAttributes redirect) {
        if (!isAdmin(session)) return "redirect:/";
        String questionText = request.getParameter("new_question_text");
        String[] answers = request.getParameterValues("new_answers[]");
        String correctIndex = request.getParameter("new_correct_answer");

        boolean added = skillService.addQuestion(skillId, questionText,
                answers != null ? List.of(answers) : List.of(), correctIndex);

        if (added) {
            FlashMessage.addRedirect(redirect, "New question added successfully!", "success");
        } else {
            FlashMessage.addRedirect(redirect, "Failed to add question. Fill all fields.", "danger");
        }
        return "redirect:/admin_editskill/" + skillId;
    }

    /** Mirrors: @app.route('/admin_update_question/<int:question_id>', methods=['POST']) */
    @PostMapping("/admin_update_question/{questionId}")
    public String updateQuestion(@PathVariable Long questionId, HttpServletRequest request,
                                  RedirectAttributes redirect) {
        var question = skillService.getQuestionOrThrow(questionId);
        Long skillId = question.getSkill().getId();

        String questionText = request.getParameter("question_text");
        if (questionText == null || questionText.isBlank()) {
            FlashMessage.addRedirect(redirect, "Question text cannot be empty!", "danger");
            return "redirect:/admin_editskill/" + skillId;
        }

        Map<Long, String> answerTexts = new HashMap<>();
        for (var answer : question.getAnswers()) {
            answerTexts.put(answer.getId(), request.getParameter("answer_text_" + answer.getId()));
        }
        String correctAnswerParam = request.getParameter("correct_answer");
        Long correctAnswerId = correctAnswerParam != null ? Long.valueOf(correctAnswerParam) : null;

        boolean ok = skillService.updateQuestion(questionId, questionText, answerTexts, correctAnswerId);
        if (!ok) {
            FlashMessage.addRedirect(redirect, "Answer text cannot be empty!", "danger");
            return "redirect:/admin_editskill/" + skillId;
        }
        FlashMessage.addRedirect(redirect, "Question updated successfully!", "success");
        return "redirect:/admin_editskill/" + skillId;
    }

    /** Mirrors: @app.route('/admin_delete_question/<int:question_id>', methods=['POST']) */
    @PostMapping("/admin_delete_question/{questionId}")
    public String deleteQuestion(@PathVariable Long questionId, HttpSession session, RedirectAttributes redirect) {
        if (!isAdmin(session)) return "redirect:/";
        Long skillId = skillService.deleteQuestion(questionId);
        FlashMessage.addRedirect(redirect, "Question deleted successfully!", "success");
        return "redirect:/admin_editskill/" + skillId;
    }

    /** Mirrors: @app.route('/admin_deleteskill/<int:skill_id>', methods=['POST']) */
    @PostMapping("/admin_deleteskill/{skillId}")
    public String deleteSkill(@PathVariable Long skillId, HttpSession session, RedirectAttributes redirect) {
        if (!isAdmin(session)) return "redirect:/";
        String name = skillService.deleteSkill(skillId);
        FlashMessage.addRedirect(redirect, "Skill \"" + name + "\" deleted.", "success");
        return "redirect:/admin_allskill";
    }

    /** Mirrors: @app.route('/admin_categories') -- fully static page in the original. */
    @GetMapping("/admin_categories")
    public String categories(Model model) {
        model.addAttribute("footer", true);
        model.addAttribute("sidebar", true);
        model.addAttribute("header", true);
        return "admin_categories";
    }

    /** Mirrors: @app.route('/admin_allskill') */
    @GetMapping("/admin_allskill")
    public String allSkills(HttpSession session, Model model) {
        if (!isAdmin(session)) return "redirect:/";
        List<Skill> skills = skillService.findAll();
        model.addAttribute("skills", skills);
        model.addAttribute("footer", true);
        model.addAttribute("sidebar", true);
        model.addAttribute("header", true);
        return "admin_allskill";
    }

    /** Mirrors: @app.route('/admin_viewresults') -- fully static page in the original. */
    @GetMapping("/admin_viewresults")
    public String viewResults(Model model) {
        model.addAttribute("footer", true);
        model.addAttribute("sidebar", true);
        model.addAttribute("header", true);
        return "admin_viewresults";
    }

    /** Mirrors: @app.route('/admin_skillresults') */
    @GetMapping("/admin_skillresults")
    public String skillResults(HttpSession session, Model model) {
        if (!isAdmin(session)) return "redirect:/";
        List<UserScore> userScores = userScoreRepository.findAllByOrderByPercentageDesc();
        model.addAttribute("userScores", userScores);
        model.addAttribute("footer", true);
        model.addAttribute("sidebar", true);
        model.addAttribute("header", true);
        return "admin_skillresults";
    }
}
