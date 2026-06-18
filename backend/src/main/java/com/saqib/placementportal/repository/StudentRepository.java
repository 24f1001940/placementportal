package com.saqib.placementportal.repository;

import com.saqib.placementportal.entity.Student;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StudentRepository extends JpaRepository<Student, Long> {
    Optional<Student> findByUserId(Long userId);

    Optional<Student> findByUserEmailIgnoreCase(String email);
}
