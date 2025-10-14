package com.frisk.cadettsplitterspadel.dto;

import com.frisk.cadettsplitterspadel.enums.BookingStatus;
import java.time.LocalDate;
import java.time.LocalTime;

public record BookingDto(
        Integer bookingId,
        Integer courtId,
        Integer courtNumber,
        LocalDate bookingDate,
        LocalTime startTime,
        LocalTime endTime,
        Integer numberOfPlayers,
        Integer priceSek,
        BookingStatus status
) {}