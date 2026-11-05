package com.example.activitytracker.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class UserTest {

    @Test
    void shouldExcludePasswordFromToString() {
        User user = new User();
        user.setName("Test User");
        user.setEmail("test@example.com");
        user.setPassword("secret-password");

        String result = user.toString();

        assertFalse(result.contains("secret-password"));
        assertFalse(result.contains("password="));
    }
}