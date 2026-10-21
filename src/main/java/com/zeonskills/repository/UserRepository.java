package com.zeonskills.repository;

import com.zeonskills.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    // Mirrors: User.query.filter_by(email=email, password=password).first()
    Optional<User> findByEmailAndPassword(String email, String password);

    // Mirrors: User.query.filter_by(email=email).first()
    Optional<User> findByEmail(String email);
}
