package com.example.activitytracker.config;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import java.util.Random;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.example.activitytracker.model.DailyQuote;
import com.example.activitytracker.model.Quote;
import com.example.activitytracker.repository.DailyQuoteRepository;
import com.example.activitytracker.repository.QuoteRepository;

import jakarta.annotation.PostConstruct;

@Component
public class DailyQuoteScheduler {

    private final DailyQuoteRepository dailyQuoteRepository;
    private final QuoteRepository quoteRepository;

    public DailyQuoteScheduler(
            DailyQuoteRepository dailyQuoteRepository,
            QuoteRepository quoteRepository) {

        this.dailyQuoteRepository = dailyQuoteRepository;
        this.quoteRepository = quoteRepository;
    }   // ← constructor ends here

    @PostConstruct
    public void generateQuoteOnStartup() {
        generateDailyQuote();
    }

    @Scheduled(cron = "0 0 0 * * *", zone = "Asia/Kolkata")
    public void generateDailyQuote() {

        LocalDate today = LocalDate.now(ZoneId.of("Asia/Kolkata"));

        if (dailyQuoteRepository.findByDate(today).isPresent()) {
            return;
        }

        List<Quote> quotes = quoteRepository.findAll();

        if (quotes.isEmpty()) {
            return;
        }

        Random random = new Random();

        Quote selectedQuote =
                quotes.get(random.nextInt(quotes.size()));

        DailyQuote dailyQuote = new DailyQuote();

        dailyQuote.setDate(today);
        dailyQuote.setQuote(selectedQuote);

        dailyQuoteRepository.save(dailyQuote);
    }
}