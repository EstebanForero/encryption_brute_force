package com.brute_force.brute_force.ciphertool;

/**
 * CipherFactory
 */
public class CipherFactory {
    private static final String DEFAULT_ALPHABET = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";

    public static CipherUtility createCipher(String cipherType) {
        return createCipher(cipherType, DEFAULT_ALPHABET);
    }

    public static CipherUtility createCipher(String cipherType, String alphabet) {
        switch (cipherType.toLowerCase()) {
            case "caesar":
                return new CaesarCipher(alphabet);
            case "vigenere":
            case "vigenère":
                return new VigenereCipher(alphabet);
            default:
                throw new IllegalArgumentException("Unsupported cipher type: " + cipherType);
        }
    }
}
