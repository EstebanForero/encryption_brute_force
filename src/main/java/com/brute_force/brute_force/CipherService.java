package com.brute_force.brute_force;

import java.util.*;
import java.util.concurrent.*;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import com.brute_force.brute_force.ciphertool.*;
import jakarta.annotation.PreDestroy;

@Service
public class CipherService {
    private final int cores = Runtime.getRuntime().availableProcessors();
    private final ExecutorService executor = Executors.newFixedThreadPool(cores);

    public String processWithKnownKey(String cipherType, String operation, String text, String key) {
        CipherUtility cipher = CipherFactory.createCipher(cipherType);

        if ("encrypt".equalsIgnoreCase(operation)) {
            return cipher.encrypt(text, key);
        } else if ("decrypt".equalsIgnoreCase(operation)) {
            return cipher.decrypt(text, key);
        }

        throw new IllegalArgumentException("Invalid operation: " + operation);
    }

    public List<DecryptionAttempt> bruteForceDecrypt(String cipherType, String text,
            CipherForm form, String language) {

        List<Callable<List<DecryptionAttempt>>> tasks = new ArrayList<>();

        if ("caesar".equalsIgnoreCase(cipherType)) {
            tasks.addAll(createCaesarBruteForceTasks(text, language));
        } else if ("vigenere".equalsIgnoreCase(cipherType) || "vigenère".equalsIgnoreCase(cipherType)) {
            tasks.addAll(createVigenereBruteForceTasks(text, form, language));
        }

        return executeTasksAndCollectResults(tasks);
    }

    private List<Callable<List<DecryptionAttempt>>> createCaesarBruteForceTasks(String text, String language) {
        List<Callable<List<DecryptionAttempt>>> tasks = new ArrayList<>();
        CipherUtility cipher = CipherFactory.createCipher("caesar");

        for (int shift = 0; shift < 26; shift++) {
            int finalShift = shift;
            tasks.add(() -> {
                String decrypted = cipher.decrypt(text, String.valueOf(finalShift));
                int score = CipherUtils.countCommonWords(decrypted, language);
                return Arrays.asList(new DecryptionAttempt(decrypted, String.valueOf(finalShift), score));
            });
        }
        return tasks;
    }

    private List<Callable<List<DecryptionAttempt>>> createVigenereBruteForceTasks(String text,
            CipherForm form, String language) {
        List<String> keys = generateVigenereKeys(form);
        List<Callable<List<DecryptionAttempt>>> tasks = new ArrayList<>();
        CipherUtility cipher = CipherFactory.createCipher("vigenere");

        final int TOP_N_PER_CHUNK = 100;
        int chunkSize = Math.max(1, keys.size() / cores);

        for (int i = 0; i < keys.size(); i += chunkSize) {
            List<String> chunk = keys.subList(i, Math.min(i + chunkSize, keys.size()));
            tasks.add(() -> {
                List<DecryptionAttempt> chunkAttempts = new ArrayList<>();
                for (String key : chunk) {
                    String decrypted = cipher.decrypt(text, key);
                    int score = CipherUtils.countCommonWords(decrypted, language);
                    chunkAttempts.add(new DecryptionAttempt(decrypted, key, score));
                }
                chunkAttempts.sort((a, b) -> b.getScore() - a.getScore());
                return chunkAttempts.stream().limit(TOP_N_PER_CHUNK).collect(Collectors.toList());
            });
        }
        return tasks;
    }

    private List<String> generateVigenereKeys(CipherForm form) {
        if ("rockyou".equals(form.getBreakMethod())) {
            int n = Math.min(form.getNumPasswords(), CipherUtils.rockyouPasswords.size());
            if (n <= 0) {
                throw new IllegalArgumentException("Number of passwords must be greater than 0.");
            }
            return CipherUtils.rockyouPasswords.subList(0, n);
        } else if ("bruteForce".equals(form.getBreakMethod())) {
            int length = form.getBruteForceLength();
            if (length < 1 || length > 5) {
                throw new IllegalArgumentException("Brute-force key length must be between 1 and 5.");
            }
            return CipherUtils.generateKeys(length);
        }
        throw new IllegalArgumentException("Invalid breaking method: " + form.getBreakMethod());
    }

    private List<DecryptionAttempt> executeTasksAndCollectResults(List<Callable<List<DecryptionAttempt>>> tasks) {
        List<DecryptionAttempt> attempts = new ArrayList<>();
        boolean partialResults = false;

        try {
            List<Future<List<DecryptionAttempt>>> futures = executor.invokeAll(tasks, 30, TimeUnit.SECONDS);
            for (Future<List<DecryptionAttempt>> future : futures) {
                try {
                    List<DecryptionAttempt> attempt = future.get(0, TimeUnit.SECONDS);
                    attempts.addAll(attempt);
                } catch (CancellationException | ExecutionException | TimeoutException e) {
                    partialResults = true;
                }
            }
        } catch (InterruptedException e) {
            partialResults = true;
        }

        if (partialResults) {
            System.out.println("Error processing tasks, partial results");
        }

        attempts.sort((a, b) -> b.getScore() - a.getScore());
        return attempts.stream().limit(120).collect(Collectors.toList());
    }

    @PreDestroy
    public void shutdownExecutor() {
        executor.shutdown();
    }
}
