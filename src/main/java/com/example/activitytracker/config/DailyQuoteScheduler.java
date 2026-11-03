package com.example.activitytracker.config;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import java.util.Random;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.example.activitytracker.model.DailyQuote;
import com.example.activitytracker.model.Quote;
import com.example.activitytracker.repository.DailyQuoteRepository;
import com.example.activitytracker.repository.QuoteRepository;

@Component
public class DailyQuoteScheduler {

    private static final Logger logger =
            LoggerFactory.getLogger(DailyQuoteScheduler.class);

    private static final ZoneId APP_ZONE =
            ZoneId.of("Asia/Kolkata");

    private final DailyQuoteRepository dailyQuoteRepository;
    private final QuoteRepository quoteRepository;

    public DailyQuoteScheduler(
            DailyQuoteRepository dailyQuoteRepository,
            QuoteRepository quoteRepository) {

        this.dailyQuoteRepository = dailyQuoteRepository;
        this.quoteRepository = quoteRepository;
    }

    @EventListener(ApplicationReadyEvent.class)
    public void generateQuoteOnStartup() {
        try {
            generateDailyQuote();
        } catch (Exception ex) {
            logger.error(
                    "Failed to generate daily quote during startup. "
                    + "The application will continue running.",
                    ex
            );
        }
    }

    @Scheduled(cron = "0 0 0 * * *", zone = "Asia/Kolkata")
    public void generateDailyQuote() {

        LocalDate today = LocalDate.now(APP_ZONE);

        if (dailyQuoteRepository.findByDate(today).isPresent()) {
            return;
        }

        List<Quote> quotes = quoteRepository.findAll();

        if (quotes.isEmpty()) {
            logger.warn("No quotes are available to generate today's daily quote.");
            return;
        }

        Quote selectedQuote =
                quotes.get(new Random().nextInt(quotes.size()));

        DailyQuote dailyQuote = new DailyQuote();
        dailyQuote.setDate(today);
        dailyQuote.setQuote(selectedQuote);

        dailyQuoteRepository.save(dailyQuote);

        logger.info("Daily quote generated for {}", today);
    }
}