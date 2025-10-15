package com.frisk.cadettsplitterspadel.controllers;

import com.frisk.cadettsplitterspadel.entities.Court;
import com.frisk.cadettsplitterspadel.enums.BookingStatus;
import com.frisk.cadettsplitterspadel.exceptions.ResourceNotFoundException;
import com.frisk.cadettsplitterspadel.repositories.BookingRepository;
import com.frisk.cadettsplitterspadel.repositories.CourtRepository;
import com.frisk.cadettsplitterspadel.services.CourtService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/wigellpadel")
public class CourtController {

    private static final Logger log = LoggerFactory.getLogger(CourtController.class);

    private final CourtRepository courtRepository;
    private final BookingRepository bookingRepository;
    private final CourtService courtService;

    public CourtController(CourtRepository courtRepository,
                           BookingRepository bookingRepository,
                           CourtService courtService) {
        this.courtRepository = courtRepository;
        this.bookingRepository = bookingRepository;
        this.courtService = courtService;
    }

    @GetMapping("/listcourts")
    public ResponseEntity<Map<String, Object>> listCourts() {
        List<Court> list = courtRepository.findAllByActiveTrue();
        Map<String, Object> body = new LinkedHashMap<>();
        if (list.isEmpty()) {
            body.put("message", "List is empty");
        }
        body.put("data", list);
        return ResponseEntity.ok(body);
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

    @PostMapping("/v1/addcourt")
    public ResponseEntity<Map<String, Object>> addCourt(@RequestBody Court court) {
        Court saved = courtService.add(court);
        log.info("court created id={} number={} rate={}sek",
                saved.getId(), saved.getCourtNumber(), saved.getHourlyRateSek());
        return ResponseEntity.status(HttpStatus.CREATED).body(Map.of("data", saved));
    }

    @PutMapping("/v1/updatecourt")
    public ResponseEntity<Map<String, Object>> updateCourt(@RequestBody Court incoming) {
        Court saved = courtService.update(incoming);
        log.info("court updated id={} number={} rate={}sek",
                saved.getId(), saved.getCourtNumber(), saved.getHourlyRateSek());
        return ResponseEntity.ok(Map.of("data", saved));
    }

    @DeleteMapping("/v1/remcourt/{id}")
    public ResponseEntity<Map<String, String>> removeCourt(@PathVariable Integer id) {
        courtService.delete(id);
        log.info("court soft-deleted id={}", id);
        return ResponseEntity.ok(Map.of("message", "Court deleted/inactive"));
    }
}
