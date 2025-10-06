package com.frisk.cadettsplitterspadel.services;

import com.frisk.cadettsplitterspadel.dto.BookingRequest;
import com.frisk.cadettsplitterspadel.dto.UpdateBookingRequest;
import com.frisk.cadettsplitterspadel.entities.Booking;
import com.frisk.cadettsplitterspadel.entities.Court;
import com.frisk.cadettsplitterspadel.entities.Customer;
import com.frisk.cadettsplitterspadel.enums.BookingStatus;
import com.frisk.cadettsplitterspadel.exceptions.ResourceNotFoundException;
import com.frisk.cadettsplitterspadel.repositories.BookingRepository;
import com.frisk.cadettsplitterspadel.repositories.CourtRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.*;
import java.util.List;
import java.util.Objects;

@Service
public class BookingServiceImpl implements BookingService {

    private static final Logger log  = LoggerFactory.getLogger(BookingServiceImpl.class);
    private static final ZoneId ZONE = ZoneId.of("Europe/Stockholm");

    private final BookingRepository bookingRepository;
    private final CourtRepository courtRepository;
    private final CustomerService customerService;

    public BookingServiceImpl(BookingRepository bookingRepository,
                              CourtRepository courtRepository,
                              CustomerService customerService) {
        this.bookingRepository = bookingRepository;
        this.courtRepository   = courtRepository;
        this.customerService   = customerService;
    }

    @Override
    @Transactional(readOnly = true)
    public List<Booking> myBookings(String userId) {
        return bookingRepository.findByCustomerUserId(
                userId,
                Sort.by(Sort.Order.desc("bookingDate"), Sort.Order.desc("startTime"))
        );
    }

    @Override
    @Transactional
    public Booking book(BookingRequest req, String userId) {
        if (req.getCourtId() == null)                      throw new IllegalArgumentException("Court id required");
        if (blank(req.getBookingDate()))                   throw new IllegalArgumentException("Booking date required (YYYY-MM-DD)");
        if (blank(req.getStartTime()))                     throw new IllegalArgumentException("Start time required (HH:mm)");
        if (blank(req.getEndTime()))                       throw new IllegalArgumentException("End time required (HH:mm)");

        Customer customer = customerService.getForUserOrThrow(userId);
        Court court = courtRepository.findByIdAndActiveTrue(req.getCourtId())
                .orElseThrow(() -> new ResourceNotFoundException("Court", req.getCourtId()));

        LocalDate date = parseDate(req.getBookingDate());
        LocalTime start = parseTime(req.getStartTime(), "startTime must be HH:mm");
        LocalTime end   = parseTime(req.getEndTime(),   "endTime must be HH:mm");

        assertFuture(date, start);
        assertEndAfterStart(start, end);
        assertWholeHours(start, end);

        var sameDay = bookingRepository.findByCourtIdAndBookingDateAndStatus(
                court.getId(), date, BookingStatus.ACTIVE,
                Sort.by(Sort.Order.asc("startTime"))
        );
        boolean overlaps = sameDay.stream().anyMatch(b ->
                b.getStartTime().isBefore(end) && b.getEndTime().isAfter(start)
        );
        if (overlaps) throw new IllegalArgumentException("Court already booked at that time");

        int hours = (int) Duration.between(start, end).toHours();
        int price = court.getHourlyRateSek() * hours;

        Booking b = new Booking();
        b.setCourt(court);
        b.setCustomer(customer);
        b.setBookingDate(date);
        b.setStartTime(start);
        b.setEndTime(end);
        b.setNumberOfPlayers(req.getNumberOfPlayers() != null ? req.getNumberOfPlayers() : 2);
        b.setPriceSek(price);
        b.setStatus(BookingStatus.ACTIVE);

        Booking saved = bookingRepository.save(b);
        log.info("user={} booked {}h court={} on {} {}-{} (price={} SEK)",
                userId, hours, court.getCourtNumber(), date, start, end, price);
        return saved;

    }

