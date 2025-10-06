package com.frisk.cadettsplitterspadel.repositories;

import com.frisk.cadettsplitterspadel.entities.Booking;
import com.frisk.cadettsplitterspadel.enums.BookingStatus;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

public interface BookingRepository extends JpaRepository<Booking, Integer> {

    List<Booking> findByCustomerUserId(String userId, Sort sort);

    List<Booking> findByStatus(BookingStatus status, Sort sort);

    List<Booking> findByCourtIdAndBookingDateAndStatus(Integer courtId, LocalDate date, BookingStatus status, Sort sort);

    boolean existsByCourtIdAndBookingDateAndStatus(Integer courtId, LocalDate date, BookingStatus status);
}