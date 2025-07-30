package com.brute_force.brute_force.ciphertool;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.*;
import java.util.stream.Collectors;

public class CipherUtils {
    public static final Map<String, List<String>> commonWordsByLanguage = new HashMap<>();
    public static final List<String> rockyouPasswords = new ArrayList<>();

    static {
        try {
            commonWordsByLanguage.put("English", readLines("/data/common_english.txt", 100));
            commonWordsByLanguage.put("Spanish", readLines("/data/common_spanish.txt", 100));
            rockyouPasswords.addAll(readLines("/data/rockyou.txt").stream().limit(1000).collect(Collectors.toList()));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static List<String> readLines(String resourcePath, int limit) throws Exception {
        try (InputStream is = CipherUtils.class.getResourceAsStream(resourcePath);
                BufferedReader reader = new BufferedReader(new InputStreamReader(is))) {
            return reader.lines().limit(limit).map(String::toUpperCase).collect(Collectors.toList());
        }
    }

    public static int countCommonWords(String text, String language) {
        List<String> commonWords = commonWordsByLanguage.getOrDefault(language, Collections.emptyList());
        String[] words = text.toUpperCase().split("\\s+");
        int count = 0;
        for (String word : words) {
            if (commonWords.contains(word)) {
                count++;
            }
        }
        return count;
    }

    public static List<String> generateKeys(int length) {
        List<String> keys = new ArrayList<>();
        char[] alphabet = "ABCDEFGHIJKLMNOPQRSTUVWXYZ".toCharArray();
        generateKeysRecursive("", length, alphabet, keys);
        return keys;
    }

    private static void generateKeysRecursive(String prefix, int length, char[] alphabet, List<String> keys) {
        if (prefix.length() == length) {
            keys.add(prefix);
            return;
        }
        for (char c : alphabet) {
            generateKeysRecursive(prefix + c, length, alphabet, keys);
        }
    }
}
