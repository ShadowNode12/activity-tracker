package com.example.activitytracker.service;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Optional;

import org.springframework.stereotype.Service;

import com.example.activitytracker.exception.DailyQuoteNotFoundException;
import com.example.activitytracker.model.DailyQuote;
import com.example.activitytracker.repository.DailyQuoteRepository;

@Service
public class DailyQuoteServiceImpl implements DailyQuoteService {

    private static final ZoneId APP_ZONE = ZoneId.of("Asia/Kolkata");

    private final DailyQuoteRepository dailyQuoteRepository;

    public DailyQuoteServiceImpl(DailyQuoteRepository dailyQuoteRepository) {
        this.dailyQuoteRepository = dailyQuoteRepository;
    }

    @Override
    public DailyQuote getDailyQuote() {

        LocalDate today = LocalDate.now(APP_ZONE);

        Optional<DailyQuote> existingQuote =
                dailyQuoteRepository.findByDate(today);

        return existingQuote.orElseThrow(() ->
                new DailyQuoteNotFoundException(
                        "Today's daily quote is not available."
                )
        );
    }
}