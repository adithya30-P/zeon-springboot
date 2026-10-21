package com.zeonskills.service;

import com.zeonskills.entity.*;
import com.zeonskills.repository.AnswerRepository;
import com.zeonskills.repository.UserAnswerRepository;
import com.zeonskills.repository.UserRepository;
import com.zeonskills.repository.UserScoreRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class QuizService {

    private final AnswerRepository answerRepository;
    private final UserAnswerRepository userAnswerRepository;
    private final UserScoreRepository userScoreRepository;
    private final UserRepository userRepository;

    /** Mirrors testqn(): shuffled_questions = list(skill.questions); random.shuffle(...) */
    public List<Question> shuffledQuestions(Skill skill) {
        List<Question> shuffled = new ArrayList<>(skill.getQuestions());
        Collections.shuffle(shuffled);
        return shuffled;
    }

    /** One row of the results table shown on testresult.html. */
    public record QuestionResult(Long questionId, String question, String userAnswer,
                                  String correctAnswer, boolean isCorrect) {}

    public record GradedResult(int score, int total, boolean passed, List<QuestionResult> results) {}

    /**
     * Mirrors the /test/<skill_id> POST route exactly:
     *  - for each question, read the submitted answer id from "question_<id>"
     *  - save a UserAnswer row if one was selected
     *  - compare against the correct answer, tally score
     *  - delete any existing UserScore for this user and insert a fresh one
     *  - pass/fail threshold is score >= 50% of total
     *
     * answersByQuestionId: map of questionId -> submitted answerId (nullable/absent = not answered)
     */
    @Transactional
    public GradedResult gradeAndSave(Long userId, Skill skill, java.util.Map<Long, Long> answersByQuestionId) {
        List<QuestionResult> results = new ArrayList<>();
        int score = 0;
        int total = skill.getQuestions().size();

        for (Question question : skill.getQuestions()) {
            Long ansId = answersByQuestionId.get(question.getId());
            Answer selected = ansId != null ? answerRepository.findById(ansId).orElse(null) : null;

            if (ansId != null) {
                userAnswerRepository.save(new UserAnswer(userId, question.getId(), ansId));
            }

            String userAnswerText = selected != null ? selected.getAnswerText() : "Not Answered";

            String correctAnswerText = null;
            for (Answer a : question.getAnswers()) {
                if (a.isCorrect()) {
                    correctAnswerText = a.getAnswerText();
                    break;
                }
            }

            boolean isCorrect = selected != null && selected.isCorrect();
            if (isCorrect) score++;

            results.add(new QuestionResult(question.getId(), question.getQuestionText(),
                    userAnswerText, correctAnswerText, isCorrect));
        }

        double percentage = total > 0 ? (score / (double) total) * 100 : 0;

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User not found: " + userId));

        Optional<UserScore> existing = userScoreRepository.findFirstByUserId(userId);
        existing.ifPresent(userScoreRepository::delete);

        UserScore userScore = new UserScore();
        userScore.setUserId(userId);
        userScore.setUserName(user.getName());
        userScore.setUserEmail(user.getEmail());
        userScore.setScore(score);
        userScore.setTotal(total);
        userScore.setPercentage(percentage);
        userScoreRepository.save(userScore);

        boolean passed = total > 0 && score >= (total * 0.5);

        return new GradedResult(score, total, passed, results);
    }
}
