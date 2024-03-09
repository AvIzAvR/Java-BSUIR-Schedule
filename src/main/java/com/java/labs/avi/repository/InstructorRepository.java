package com.java.labs.avi.repository;

import com.java.labs.avi.model.Instructor;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface InstructorRepository extends JpaRepository<Instructor, Long> {
    Optional<Instructor> findByFullName(String fullName);
}