    @Override
    @Transactional
    public Booking update(UpdateBookingRequest req, String userId, boolean isAdmin) {
        if (req.getBookingId() == null)                    throw new IllegalArgumentException("bookingId is required");
        if (blank(req.getBookingDate()))                   throw new IllegalArgumentException("bookingDate is required (YYYY-MM-DD)");
        if (blank(req.getStartTime()))                     throw new IllegalArgumentException("startTime is required (HH:mm)");
        if (blank(req.getEndTime()))                       throw new IllegalArgumentException("endTime is required (HH:mm)");

        Booking b = bookingRepository.findById(req.getBookingId())
                .orElseThrow(() -> new ResourceNotFoundException("Booking", req.getBookingId()));

        if (!isAdmin && !Objects.equals(b.getCustomer().getUserId(), userId)) {
            throw new ResourceNotFoundException("Booking", req.getBookingId());
        }
        if (b.getStatus() == BookingStatus.CANCELED) {
            throw new IllegalArgumentException("Booking is canceled");
        }
        if (!courtRepository.existsByIdAndActiveTrue(b.getCourt().getId())) {
            throw new IllegalArgumentException("Court is inactive or does not exist");
        }

        LocalDate date = parseDate(req.getBookingDate());
        LocalTime start = parseTime(req.getStartTime(), "startTime must be HH:mm");
        LocalTime end   = parseTime(req.getEndTime(),   "endTime must be HH:mm");

        assertFuture(date, start);
        assertEndAfterStart(start, end);
        assertWholeHours(start, end);

        var sameDay = bookingRepository.findByCourtIdAndBookingDateAndStatus(
                b.getCourt().getId(), date, BookingStatus.ACTIVE,
                Sort.by(Sort.Order.asc("startTime"))
        );
        boolean overlaps = sameDay.stream().anyMatch(other ->
                !Objects.equals(other.getBookingId(), b.getBookingId()) &&
                        other.getStartTime().isBefore(end) &&
                        other.getEndTime().isAfter(start)
        );
        if (overlaps) throw new IllegalArgumentException("Court is already booked in that interval");

        int hours = (int) Duration.between(start, end).toHours();
        int price = b.getCourt().getHourlyRateSek() * hours;

        b.setBookingDate(date);
        b.setStartTime(start);
        b.setEndTime(end);
        b.setPriceSek(price);

        Booking saved = bookingRepository.save(b);
        log.info("user={} updated booking id={} to {} {}-{} (price={} SEK)",
                userId, b.getBookingId(), date, start, end, price);
        return saved;
    }

    @Override
    @Transactional
    public void cancel(Integer bookingId, String userId, boolean isAdmin) {
        if (bookingId == null) throw new IllegalArgumentException("bookingId is required");

        Booking b = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new ResourceNotFoundException("Booking", bookingId));

        if (!isAdmin && !Objects.equals(b.getCustomer().getUserId(), userId)) {
            throw new ResourceNotFoundException("Booking", bookingId);
        }
        if (b.getStatus() == BookingStatus.CANCELED) {
            log.info("user={} cancel booking id={} (already canceled)", userId, bookingId);
            return;
        }

        LocalDate today = LocalDate.now(ZONE);
        LocalDate limit = b.getBookingDate().minusDays(7);
        if (today.isAfter(limit)) {
            throw new IllegalArgumentException("Booking cannot be canceled within 7 days of start date");
        }

        b.setStatus(BookingStatus.CANCELED);
        bookingRepository.save(b);
        log.info("user={} canceled booking id={} (date={}, {}-{})",
                userId, bookingId, b.getBookingDate(), b.getStartTime(), b.getEndTime());
    }

    @Override
    @Transactional(readOnly = true)
    public boolean isAvailable(Integer courtId, LocalDate date) {
        return !bookingRepository.existsByCourtIdAndBookingDateAndStatus(
                courtId, date, BookingStatus.ACTIVE);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Booking> listCanceled() {
        return bookingRepository.findByStatus(
                BookingStatus.CANCELED,
                Sort.by(Sort.Order.desc("bookingDate"), Sort.Order.desc("startTime"))
        );
    }

    @Override
    @Transactional(readOnly = true)
    public List<Booking> listUpcoming() {
        List<Booking> active = bookingRepository.findByStatus(
                BookingStatus.ACTIVE,
                Sort.by(Sort.Order.asc("bookingDate"), Sort.Order.asc("startTime"))
        );
        LocalDate today = LocalDate.now(ZONE);
        LocalTime now   = LocalTime.now(ZONE);

        return active.stream()
                .filter(b -> b.getBookingDate().isAfter(today)
                        || (b.getBookingDate().isEqual(today) && !b.getEndTime().isBefore(now)))
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<Booking> listPast() {
        List<Booking> active = bookingRepository.findByStatus(
                BookingStatus.ACTIVE,
                Sort.by(Sort.Order.desc("bookingDate"), Sort.Order.desc("startTime"))
        );
        LocalDate today = LocalDate.now(ZONE);
        LocalTime now   = LocalTime.now(ZONE);

        return active.stream()
                .filter(b -> b.getBookingDate().isBefore(today)
                        || (b.getBookingDate().isEqual(today) && b.getEndTime().isBefore(now)))
                .toList();
    }

    private static boolean blank(String s) {
        return s == null || s.isBlank();
    }

    private static LocalDate parseDate(String s) {
        try { return LocalDate.parse(s); }
        catch (Exception e) { throw new IllegalArgumentException("bookingDate must be YYYY-MM-DD"); }
    }

    private static LocalTime parseTime(String s, String msg) {
        try { return LocalTime.parse(s); }
        catch (Exception e) { throw new IllegalArgumentException(msg); }
    }

    private static void assertFuture(LocalDate date, LocalTime start) {
        LocalDate today = LocalDate.now(ZONE);
        LocalTime now   = LocalTime.now(ZONE);
        if (date.isBefore(today) || (date.isEqual(today) && !start.isAfter(now))) {
            throw new IllegalArgumentException("Start time must be in the future");
        }
    }

    private static void assertEndAfterStart(LocalTime start, LocalTime end) {
        if (!end.isAfter(start)) {
            throw new IllegalArgumentException("endTime must be after startTime");
        }
    }

    private static void assertWholeHours(LocalTime start, LocalTime end) {
        if (start.getMinute() != 0 || end.getMinute() != 0) {
            throw new IllegalArgumentException("Times must be whole hours");
        }
    }
}