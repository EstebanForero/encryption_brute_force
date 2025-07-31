package com.brute_force.brute_force.ciphertool;

public class VigenereCipher implements CipherUtility {
    private final String alphabet;

    public VigenereCipher(String alphabet) {
        this.alphabet = alphabet.toUpperCase();
    }

    public VigenereCipher() {
        this("ABCDEFGHIJKLMNOPQRSTUVWXYZ");
    }

    @Override
    public String encrypt(String text, String key) {
        return processText(text, key, true);
    }

    @Override
    public String decrypt(String text, String key) {
        return processText(text, key, false);
    }

    private String processText(String text, String key, boolean encrypt) {
        StringBuilder result = new StringBuilder();
        String upperKey = key.toUpperCase();
        int keyIndex = 0;

        for (char c : text.toUpperCase().toCharArray()) {
            int pos = alphabet.indexOf(c);
            if (pos != -1) {
                int shift = alphabet.indexOf(upperKey.charAt(keyIndex % upperKey.length()));
                int newPos;
                if (encrypt) {
                    newPos = (pos + shift) % alphabet.length();
                } else {
                    newPos = (pos - shift + alphabet.length()) % alphabet.length();
                }
                result.append(alphabet.charAt(newPos));
                keyIndex++;
            } else {
                result.append(c); // Preserve characters that aren't in the alphabet, like ',, ,.' etc
            }
        }
        return result.toString();
    }
}
