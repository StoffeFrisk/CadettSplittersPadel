package com.frisk.cadettsplitterspadel.controllers;



import com.frisk.cadettsplitterspadel.entities.Booking;
import com.frisk.cadettsplitterspadel.entities.Court;
import com.frisk.cadettsplitterspadel.enums.BookingStatus;
import com.frisk.cadettsplitterspadel.repositories.BookingRepository;
import com.frisk.cadettsplitterspadel.repositories.CourtRepository;
import com.frisk.cadettsplitterspadel.services.CourtService;
import com.frisk.cadettsplitterspadel.services.CourtServiceImpl;
import com.frisk.cadettsplitterspadel.exceptions.GlobalExceptionHandler;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import org.springframework.http.MediaType;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.data.domain.Sort;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class CourtControllerTest {

    private MockMvc mockMvc;

    @Mock private CourtRepository courtRepository;
    @Mock private BookingRepository bookingRepository;

    private CourtService courtService;
    private CourtController courtController;

    @BeforeEach
    void setUp() {
        courtService = new CourtServiceImpl(courtRepository);
        courtController = new CourtController(courtRepository, bookingRepository, courtService);
        mockMvc = MockMvcBuilders
                .standaloneSetup(courtController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    void listCourts_returnsEmptyListMessage() throws Exception {
        when(courtRepository.findAllByActiveTrue()).thenReturn(List.of());

        mockMvc.perform(get("/api/wigellpadel/listcourts"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("List is empty"))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data.length()").value(0));


        verify(courtRepository).findAllByActiveTrue();
        verifyNoMoreInteractions(courtRepository, bookingRepository);
    }

    @Test
    void addCourt_createsAndReturnCreated() throws Exception {
        String json = """
          { "courtNumber": 4, "hourlyRateSek": 300 }
        """;

        when(courtRepository.existsByCourtNumber(4)).thenReturn(false);

        Court savedCourt = new Court();
        savedCourt.setCourtNumber(4);
        savedCourt.setHourlyRateSek(300);
        savedCourt.setActive(true);
        ReflectionTestUtils.setField(savedCourt, "id", 123);

        when(courtRepository.save(any(Court.class))).thenReturn(savedCourt);
        mockMvc.perform(post("/api/wigellpadel/v1/addcourt")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.id").value(123))
                .andExpect(jsonPath("$.data.courtNumber").value(4))
                .andExpect(jsonPath("$.data.hourlyRateSek").value(300))
                .andExpect(jsonPath("$.data.active").value(true));

        verify(courtRepository).existsByCourtNumber(4);
        verify(courtRepository).save(any(Court.class));
        verifyNoMoreInteractions(courtRepository, bookingRepository);

    }

    @Test
    void addCourt_returnsMessageWhenCourtNumberAlreadyExists() throws Exception {
        String json = """
                {"courtNumber": 4, "hourlyRateSek": 300 }
                """;

        when(courtRepository.existsByCourtNumber(4)).thenReturn(true);

        mockMvc.perform(post("/api/wigellpadel/v1/addcourt")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("court_number already exists"));

        verify(courtRepository).existsByCourtNumber(4);
        verifyNoMoreInteractions(courtRepository, bookingRepository);
    }

    @Test
    void checkAvailability_returnsAvailableWhenNoBookings() throws Exception {
        Integer courtId = 5;
        String date = "2025-01-01";

        Court court = new Court();
        court.setActive(true);
        court.setCourtNumber(5);
        court.setHourlyRateSek(300);
        ReflectionTestUtils.setField(court, "id", courtId);

        when(courtRepository.findByIdAndActiveTrue(courtId)).thenReturn(Optional.of(court));
        when(bookingRepository.existsByCourtIdAndBookingDateAndStatus(
                eq(courtId), eq(LocalDate.parse(date)), eq(BookingStatus.ACTIVE)))
        .thenReturn(false);

        mockMvc.perform(get("/api/wigellpadel/checkavailability/{courtId}/{date}", courtId, date))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.courtId").value(courtId))
                .andExpect(jsonPath("$.data.date").value(date))
                .andExpect(jsonPath("$.data.available").value(true))
                .andExpect(jsonPath("$.data.booked").doesNotExist());

        verify(courtRepository).findByIdAndActiveTrue(courtId);
        verify(bookingRepository).existsByCourtIdAndBookingDateAndStatus(courtId, LocalDate.parse(date), BookingStatus.ACTIVE);
        verifyNoMoreInteractions(courtRepository, bookingRepository);
    }

    @Test
    void checkAvailability_returns400WhenDateIsInvalid() throws Exception {
        Integer courtId = 6;
        String badDate = "2025-13-01";

        Court court = new Court();
        court.setActive(true);
        when(courtRepository.findByIdAndActiveTrue(courtId)).thenReturn(Optional.of(court));
        mockMvc.perform(get("/api/wigellpadel/checkavailability/{courtId}/{date}", courtId, badDate))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("date must be YYYY-MM-DD"));

        verify(courtRepository).findByIdAndActiveTrue(courtId);
        verifyNoMoreInteractions(courtRepository, bookingRepository);
    }

    @Test
    void checkAvailability_returnNotfound() throws Exception {
        Integer courtId = 99;
        String date = "2025-12-01";

        when(courtRepository.findByIdAndActiveTrue(courtId)).thenReturn(Optional.empty());
        mockMvc.perform(get("/api/wigellpadel/checkavailability/{courtId}/{date}", courtId, date))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Court with id 99 not found"));

        verify(courtRepository).findByIdAndActiveTrue(courtId);
        verifyNoMoreInteractions(courtRepository, bookingRepository);
    }

    @Test
    void listCourts_returnDataWhenNotEmpty() throws Exception {
        Court court = new Court();
        court.setActive(true);
        court.setCourtNumber(8);
        court.setHourlyRateSek(400);

        when(courtRepository.findAllByActiveTrue()).thenReturn(List.of(court));
        mockMvc.perform(get("/api/wigellpadel/listcourts"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data.length()").value(1))
                .andExpect(jsonPath("$.message").doesNotExist());

        verify(courtRepository).findAllByActiveTrue();
        verifyNoMoreInteractions(courtRepository, bookingRepository);
    }

    @Test
    void removeCourt_softDeleteAndReturnMessage() throws Exception {
        Integer courtId = 42;

        Court existingCourt = new Court();
        existingCourt.setActive(true);
        existingCourt.setCourtNumber(5);
        existingCourt.setHourlyRateSek(300);

        when(courtRepository.findById(courtId)).thenReturn(Optional.of(existingCourt));
        when(courtRepository.save(any(Court.class))).thenAnswer(inv -> inv.getArgument(0));

        mockMvc.perform(delete("/api/wigellpadel/v1/remcourt/{id}", courtId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Court deleted/inactive"));

        verify(courtRepository).findById(courtId);
        verify(courtRepository).save(any(Court.class));
        verifyNoMoreInteractions(courtRepository, bookingRepository);
    }

    @Test
    void removeCourt_returnsMessageWhenCourtInactive() throws Exception {
        Integer courtId = 50;

        Court existingCourt = new Court();
        existingCourt.setActive(false);

        when(courtRepository.findById(courtId)).thenReturn(Optional.of(existingCourt));
        mockMvc.perform(delete("/api/wigellpadel/v1/remcourt/{id}", courtId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Court deleted/inactive"));

        verify(courtRepository).findById(courtId);
        verifyNoMoreInteractions(courtRepository, bookingRepository);
    }

    @Test
    void removeCourt_returns404_whenCourtNotFound() throws Exception {
        Integer courtId = 77;

        when(courtRepository.findById(courtId)).thenReturn(Optional.empty());

        mockMvc.perform(delete("/api/wigellpadel/v1/remcourt/{id}", courtId))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Court with id 77 not found"));

        verify(courtRepository).findById(courtId);
        verifyNoMoreInteractions(courtRepository, bookingRepository);
    }

    @Test
    void updateCourt_returnsOkAndUpdatedData() throws Exception {
        String json = """
      {
        "id": 10,
        "courtNumber": 6,
        "hourlyRateSek": 350
      }
    """;

        Court existing = new Court();
        existing.setActive(true);
        existing.setCourtNumber(5);
        existing.setHourlyRateSek(300);
        ReflectionTestUtils.setField(existing, "id", 10);


        when(courtRepository.findById(10)).thenReturn(Optional.of(existing));
        when(courtRepository.existsByCourtNumber(6)).thenReturn(false);
        when(courtRepository.save(any(Court.class))).thenAnswer(inv -> inv.getArgument(0));

        mockMvc.perform(put("/api/wigellpadel/v1/updatecourt")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(json))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id").value(10))
                .andExpect(jsonPath("$.data.courtNumber").value(6))
                .andExpect(jsonPath("$.data.hourlyRateSek").value(350))
                .andExpect(jsonPath("$.data.active").value(true));

        verify(courtRepository).findById(10);
        verify(courtRepository).existsByCourtNumber(6);
        verify(courtRepository).save(any(Court.class));
        verifyNoMoreInteractions(courtRepository, bookingRepository);
    }

    @Test
    void checkAvailability_returnsFalseAndBookedSlots_whenThereAreBookings() throws Exception {
        Integer courtId = 7;
        String date = "2025-02-01";
        LocalDate theDate = LocalDate.parse(date);

        Court court = new Court();
        court.setActive(true);
        when(courtRepository.findByIdAndActiveTrue(courtId)).thenReturn(Optional.of(court));

        when(bookingRepository.existsByCourtIdAndBookingDateAndStatus(
                eq(courtId), eq(theDate), eq(BookingStatus.ACTIVE)
        )).thenReturn(true);

        Booking b1 = org.mockito.Mockito.mock(Booking.class);
        Booking b2 = org.mockito.Mockito.mock(Booking.class);
        org.mockito.Mockito.when(b1.getStartTime()).thenReturn(LocalTime.of(10, 0));
        org.mockito.Mockito.when(b1.getEndTime()).thenReturn(LocalTime.of(11, 0));
        org.mockito.Mockito.when(b2.getStartTime()).thenReturn(LocalTime.of(12, 0));
        org.mockito.Mockito.when(b2.getEndTime()).thenReturn(LocalTime.of(13, 0));

        when(bookingRepository.findByCourtIdAndBookingDateAndStatus(
                eq(courtId), eq(theDate), eq(BookingStatus.ACTIVE), org.mockito.ArgumentMatchers.any(Sort.class)
        )).thenReturn(List.of(b1, b2));

        mockMvc.perform(get("/api/wigellpadel/checkavailability/{courtId}/{date}", courtId, date))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.courtId").value(courtId))
                .andExpect(jsonPath("$.data.date").value(date))
                .andExpect(jsonPath("$.data.available").value(false))
                .andExpect(jsonPath("$.data.booked").isArray())
                .andExpect(jsonPath("$.data.booked.length()").value(2))
                .andExpect(jsonPath("$.data.booked[0].startTime").value("10:00"))
                .andExpect(jsonPath("$.data.booked[0].endTime").value("11:00"))
                .andExpect(jsonPath("$.data.booked[1].startTime").value("12:00"))
                .andExpect(jsonPath("$.data.booked[1].endTime").value("13:00"));

        verify(courtRepository).findByIdAndActiveTrue(courtId);
        verify(bookingRepository).existsByCourtIdAndBookingDateAndStatus(courtId, theDate, BookingStatus.ACTIVE);
        verify(bookingRepository).findByCourtIdAndBookingDateAndStatus(
                eq(courtId), eq(theDate), eq(BookingStatus.ACTIVE), org.mockito.ArgumentMatchers.any(Sort.class)
        );
        verifyNoMoreInteractions(courtRepository, bookingRepository);
    }


}
