package com.zeonskills.repository;

import com.zeonskills.entity.UserScore;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UserScoreRepository extends JpaRepository<UserScore, Long> {
    // Mirrors: UserScore.query.filter_by(user_id=user_id).first()
    Optional<UserScore> findFirstByUserId(Long userId);

    // Mirrors: UserScore.query.order_by(UserScore.percentage.desc()).all()
    List<UserScore> findAllByOrderByPercentageDesc();
}
