package com.harness.demo;

public class InputNormalizer {

    public String normalize(String input) {
        if (input == null || input.isBlank()) {
            return "world";
        }
        return input.trim();
    }
}
