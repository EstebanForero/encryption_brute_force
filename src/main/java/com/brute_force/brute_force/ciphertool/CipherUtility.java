package com.brute_force.brute_force.ciphertool;

/**
 * CipherUtility
 */
public interface CipherUtility {

    String encrypt(String text, String key);

    String decrypt(String text, String key);
}
