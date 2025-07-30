package com.brute_force.brute_force.ciphertool;

public class VigenereCipher {
    private static final String ALPHABET = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";

    public static String encrypt(String text, String key) {
        StringBuilder result = new StringBuilder();
        key = key.toUpperCase();
        int keyIndex = 0;
        for (char c : text.toUpperCase().toCharArray()) {
            int pos = ALPHABET.indexOf(c);
            if (pos != -1) {
                int shift = ALPHABET.indexOf(key.charAt(keyIndex % key.length()));
                int newPos = (pos + shift) % 26;
                result.append(ALPHABET.charAt(newPos));
                keyIndex++;
            } else {
                result.append(c); // Preserve spaces and non-letters
            }
        }
        return result.toString();
    }

    public static String decrypt(String text, String key) {
        StringBuilder result = new StringBuilder();
        key = key.toUpperCase();
        int keyIndex = 0;
        for (char c : text.toUpperCase().toCharArray()) {
            int pos = ALPHABET.indexOf(c);
            if (pos != -1) {
                int shift = ALPHABET.indexOf(key.charAt(keyIndex % key.length()));
                int newPos = (pos - shift + 26) % 26;
                result.append(ALPHABET.charAt(newPos));
                keyIndex++;
            } else {
                result.append(c); // Preserve spaces and non-letters
            }
        }
        return result.toString();
    }
}
