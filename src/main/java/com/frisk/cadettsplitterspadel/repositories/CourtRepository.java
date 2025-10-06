package com.frisk.cadettsplitterspadel.repositories;

import com.frisk.cadettsplitterspadel.entities.Court;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CourtRepository extends JpaRepository<Court, Integer> {

    List<Court> findAllByActiveTrue();
    Optional<Court> findByIdAndActiveTrue(Integer id);
    boolean existsByIdAndActiveTrue(Integer id);

    Optional<Court> findByCourtNumber(int courtNumber);
    boolean existsByCourtNumber(int courtNumber);
}