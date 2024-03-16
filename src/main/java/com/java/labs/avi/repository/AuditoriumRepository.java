package com.java.labs.avi.repository;

import com.java.labs.avi.model.Auditorium;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AuditoriumRepository extends JpaRepository<Auditorium, Long> {
    Optional<Auditorium> findByNumber(String number);
}
