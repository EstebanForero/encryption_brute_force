package com.brute_force.brute_force;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import com.brute_force.brute_force.ciphertool.*;

import jakarta.annotation.PreDestroy;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;
import java.util.stream.Collectors;

@Controller
public class CipherController {

    private final int cores = Runtime.getRuntime().availableProcessors();
    private final ExecutorService executor = Executors.newFixedThreadPool(cores);

    @GetMapping("/")
    public String showForm(Model model) {
        model.addAttribute("cipherForm", new CipherForm());
        return "cipher-form";
    }

    @GetMapping("/update-form")
    public String updateForm(@RequestParam String cipher, @RequestParam String operation, Model model) {
        model.addAttribute("cipher", cipher);
        model.addAttribute("operation", operation);
        return "fragments/form-options";
    }

    @GetMapping("/key-options")
    public String keyOptions(@RequestParam String cipher, @RequestParam(defaultValue = "false") boolean useKnownKey,
            Model model) {
        model.addAttribute("cipher", cipher);
        model.addAttribute("useKnownKey", useKnownKey);
        return "fragments/key-options";
    }

    @GetMapping("/vigenere-break-options")
    public String vigenereBreakOptions(@RequestParam String breakMethod, Model model) {
        model.addAttribute("breakMethod", breakMethod);
        return "fragments/vigenere-break-options";
    }

    @PostMapping("/process")
    public String processForm(@ModelAttribute CipherForm form, Model model) {
        String result = "";
        List<DecryptionAttempt> attempts = new ArrayList<>();
        String language = form.getLanguage();
        boolean partialResults = false;

        // Validate input
        if (form.getText() == null || form.getText().trim().isEmpty()) {
            model.addAttribute("error", "Text input cannot be empty.");
            return "result";
        }

        if ("Encrypt".equals(form.getOperation())) {
            if ("Caesar".equals(form.getCipher())) {
                if (form.getKey() < 0 || form.getKey() > 25) {
                    model.addAttribute("error", "Caesar key must be between 0 and 25.");
                    return "result";
                }
                result = CaesarCipher.encrypt(form.getText(), form.getKey());
            } else if ("Vigenère".equals(form.getCipher())) {
                if (form.getVigenereKey() == null || form.getVigenereKey().trim().isEmpty()) {
                    model.addAttribute("error", "Vigenère keyword cannot be empty.");
                    return "result";
                }
                result = VigenereCipher.encrypt(form.getText(), form.getVigenereKey());
            }
        } else if ("Decrypt".equals(form.getOperation())) {
            if (form.isUseKnownKey()) {
                if ("Caesar".equals(form.getCipher())) {
                    if (form.getKey() < 0 || form.getKey() > 25) {
                        model.addAttribute("error", "Caesar key must be between 0 and 25.");
                        return "result";
                    }
                    result = CaesarCipher.decrypt(form.getText(), form.getKey());
                } else if ("Vigenère".equals(form.getCipher())) {
                    if (form.getVigenereKey() == null || form.getVigenereKey().trim().isEmpty()) {
                        model.addAttribute("error", "Vigenère keyword cannot be empty.");
                        return "result";
                    }
                    result = VigenereCipher.decrypt(form.getText(), form.getVigenereKey());
                }
            } else {
                List<Callable<DecryptionAttempt>> tasks = new ArrayList<>();
                if ("Caesar".equals(form.getCipher())) {
                    for (int shift = 0; shift < 26; shift++) {
                        int finalShift = shift;
                        tasks.add(() -> {
                            String decrypted = CaesarCipher.decrypt(form.getText(), finalShift);
                            int score = CipherUtils.countCommonWords(decrypted, language);
                            return new DecryptionAttempt(decrypted, String.valueOf(finalShift), score);
                        });
                    }
                } else if ("Vigenère".equals(form.getCipher())) {
                    List<String> keys = new ArrayList<>();
                    if ("rockyou".equals(form.getBreakMethod())) {
                        int n = Math.min(form.getNumPasswords(), CipherUtils.rockyouPasswords.size());
                        if (n <= 0) {
                            model.addAttribute("error", "Number of passwords must be greater than 0.");
                            return "result";
                        }
                        keys = CipherUtils.rockyouPasswords.subList(0, n);
                    } else if ("bruteForce".equals(form.getBreakMethod())) {
                        int length = form.getBruteForceLength();
                        if (length < 1 || length > 3) {
                            model.addAttribute("error", "Brute-force key length must be between 1 and 3.");
                            return "result";
                        }
                        System.out.println("The length of the brute force is: " + length);
                        keys = CipherUtils.generateKeys(length);
                    } else {
                        System.err.println("Breaking method: " + form.getBreakMethod());
                        model.addAttribute("error", "Invalid breaking method selected.");
                        return "result";
                    }
                    // Distribute keys across threads
                    int chunkSize = Math.max(1, keys.size() / cores);
                    for (int i = 0; i < keys.size(); i += chunkSize) {
                        List<String> chunk = keys.subList(i, Math.min(i + chunkSize, keys.size()));
                        tasks.add(() -> {
                            List<DecryptionAttempt> chunkAttempts = new ArrayList<>();
                            for (String key : chunk) {
                                String decrypted = VigenereCipher.decrypt(form.getText(), key);
                                int score = CipherUtils.countCommonWords(decrypted, language);
                                chunkAttempts.add(new DecryptionAttempt(decrypted, key, score));
                            }
                            return chunkAttempts.get(0); // Return first for compatibility, handled in main thread
                        });
                    }
                }
                try {
                    List<Future<DecryptionAttempt>> futures = executor.invokeAll(tasks, 30, TimeUnit.SECONDS);
                    for (Future<DecryptionAttempt> future : futures) {
                        try {
                            DecryptionAttempt attempt = future.get(0, TimeUnit.SECONDS);
                            attempts.add(attempt);
                        } catch (CancellationException | ExecutionException | TimeoutException e) {
                            partialResults = true;
                        }
                    }
                } catch (InterruptedException e) {
                    partialResults = true;
                }
                attempts.sort((a, b) -> b.getScore() - a.getScore());
                attempts = attempts.stream().limit(100).collect(Collectors.toList());
            }
        }

        if (!attempts.isEmpty()) {
            model.addAttribute("attempts", attempts);
            model.addAttribute("partialResults", partialResults);
        } else {
            model.addAttribute("result", result);
        }
        return "result";
    }

    @PreDestroy
    public void shutdownExecutor() {
        executor.shutdown();
    }
}
