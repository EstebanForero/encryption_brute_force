package com.brute_force.brute_force.ciphertool;

import java.util.ArrayList;
import java.util.List;

public class CaesarCipher implements CipherUtility {
    private final String alphabet;

    public CaesarCipher(String alphabet) {
        this.alphabet = alphabet.toUpperCase();
    }

    public CaesarCipher() {
        this("ABCDEFGHIJKLMNOPQRSTUVWXYZ");
    }

    @Override
    public String encrypt(String text, String key) {
        int shift = Integer.parseInt(key);
        return processText(text, shift);
    }

    @Override
    public String decrypt(String text, String key) {
        int shift = Integer.parseInt(key);
        return processText(text, alphabet.length() - (shift % alphabet.length()));
    }

    private String processText(String text, int shift) {
        StringBuilder result = new StringBuilder();
        for (char c : text.toUpperCase().toCharArray()) {
            int pos = alphabet.indexOf(c);
            if (pos != -1) {
                int newPos = (pos + shift) % alphabet.length();
                result.append(alphabet.charAt(newPos));
            } else {
                result.append(c); // Preserve characters that aren't in the alphabet, like ',, ,.' etc
            }
        }
        return result.toString();
    }

    public List<String> bruteForceDecrypt(String text) {
        List<String> results = new ArrayList<>();
        for (int shift = 0; shift < alphabet.length(); shift++) {
            results.add(decrypt(text, String.valueOf(shift)));
        }
        return results;
    }
}
