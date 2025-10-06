package com.frisk.cadettsplitterspadel.services;

import com.frisk.cadettsplitterspadel.dto.BookingRequest;
import com.frisk.cadettsplitterspadel.dto.UpdateBookingRequest;
import com.frisk.cadettsplitterspadel.entities.Booking;

import java.time.LocalDate;
import java.util.List;

public interface BookingService {
    List<Booking> myBookings(String userId);
    Booking book(BookingRequest req, String userId);
    Booking update(UpdateBookingRequest req, String userId, boolean isAdmin);
    void    cancel(Integer bookingId, String userId, boolean isAdmin);

    boolean isAvailable(Integer courtId, LocalDate date);

    List<Booking> listCanceled();
    List<Booking> listUpcoming();
    List<Booking> listPast();
}