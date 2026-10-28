package com.example.activitytracker.service;

import com.example.activitytracker.exception.AIQuotaExceededException;
import com.example.activitytracker.repository.AIGlobalDailyUsageRepository;
import com.example.activitytracker.repository.AIUserDailyUsageRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

import java.time.LocalDate;

@ExtendWith(MockitoExtension.class)
class AIQuotaServiceImplTest {

    @Mock
    private AIUserDailyUsageRepository userUsageRepository;

    @Mock
    private AIGlobalDailyUsageRepository globalUsageRepository;

    private AIQuotaServiceImpl aiQuotaService;

    @BeforeEach
    void setUp() {
        aiQuotaService = new AIQuotaServiceImpl(
                userUsageRepository,
                globalUsageRepository,
                5,
                100,
                "Asia/Kolkata"
        );
    }

    @Test
    void shouldReserveRequestWhenBothQuotasAreAvailable() {
        when(userUsageRepository.reserveRequest(
                anyLong(), any(LocalDate.class), anyInt()))
                .thenReturn(1);

        when(globalUsageRepository.reserveRequest(
                any(LocalDate.class), anyInt()))
                .thenReturn(1);

        assertDoesNotThrow(() -> aiQuotaService.reserveRequest(1L));

        verify(userUsageRepository).ensureDailyRowExists(
                eq(1L), any(LocalDate.class));

        verify(globalUsageRepository).ensureDailyRowExists(
                any(LocalDate.class));

        verify(userUsageRepository).reserveRequest(
                eq(1L), any(LocalDate.class), eq(5));

        verify(globalUsageRepository).reserveRequest(
                any(LocalDate.class), eq(100));
    }

    @Test
    void shouldRejectRequestWhenUserQuotaIsExhausted() {
        when(userUsageRepository.reserveRequest(
                anyLong(), any(LocalDate.class), anyInt()))
                .thenReturn(0);

        assertThrows(
                AIQuotaExceededException.class,
                () -> aiQuotaService.reserveRequest(1L)
        );

        verify(userUsageRepository).reserveRequest(
                eq(1L), any(LocalDate.class), eq(5));

        verify(globalUsageRepository, never()).reserveRequest(
                any(LocalDate.class), anyInt());
    }

    @Test
    void shouldRejectRequestWhenGlobalQuotaIsExhausted() {
        when(userUsageRepository.reserveRequest(
                anyLong(), any(LocalDate.class), anyInt()))
                .thenReturn(1);

        when(globalUsageRepository.reserveRequest(
                any(LocalDate.class), anyInt()))
                .thenReturn(0);

        assertThrows(
                AIQuotaExceededException.class,
                () -> aiQuotaService.reserveRequest(1L)
        );

        verify(userUsageRepository).reserveRequest(
                eq(1L), any(LocalDate.class), eq(5));

        verify(globalUsageRepository).reserveRequest(
                any(LocalDate.class), eq(100));
    }

    @Test
    void shouldRejectNullUserId() {
        assertThrows(
                IllegalArgumentException.class,
                () -> aiQuotaService.reserveRequest(null)
        );

        verifyNoInteractions(userUsageRepository, globalUsageRepository);
    }
}
