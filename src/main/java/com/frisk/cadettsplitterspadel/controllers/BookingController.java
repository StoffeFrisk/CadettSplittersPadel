package com.frisk.cadettsplitterspadel.controllers;

import com.frisk.cadettsplitterspadel.dto.BookingRequest;
import com.frisk.cadettsplitterspadel.dto.UpdateBookingRequest;
import com.frisk.cadettsplitterspadel.entities.Booking;
import com.frisk.cadettsplitterspadel.services.BookingService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/wigellpadel")
public class BookingController {

    private final BookingService bookingService;

    public BookingController(BookingService bookingService) {
        this.bookingService = bookingService;
    }

    @GetMapping("/v1/mybookings")
    public ResponseEntity<Map<String, Object>> myBookings(Authentication auth) {
        List<Booking> list = bookingService.myBookings(auth.getName());
        Map<String, Object> body = new LinkedHashMap<>();
        if (list.isEmpty()) body.put("message", "No bookings found");
        body.put("data", list);
        return ResponseEntity.ok(body);
    }

    @PostMapping("/v1/booking/bookcourt")
    public ResponseEntity<Map<String, Object>> bookCourt(@RequestBody BookingRequest req,
                                                         Authentication auth) {
        Booking saved = bookingService.book(req, auth.getName());
        return ResponseEntity.status(HttpStatus.CREATED).body(Map.of("data", saved));
    }

    @PutMapping("/v1/updatebooking")
    public ResponseEntity<Map<String, Object>> updateBooking(@RequestBody UpdateBookingRequest req,
                                                             Authentication auth) {
        boolean isAdmin = auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
        Booking saved = bookingService.update(req, auth.getName(), isAdmin);
        return ResponseEntity.ok(Map.of("data", saved));
    }

    @DeleteMapping("/v1/cancelbooking")
    public ResponseEntity<Map<String, Object>> cancelBooking(@RequestParam Integer id,
                                                             Authentication auth) {
        boolean isAdmin = auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
        bookingService.cancel(id, auth.getName(), isAdmin);
        return ResponseEntity.ok(Map.of("message", "Booking canceled"));
    }

    @GetMapping("/v1/listcanceled")
    public ResponseEntity<Map<String, Object>> listCanceled() {
        List<Booking> list = bookingService.listCanceled();
        Map<String, Object> body = new LinkedHashMap<>();
        if (list.isEmpty()) body.put("message", "No canceled bookings");
        body.put("data", list);
        return ResponseEntity.ok(body);
    }

    @GetMapping("/v1/listupcoming")
    public ResponseEntity<Map<String, Object>> listUpcoming() {
        List<Booking> list = bookingService.listUpcoming();
        Map<String, Object> body = new LinkedHashMap<>();
        if (list.isEmpty()) body.put("message", "No upcoming bookings");
        body.put("data", list);
        return ResponseEntity.ok(body);
    }

    @GetMapping("/v1/listpast")
    public ResponseEntity<Map<String, Object>> listPast() {
        List<Booking> list = bookingService.listPast();
        Map<String, Object> body = new LinkedHashMap<>();
        if (list.isEmpty()) body.put("message", "No past bookings");
        body.put("data", list);
        return ResponseEntity.ok(body);
    }
}
