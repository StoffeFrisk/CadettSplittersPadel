package com.frisk.cadettsplitterspadel.services;

import com.frisk.cadettsplitterspadel.entities.Court;
import com.frisk.cadettsplitterspadel.exceptions.ResourceNotFoundException;
import com.frisk.cadettsplitterspadel.repositories.CourtRepository;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Optional;

import static org.mockito.Mockito.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;


@ExtendWith(MockitoExtension.class)
public class CourtServiceImplTest {

    @Mock
    private CourtRepository courtRepository;

    @InjectMocks
    private CourtServiceImpl courtService;

    @Test
    void add_returnsSavedCourt() {
        Court court = new Court();
        court.setCourtNumber(3);
        court.setHourlyRateSek(250);

        when(courtRepository.existsByCourtNumber(3)).thenReturn(false);

        Court savedCourt = new Court();
        savedCourt.setActive(true);

        savedCourt.setCourtNumber(3);
        savedCourt.setHourlyRateSek(250);
        when(courtRepository.save(any(Court.class))).thenReturn(savedCourt);

        Court result = courtService.add(court);

        verify(courtRepository).existsByCourtNumber(3);
        verify(courtRepository).save(any(Court.class));

        assertThat(result.getCourtNumber()).isEqualTo(3);
        assertThat(result.getHourlyRateSek()).isEqualTo(250);
        assertThat(result.isActive()).isTrue();

    }

