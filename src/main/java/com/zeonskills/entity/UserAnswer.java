package com.zeonskills.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Tracks which answer a user selected for a question.
 * Mirrors the original Flask UserAnswer model exactly: it stores raw
 * foreign-key ids (user_id, question_id, answer_id) with NO JPA
 * relationship mappings, because the original SQLAlchemy model did not
 * declare relationship() on this table either - only plain db.Column FKs.
 */
@Entity
@Table(name = "user_answer")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UserAnswer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "question_id", nullable = false)
    private Long questionId;

    @Column(name = "answer_id", nullable = false)
    private Long answerId;

    public UserAnswer(Long userId, Long questionId, Long answerId) {
        this.userId = userId;
        this.questionId = questionId;
        this.answerId = answerId;
    }
}
