package com.zeonskills.service;

import com.zeonskills.entity.User;
import com.zeonskills.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;

    /** Mirrors: User.query.filter_by(email=email, password=password).first() */
    public Optional<User> authenticate(String email, String password) {
        return userRepository.findByEmailAndPassword(email, password);
    }

    /** Mirrors: User.query.filter_by(email=email).first() */
    public boolean emailAlreadyRegistered(String email) {
        return userRepository.findByEmail(email).isPresent();
    }

    /**
     * Mirrors:
     *   role = 'admin' if email.endswith('@zeon.com') else 'user'
     *   new_user = User(name=name, email=email, password=password, role=role)
     */
    public User register(String name, String email, String password) {
        User user = new User();
        user.setName(name);
        user.setEmail(email);
        user.setPassword(password);
        user.setRole(email.endsWith("@zeon.com") ? "admin" : "user");
        return userRepository.save(user);
    }
}
