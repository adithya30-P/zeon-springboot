package com.zeonskills.service;

import com.zeonskills.entity.Answer;
import com.zeonskills.entity.Question;
import com.zeonskills.entity.Skill;
import com.zeonskills.repository.QuestionRepository;
import com.zeonskills.repository.SkillRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Transactional
public class SkillService {

    private final SkillRepository skillRepository;
    private final QuestionRepository questionRepository;

    /** Mirrors: Skill.query.all() */
    public List<Skill> findAll() {
        return skillRepository.findAll();
    }

    /** Mirrors: Skill.query.get_or_404(skill_id) */
    public Skill getOrThrow(Long skillId) {
        return skillRepository.findById(skillId)
                .orElseThrow(() -> new EntityNotFoundException("Skill not found: " + skillId));
    }

    public Question getQuestionOrThrow(Long questionId) {
        return questionRepository.findById(questionId)
                .orElseThrow(() -> new EntityNotFoundException("Question not found: " + questionId));
    }

    /**
     * Mirrors popularquiz(): groups all skills by category, preserving
     * first-seen category order (Flask dict preserves insertion order too).
     */
    public Map<String, List<Skill>> groupByCategory() {
        Map<String, List<Skill>> grouped = new LinkedHashMap<>();
        for (Skill skill : skillRepository.findAll()) {
            grouped.computeIfAbsent(skill.getCategory(), k -> new java.util.ArrayList<>()).add(skill);
        }
        return grouped;
    }

    /** Mirrors admin_createskill(): create skill + its questions + answers in one go. */
    public Skill createSkillWithQuestions(String name, String description, String category, String level,
                                           List<NewQuestion> questions) {
        Skill skill = new Skill();
        skill.setName(name);
        skill.setDescription(description);
        skill.setCategory(category);
        skill.setLevel(level);
        skill = skillRepository.save(skill);

        for (NewQuestion nq : questions) {
            if (nq.text() == null || nq.text().isBlank()) continue;
            Question question = new Question();
            question.setQuestionText(nq.text());
            question.setSkill(skill);

            for (int i = 0; i < nq.answers().size(); i++) {
                String ansText = nq.answers().get(i);
                if (ansText == null || ansText.isBlank()) continue;
                Answer answer = new Answer();
                answer.setAnswerText(ansText.trim());
                answer.setCorrect(String.valueOf(i).equals(nq.correctIndex()));
                answer.setQuestion(question);
                question.getAnswers().add(answer);
            }
            skill.getQuestions().add(question);
        }
        return skillRepository.save(skill);
    }

    /** Mirrors admin_editskill() POST branch. */
    public Skill updateSkillDetails(Long skillId, String name, String description, String category, String level) {
        Skill skill = getOrThrow(skillId);
        skill.setName(name);
        skill.setDescription(description);
        skill.setCategory(category);
        skill.setLevel(level);
        return skillRepository.save(skill);
    }

    /** Mirrors admin_add_question(): only added if exactly 4 answers + a correct index given. */
    public boolean addQuestion(Long skillId, String questionText, List<String> answers, String correctIndex) {
        if (questionText == null || questionText.isBlank() || answers.size() != 4 || correctIndex == null) {
            return false;
        }
        Skill skill = getOrThrow(skillId);
        Question question = new Question();
        question.setQuestionText(questionText);
        question.setSkill(skill);
        for (int i = 0; i < answers.size(); i++) {
            Answer answer = new Answer();
            answer.setAnswerText(answers.get(i).trim());
            answer.setCorrect(String.valueOf(i).equals(correctIndex));
            answer.setQuestion(question);
            question.getAnswers().add(answer);
        }
        skill.getQuestions().add(question);
        skillRepository.save(skill);
        return true;
    }

    /**
     * Mirrors admin_update_question(): updates question text and every
     * answer's text, plus which single answer is correct.
     * Returns false (mirrors a flash "danger") if any text is blank.
     */
    public boolean updateQuestion(Long questionId, String questionText, Map<Long, String> answerTexts, Long correctAnswerId) {
        Question question = getQuestionOrThrow(questionId);
        if (questionText == null || questionText.isBlank()) {
            return false;
        }
        question.setQuestionText(questionText.trim());

        for (Answer answer : question.getAnswers()) {
            String text = answerTexts.get(answer.getId());
            if (text == null || text.isBlank()) {
                return false;
            }
            answer.setAnswerText(text.trim());
            answer.setCorrect(answer.getId().equals(correctAnswerId));
        }
        questionRepository.save(question);
        return true;
    }

    /** Mirrors admin_delete_question(): returns the skillId so the controller can redirect back. */
    public Long deleteQuestion(Long questionId) {
        Question question = getQuestionOrThrow(questionId);
        Long skillId = question.getSkill().getId();
        questionRepository.delete(question);
        return skillId;
    }

    /** Mirrors admin_deleteskill(): cascade removes its questions/answers. Returns the deleted skill's name. */
    public String deleteSkill(Long skillId) {
        Skill skill = getOrThrow(skillId);
        String name = skill.getName();
        skillRepository.delete(skill);
        return name;
    }

    /** Simple holder for a question being created via admin_createskill's dynamic form. */
    public record NewQuestion(String text, String correctIndex, List<String> answers) {}
}
