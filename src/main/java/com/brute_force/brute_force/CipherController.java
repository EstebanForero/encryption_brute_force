package com.brute_force.brute_force;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;
import java.util.stream.Collectors;

import com.brute_force.brute_force.ciphertool.*;
import jakarta.annotation.PreDestroy;

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

    @PostMapping("/process")
    public String processForm(@ModelAttribute CipherForm form, Model model) {
        String result = "";
        List<DecryptionAttempt> attempts = new ArrayList<>();
        String language = form.getLanguage();

        if ("Encrypt".equals(form.getOperation())) {
            if ("Caesar".equals(form.getCipher())) {
                result = CaesarCipher.encrypt(form.getText(), form.getKey());
            } else if ("Vigenère".equals(form.getCipher())) {
                result = VigenereCipher.encrypt(form.getText(), form.getVigenereKey());
            }
        } else if ("Decrypt".equals(form.getOperation())) {
            if (form.isUseKnownKey()) {
                if ("Caesar".equals(form.getCipher())) {
                    result = CaesarCipher.decrypt(form.getText(), form.getKey());
                } else if ("Vigenère".equals(form.getCipher())) {
                    result = VigenereCipher.decrypt(form.getText(), form.getVigenereKey());
                }
            } else {
                ConcurrentLinkedQueue<DecryptionAttempt> queue = new ConcurrentLinkedQueue<>();
                if ("Caesar".equals(form.getCipher())) {
                    List<Callable<Void>> tasks = new ArrayList<>();
                    for (int shift = 0; shift < 26; shift++) {
                        int finalShift = shift;
                        tasks.add(() -> {
                            String decrypted = CaesarCipher.decrypt(form.getText(), finalShift);
                            int score = CipherUtils.countCommonWords(decrypted, language);
                            queue.add(new DecryptionAttempt(decrypted, String.valueOf(finalShift), score));
                            return null;
                        });
                    }
                    executeTasks(tasks);
                } else if ("Vigenère".equals(form.getCipher())) {
                    if (form.isUseDictionary()) {
                        List<String> keys = CipherUtils.rockyouPasswords;
                        int chunkSize = (int) Math.ceil((double) keys.size() / cores);
                        List<Callable<Void>> tasks = new ArrayList<>();
                        for (int i = 0; i < keys.size(); i += chunkSize) {
                            List<String> chunk = keys.subList(i, Math.min(i + chunkSize, keys.size()));
                            tasks.add(() -> {
                                for (String key : chunk) {
                                    String decrypted = VigenereCipher.decrypt(form.getText(), key);
                                    int score = CipherUtils.countCommonWords(decrypted, language);
                                    queue.add(new DecryptionAttempt(decrypted, key, score));
                                }
                                return null;
                            });
                        }
                        executeTasks(tasks);
                    } else {
                        int keyLength = form.getKeyLength();
                        if (keyLength > 0 && keyLength <= 3) {
                            List<String> possibleKeys = CipherUtils.generateKeys(keyLength);
                            int chunkSize = (int) Math.ceil((double) possibleKeys.size() / cores);
                            List<Callable<Void>> tasks = new ArrayList<>();
                            for (int i = 0; i < possibleKeys.size(); i += chunkSize) {
                                List<String> chunk = possibleKeys.subList(i,
                                        Math.min(i + chunkSize, possibleKeys.size()));
                                tasks.add(() -> {
                                    for (String key : chunk) {
                                        String decrypted = VigenereCipher.decrypt(form.getText(), key);
                                        int score = CipherUtils.countCommonWords(decrypted, language);
                                        queue.add(new DecryptionAttempt(decrypted, key, score));
                                    }
                                    return null;
                                });
                            }
                            executeTasks(tasks);
                        }
                    }
                }
                attempts.addAll(queue);
                attempts.sort((a, b) -> b.getScore() - a.getScore());
                attempts = attempts.stream().limit(100).collect(Collectors.toList());
            }
        }

        if (!attempts.isEmpty()) {
            model.addAttribute("attempts", attempts);
        } else {
            model.addAttribute("result", result);
        }
        return "result";
    }

    private void executeTasks(List<Callable<Void>> tasks) {
        try {
            List<Future<Void>> futures = executor.invokeAll(tasks);
            for (Future<Void> future : futures) {
                future.get(); // Wait for all tasks to complete
            }
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }
    }

    @PreDestroy
    public void shutdownExecutor() {
        executor.shutdown();
    }
}
