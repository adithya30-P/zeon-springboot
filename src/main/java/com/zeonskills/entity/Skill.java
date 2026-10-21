package com.zeonskills.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents a skill (quiz topic).
 * Mirrors the original Flask Skill model:
 *   id, name, description (nullable), category, level, questions (1-to-many)
 */
@Entity
@Table(name = "skill")
@Getter
@Setter
@NoArgsConstructor
public class Skill {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(nullable = false, length = 50)
    private String category;

    @Column(nullable = false, length = 50)
    private String level;

    // backref='skill' in Flask -> mappedBy "skill" here.
    // cascade="all, delete-orphan" in Flask -> CascadeType.ALL + orphanRemoval here.
    @OneToMany(mappedBy = "skill", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnore
    private List<Question> questions = new ArrayList<>();
}
