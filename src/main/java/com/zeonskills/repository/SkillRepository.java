package com.zeonskills.repository;

import com.zeonskills.entity.Skill;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SkillRepository extends JpaRepository<Skill, Long> {
    // Mirrors: Skill.query.all()
}