    @Test
    void add_throwsWhenCourtNumberIsNotPositive() {
        Court court = new Court();
        court.setCourtNumber(0);
        court.setHourlyRateSek(250);

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> courtService.add(court));
        assertThat(ex.getMessage()).contains("courtNumber must be > 0");
    }

    @Test
    void add_throwsWhenHourlyRateSekIsNotPositive() {
        Court court = new Court();
        court.setCourtNumber(2);
        court.setHourlyRateSek(0);

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> courtService.add(court));
        assertThat(ex.getMessage()).contains("hourlyRateSek must be > 0");
    }

    @Test
    void add_throwsWhenCourtNumberAlreadyExists() {
        Court court = new Court();
        court.setCourtNumber(5);
        court.setHourlyRateSek(250);

        when(courtRepository.existsByCourtNumber(5)).thenReturn(true);

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> courtService.add(court));
        assertThat(ex.getMessage()).contains("court_number already exists");

        verify(courtRepository).existsByCourtNumber(5);
        verify(courtRepository, never()).save(any());
    }

    @Test
    void update_updatesNumberAndRate() {
        Court incomingCourt = new Court();
        incomingCourt.setCourtNumber(4);
        incomingCourt.setHourlyRateSek(250);

        ReflectionTestUtils.setField(incomingCourt, "id", 10);

        Court existingCourt = new Court();
        existingCourt.setActive(true);
        existingCourt.setCourtNumber(3);
        existingCourt.setHourlyRateSek(250);

        ReflectionTestUtils.setField(existingCourt, "id", 10);

        when(courtRepository.findById(10)).thenReturn(Optional.of(existingCourt));
        when(courtRepository.existsByCourtNumber(4)).thenReturn(false);
        when(courtRepository.save(any(Court.class))).thenAnswer(inv ->inv.getArgument(0));

        Court result = courtService.update(incomingCourt);
        verify(courtRepository).findById(10);
        verify(courtRepository).existsByCourtNumber(4);
        verify(courtRepository).save(any(Court.class));

        assertThat(result.getCourtNumber()).isEqualTo(4);
        assertThat(result.getHourlyRateSek()).isEqualTo(250);
        assertThat(result.isActive()).isTrue();
    }

    @Test
    void update_throwsWhenIdIsNull() {
        Court incomingCourt = new Court();
        incomingCourt.setCourtNumber(4);
        incomingCourt.setHourlyRateSek(250);

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> courtService.update(incomingCourt));

        assertThat(ex.getMessage()).contains("Court id is required");
        verifyNoInteractions(courtRepository);
    }

    @Test
    void update_throwsWhenCourtNotFound() {
        Court incomingCourt = new Court();
        incomingCourt.setCourtNumber(4);
        incomingCourt.setHourlyRateSek(250);
        ReflectionTestUtils.setField(incomingCourt, "id", 10);

        when(courtRepository.findById(10)).thenReturn(Optional.empty());

        ResourceNotFoundException ex = assertThrows(ResourceNotFoundException.class, () -> courtService.update(incomingCourt));

        assertThat(ex.getMessage()).isEqualTo("Court with id 10 not found");
        verify(courtRepository).findById(10);
        verifyNoMoreInteractions(courtRepository);

    }

    @Test
    void update_throwsWhenCourtIsInactive() {
        Court incomingCourt = new Court();
        incomingCourt.setCourtNumber(4);
        incomingCourt.setHourlyRateSek(250);
        ReflectionTestUtils.setField(incomingCourt, "id", 11);

        Court existingCourt = new Court();
        existingCourt.setActive(false);
        existingCourt.setCourtNumber(3);
        existingCourt.setHourlyRateSek(250);
        ReflectionTestUtils.setField(existingCourt, "id", 11);

        when(courtRepository.findById(11)).thenReturn(Optional.of(existingCourt));

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> courtService.update(incomingCourt));
        assertThat(ex.getMessage()).contains("Court is deleted/inactive");

        verify(courtRepository).findById(11);
        verifyNoMoreInteractions(courtRepository);
    }

    @Test
    void update_throwsWhenCourtNumberNotPositive() {
        Court incomingCourt = new Court();
        incomingCourt.setCourtNumber(0);
        incomingCourt.setHourlyRateSek(250);
        ReflectionTestUtils.setField(incomingCourt, "id", 12);

        Court existingCourt = new Court();
        existingCourt.setActive(true);
        existingCourt.setCourtNumber(3);
        existingCourt.setHourlyRateSek(250);
        ReflectionTestUtils.setField(existingCourt, "id", 12);

        when(courtRepository.findById(12)).thenReturn(Optional.of(existingCourt));

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> courtService.update(incomingCourt));
        assertThat(ex.getMessage()).contains("courtNumber must be > 0");

        verify(courtRepository).findById(12);
        verifyNoMoreInteractions(courtRepository);
    }

    @Test
    void update_throwsWhenHourlyRateSekNotPositive() {
        Court incomingCourt = new Court();
        incomingCourt.setCourtNumber(4);
        incomingCourt.setHourlyRateSek(0);
        ReflectionTestUtils.setField(incomingCourt, "id", 13);

        Court existingCourt = new Court();
        existingCourt.setActive(true);
        existingCourt.setCourtNumber(3);
        existingCourt.setHourlyRateSek(250);

        when(courtRepository.findById(13)).thenReturn(Optional.of(existingCourt));

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> courtService.update(incomingCourt));
        assertThat(ex.getMessage()).contains("hourlyRateSek must be > 0");

        verify(courtRepository).findById(13);
        verifyNoMoreInteractions(courtRepository);
    }

    @Test
    void update_throwsWhenCourtNumberAlreadyExists() {
        Court incomingCourt = new Court();
        incomingCourt.setCourtNumber(7);
        incomingCourt.setHourlyRateSek(250);
        ReflectionTestUtils.setField(incomingCourt, "id", 14);

        Court existingCourt = new Court();
        existingCourt.setActive(true);
        existingCourt.setCourtNumber(3);
        existingCourt.setHourlyRateSek(250);

        when(courtRepository.findById(14)).thenReturn(Optional.of(existingCourt));
        when(courtRepository.existsByCourtNumber(7)).thenReturn(true);

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> courtService.update(incomingCourt));
        assertThat(ex.getMessage()).contains("court_number already exists");

        verify(courtRepository).findById(14);
        verify(courtRepository).existsByCourtNumber(7);
        verify(courtRepository, never()).save(any());
        verifyNoMoreInteractions(courtRepository);
    }

    @Test
    void delete_setsActiveFalseAndSaves() {

        Integer id = 20;

        Court existingCourt = new Court();
        existingCourt.setActive(true);
        existingCourt.setCourtNumber(9);
        existingCourt.setHourlyRateSek(250);
        ReflectionTestUtils.setField(existingCourt, "id", id);

        when(courtRepository.findById(id)).thenReturn(Optional.of(existingCourt));
        when(courtRepository.save(any(Court.class))).thenAnswer(inv -> inv.getArgument(0));

        courtService.delete(id);

        verify(courtRepository).findById(id);
        ArgumentCaptor<Court> courtCaptor = ArgumentCaptor.forClass(Court.class);
        verify(courtRepository).save(courtCaptor.capture());

        Court savedCourt = courtCaptor.getValue();
        assertThat(savedCourt.isActive()).isFalse();
        verifyNoMoreInteractions(courtRepository);
    }

    @Test
    void delete_throwsWhenIdIsNull() {

        Integer id = null;

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> courtService.delete(id));
        assertThat(ex.getMessage()).contains("courtId required");
        verifyNoInteractions(courtRepository);
    }

    @Test
    void delete_throwsWhenCourtNotFound() {
        Integer id = 21;

        when(courtRepository.findById(21)).thenReturn(Optional.empty());

        ResourceNotFoundException ex = assertThrows(ResourceNotFoundException.class, () -> courtService.delete(id));
        assertThat(ex.getMessage()).contains("Court with id 21 not found");

        verify(courtRepository).findById(21);
        verifyNoMoreInteractions(courtRepository);
    }

    @Test
    void delete_CourtAlreadyInactive() {
        Integer id = 22;

        Court existingCourt = new Court();
        existingCourt.setActive(false);
        existingCourt.setCourtNumber(10);
        existingCourt.setHourlyRateSek(250);
        ReflectionTestUtils.setField(existingCourt, "id", id);

        when(courtRepository.findById(id)).thenReturn(Optional.of(existingCourt));
        courtService.delete(id);

        verify(courtRepository).findById(id);
        verify(courtRepository, never()).save(any());
        verifyNoMoreInteractions(courtRepository);
    }


}
