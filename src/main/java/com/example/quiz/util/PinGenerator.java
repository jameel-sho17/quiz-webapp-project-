package com.example.quiz.util;

import java.security.SecureRandom;

public final class PinGenerator {
    private static final SecureRandom RAND = new SecureRandom();

    private PinGenerator() {}

    /** Generates a 6-digit numeric PIN as a String, no leading zero. */
    public static String generate6Digit() {
        int n = 100000 + RAND.nextInt(900000); // 100000-999999
        return Integer.toString(n);
    }
}
