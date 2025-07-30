package com.brute_force.brute_force.ciphertool;

import java.util.ArrayList;
import java.util.List;

public class CaesarCipher {
    private static final String ALPHABET = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";

    public static String encrypt(String text, int shift) {
        StringBuilder result = new StringBuilder();
        for (char c : text.toUpperCase().toCharArray()) {
            int pos = ALPHABET.indexOf(c);
            if (pos != -1) {
                int newPos = (pos + shift) % 26;
                result.append(ALPHABET.charAt(newPos));
            } else { // This part appends any letter that isn't in the alphabet
                result.append(c);
            }
        }
        return result.toString();
    }

    public static String decrypt(String text, int shift) {
        return encrypt(text, 26 - (shift % 26));
    }

    public static List<String> bruteForceDecrypt(String text) {
        List<String> results = new ArrayList<>();
        for (int shift = 0; shift < 26; shift++) {
            results.add(decrypt(text, shift));
        }
        // Todo decrypt inverse
        return results;
    }
}
