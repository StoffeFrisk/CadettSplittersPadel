package com.frisk.cadettsplitterspadel.services;

import com.frisk.cadettsplitterspadel.entities.Court;
import com.frisk.cadettsplitterspadel.exceptions.ResourceNotFoundException;
import com.frisk.cadettsplitterspadel.repositories.CourtRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class CourtServiceImpl implements CourtService {

    private static final Logger audit = LoggerFactory.getLogger("com.frisk.audit");
    private final CourtRepository courtRepository;

    public CourtServiceImpl(CourtRepository courtRepository) {
        this.courtRepository = courtRepository;
    }

    @Override
    public Court add(Court court) {
        if (court.getCourtNumber() <= 0)
            throw new IllegalArgumentException("courtNumber must be > 0");
        if (court.getHourlyRateSek() <= 0)
            throw new IllegalArgumentException("hourlyRateSek must be > 0");
        if (courtRepository.existsByCourtNumber(court.getCourtNumber()))
            throw new IllegalArgumentException("court_number already exists");

        court.setActive(true);
        Court saved = courtRepository.save(court);
        audit.info("SAVE by={} entity=Court id={}", currentUserName(), saved.getId());
        return saved;
    }

    @Override
    public Court update(Court incoming) {
        Integer id = incoming.getId();
        if (id == null) throw new IllegalArgumentException("Court id is required");

        Court existing = courtRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Court", id));
        if (!existing.isActive()) throw new IllegalArgumentException("Court is deleted/inactive");

        if (incoming.getCourtNumber() <= 0)
            throw new IllegalArgumentException("courtNumber must be > 0");
        if (incoming.getHourlyRateSek() <= 0)
            throw new IllegalArgumentException("hourlyRateSek must be > 0");
        if (incoming.getCourtNumber() != existing.getCourtNumber()
                && courtRepository.existsByCourtNumber(incoming.getCourtNumber()))
            throw new IllegalArgumentException("court_number already exists");

        existing.setCourtNumber(incoming.getCourtNumber());
        existing.setHourlyRateSek(incoming.getHourlyRateSek());
        return courtRepository.save(existing);
    }

    @Override
    public void delete(Integer id) {
        if (id == null) throw new IllegalArgumentException("courtId required");
        Court court = courtRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Court", id));
        if (!court.isActive()) return;

        court.setActive(false);
        courtRepository.save(court);
        audit.info("DELETE by={} entity=Court id={}", currentUserName(), court.getId());
    }

    private String currentUserName() {
        var auth = SecurityContextHolder.getContext().getAuthentication();
        return (auth != null && auth.isAuthenticated() && auth.getName() != null)
                ? auth.getName() : "anonymous";
    }
}
