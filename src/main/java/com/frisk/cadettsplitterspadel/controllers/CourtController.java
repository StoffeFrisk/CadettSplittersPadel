package com.frisk.cadettsplitterspadel.controllers;

import com.frisk.cadettsplitterspadel.entities.Court;
import com.frisk.cadettsplitterspadel.enums.BookingStatus;
import com.frisk.cadettsplitterspadel.exceptions.ResourceNotFoundException;
import com.frisk.cadettsplitterspadel.repositories.BookingRepository;
import com.frisk.cadettsplitterspadel.repositories.CourtRepository;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/wigellpadel")
public class CourtController {

    private final CourtRepository courtRepository;
    private final BookingRepository bookingRepository;
    private static final Logger log = LoggerFactory.getLogger(CourtController.class);

    public CourtController(CourtRepository courtRepository, BookingRepository bookingRepository) {
        this.courtRepository = courtRepository;
        this.bookingRepository = bookingRepository;
    }

    //User
    @GetMapping("/listcourts")
    public ResponseEntity<Map<String, Object>> listCourts() {
        List<Court> list = courtRepository.findAllByActiveTrue();
        Map<String, Object> courts = new HashMap<>();
        if(list.isEmpty()) {
            courts.put("message", "List is empty");
        }
        courts.put("data", list);
        return ResponseEntity.ok(courts);

    }

    @GetMapping("/checkavailability/{courtId}/{date}")
    public ResponseEntity<Map<String, Object>> checkAvailability(
            @PathVariable Integer courtId,
            @PathVariable String date) {

        courtRepository.findByIdAndActiveTrue(courtId)
                .orElseThrow(() -> new ResourceNotFoundException("Court", courtId));

        LocalDate theDate;
        try {
            theDate = LocalDate.parse(date);
        } catch (Exception e) {
            throw new IllegalArgumentException("date must be YYYY-MM-DD");
        }

        boolean any = bookingRepository.existsByCourtIdAndBookingDateAndStatus(
                courtId, theDate, BookingStatus.ACTIVE);

        boolean available = !any;

        Map<String, Object> data = new LinkedHashMap<>();
        data.put("courtId", courtId);
        data.put("date", theDate.toString());
        data.put("available", available);

        if (!available) {
            var sameDay = bookingRepository.findByCourtIdAndBookingDateAndStatus(
                    courtId, theDate, BookingStatus.ACTIVE,
                    Sort.by(Sort.Order.asc("startTime"))
            );
            var slots = sameDay.stream()
                    .map(b -> Map.of(
                            "startTime", b.getStartTime().toString(),
                            "endTime", b.getEndTime().toString()))
                    .toList();
            data.put("booked", slots);
        }

        return ResponseEntity.ok(Map.of("data", data));
    }

    //Admin
    @PostMapping("/v1/addcourt")
    public ResponseEntity<Map<String, Object>> addCourt(@RequestBody Court court) {
        if (court.getCourtNumber() <= 0) {
            throw new IllegalArgumentException("courtNumber must be > 0");
        }
        if (court.getHourlyRateSek() <= 0) {
            throw new IllegalArgumentException("hourlyRateSek must be > 0");
        }
        if (courtRepository.existsByCourtNumber(court.getCourtNumber())) {
            throw new IllegalArgumentException("court_number already exists");
        }

        court.setActive(true);
        Court saved = courtRepository.save(court);
        log.info("court created id={} number={} rate={}sek",
                saved.getId(), saved.getCourtNumber(), saved.getHourlyRateSek());
        return ResponseEntity.status(HttpStatus.CREATED).body(Map.of("data", saved));
    }

    @PutMapping("/v1/updatecourt")
    public ResponseEntity<Map<String, Object>> updateCourt(@RequestBody Court incoming) {
        Integer id = incoming.getId();
        if (id == null) throw new IllegalArgumentException("Court id is required");

        Court existing = courtRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Court", id));
        if (!existing.isActive()) throw new IllegalArgumentException("Court is deleted/inactive");

        if (incoming.getCourtNumber() <= 0) {
            throw new IllegalArgumentException("courtNumber must be > 0");
        }
        if (incoming.getHourlyRateSek() <= 0) {
            throw new IllegalArgumentException("hourlyRateSek must be > 0");
        }
        if (incoming.getCourtNumber() != existing.getCourtNumber()
                && courtRepository.existsByCourtNumber(incoming.getCourtNumber())) {
            throw new IllegalArgumentException("court_number already exists");
        }

        int oldNumber = existing.getCourtNumber();
        int oldRate   = existing.getHourlyRateSek();

        existing.setCourtNumber(incoming.getCourtNumber());
        existing.setHourlyRateSek(incoming.getHourlyRateSek());

        Court saved = courtRepository.save(existing);
        log.info("court updated id={} number:{}->{} rate:{}->{}",
                saved.getId(), oldNumber, saved.getCourtNumber(), oldRate, saved.getHourlyRateSek());
        return ResponseEntity.ok(Map.of("data", saved));
    }

    @DeleteMapping("/v1/remcourt/{id}")
    public ResponseEntity<Map<String, String>> removeCourt(@PathVariable Integer id) {
        Court court = courtRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Court", id));
        if(!court.isActive()) {
            return ResponseEntity.ok(Map.of("message", "Court is already deleted/inactive"));
        }
        court.setActive(false);
        courtRepository.save(court);
        log.info("court soft-deleted id={} number={}", court.getId(), court.getCourtNumber());
        return ResponseEntity.ok(Map.of("message", "Court deleted/inactive"));
    }
}
