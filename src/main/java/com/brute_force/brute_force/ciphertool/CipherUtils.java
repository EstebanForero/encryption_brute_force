package com.brute_force.brute_force.ciphertool;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.*;
import java.util.stream.Collectors;

public class CipherUtils {
    public static final Map<String, Set<String>> commonWordsByLanguage = new HashMap<>();
    public static final List<String> rockyouPasswords = new ArrayList<>();

    static {
        try {
            List<String> common_english = readLines("/data/common_english.txt", 300);
            List<String> common_spanish = readLines("/data/common_spanish.txt", 300);

            Set<String> common_english_set = new HashSet<String>(common_english);
            Set<String> common_spanish_set = new HashSet<String>(common_spanish);

            commonWordsByLanguage.put("English", common_english_set);
            commonWordsByLanguage.put("Spanish", common_spanish_set);
            rockyouPasswords.addAll(readLines("/data/rockyou.txt", 1000).stream().collect(Collectors.toList()));
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
        Set<String> commonWords = commonWordsByLanguage.getOrDefault(language, Collections.emptySet());
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
