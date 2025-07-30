package com.brute_force.brute_force.ciphertool;

public class DecryptionAttempt {
    private String text;
    private String key;
    private int score;

    public DecryptionAttempt(String text, String key, int score) {
        this.text = text;
        this.key = key;
        this.score = score;
    }

    public String getText() {
        return text;
    }

    public String getKey() {
        return key;
    }

    public int getScore() {
        return score;
    }
}
