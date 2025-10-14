package com.frisk.cadettsplitterspadel.repositories;

import com.frisk.cadettsplitterspadel.entities.Booking;
import com.frisk.cadettsplitterspadel.enums.BookingStatus;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import com.frisk.cadettsplitterspadel.dto.BookingDto;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface BookingRepository extends JpaRepository<Booking, Integer> {


    @Query("""
select new com.frisk.cadettsplitterspadel.dto.BookingDto(
  b.id,
  c.id,
  c.courtNumber,
  b.bookingDate,
  b.startTime,
  b.endTime,
  b.numberOfPlayers,
  b.priceSek,
  b.status
)
from Booking b
join b.court c
join b.customer cu
where cu.userId = :userId
order by b.bookingDate desc, b.startTime desc
""")
    java.util.List<BookingDto> findMyBookingsDto(@Param("userId") String userId);

    List<Booking> findByCustomerUserId(String userId, Sort sort);

    List<Booking> findByStatus(BookingStatus status, Sort sort);

    List<Booking> findByCourtIdAndBookingDateAndStatus(Integer courtId, LocalDate date, BookingStatus status, Sort sort);

    boolean existsByCourtIdAndBookingDateAndStatus(Integer courtId, LocalDate date, BookingStatus status);
}