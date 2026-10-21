package com.zeonskills.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Stores a user's latest quiz score (one row kept per user - the original
 * route deletes any existing UserScore for the user before inserting a
 * new one, so this table only ever holds the most recent attempt).
 * Mirrors the original Flask UserScore model exactly - plain columns,
 * no relationship() mappings, same as the source.
 */
@Entity
@Table(name = "user_score")
@Getter
@Setter
@NoArgsConstructor
public class UserScore {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "user_name", nullable = false, length = 100)
    private String userName;

    @Column(name = "user_email", nullable = false, length = 100)
    private String userEmail;

    @Column(nullable = false)
    private Integer score;

    @Column(nullable = false)
    private Integer total;

    @Column(nullable = false)
    private Double percentage;
}
