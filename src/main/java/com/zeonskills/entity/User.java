package com.zeonskills.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Stores registered users (both admin and normal).
 * Mirrors the original Flask/SQLAlchemy User model exactly:
 *   id, name, email (unique), password, role (default 'user')
 *
 * NOTE (flagged, not changed): the original project stores passwords in
 * plain text and compares them directly in the login route. This class
 * mirrors that behavior for fidelity to the source project. Hashing
 * (e.g. BCryptPasswordEncoder) is a recommended follow-up, not applied
 * here because it isn't present in the original and wasn't requested.
 */
@Entity
@Table(name = "user")
@Getter
@Setter
@NoArgsConstructor
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(nullable = false, unique = true, length = 100)
    private String email;

    @Column(nullable = false, length = 100)
    private String password;

    @Column(nullable = false, length = 10)
    private String role = "user"; // 'user' or 'admin'
}
