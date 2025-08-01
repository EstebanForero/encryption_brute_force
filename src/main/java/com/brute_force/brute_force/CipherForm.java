package com.brute_force.brute_force;

public class CipherForm {
    private String cipher;
    private String operation;
    private String language;
    private String text;
    private int key;
    private String vigenereKey;
    private boolean useKnownKey;
    private String breakMethod;
    private int numPasswords;
    private int bruteForceLength = 1; // The efault is 1

    public String getCipher() {
        return cipher;
    }

    public void setCipher(String cipher) {
        this.cipher = cipher;
    }

    public String getOperation() {
        return operation;
    }

    public void setOperation(String operation) {
        this.operation = operation;
    }

    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public int getKey() {
        return key;
    }

    public void setKey(int key) {
        this.key = key;
    }

    public String getVigenereKey() {
        return vigenereKey;
    }

    public void setVigenereKey(String vigenereKey) {
        this.vigenereKey = vigenereKey;
    }

    public boolean isUseKnownKey() {
        return useKnownKey;
    }

    public void setUseKnownKey(boolean useKnownKey) {
        this.useKnownKey = useKnownKey;
    }

    public String getBreakMethod() {
        return breakMethod;
    }

    public void setBreakMethod(String breakMethod) {
        this.breakMethod = breakMethod;
    }

    public int getNumPasswords() {
        return numPasswords;
    }

    public void setNumPasswords(int numPasswords) {
        this.numPasswords = numPasswords;
    }

    public int getBruteForceLength() {
        return bruteForceLength;
    }

    public void setBruteForceLength(int bruteForceLength) {
        this.bruteForceLength = bruteForceLength;
    }
}
