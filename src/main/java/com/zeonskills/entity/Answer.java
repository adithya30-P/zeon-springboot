package com.zeonskills.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Stores possible answers for each question.
 * Mirrors the original Flask Answer model:
 *   id, answer_text, is_correct (default false), question_id (FK)
 */
@Entity
@Table(name = "answer")
@Getter
@Setter
@NoArgsConstructor
public class Answer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "answer_text", nullable = false, length = 200)
    private String answerText;

    @Column(name = "is_correct", nullable = false)
    private boolean isCorrect = false;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "question_id", nullable = false)
    @JsonIgnore
    private Question question;
}
