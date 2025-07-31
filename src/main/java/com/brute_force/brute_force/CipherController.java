package com.brute_force.brute_force;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import com.brute_force.brute_force.ciphertool.*;

import java.util.List;

@Controller
public class CipherController {

    private final CipherService cipherService;

    public CipherController(CipherService cipherService) {
        this.cipherService = cipherService;
    }

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
    public String keyOptions(@RequestParam String cipher,
            @RequestParam(defaultValue = "false") boolean useKnownKey,
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
        try {
            validateInput(form);

            if ("Encrypt".equals(form.getOperation()) || form.isUseKnownKey()) {
                String result = processWithKnownKey(form);
                model.addAttribute("result", result);
            } else {
                List<DecryptionAttempt> attempts = processBruteForce(form);
                model.addAttribute("attempts", attempts);
            }

        } catch (IllegalArgumentException e) {
            model.addAttribute("error", e.getMessage());
        }

        return "result";
    }

    private void validateInput(CipherForm form) {
        if (form.getText() == null || form.getText().trim().isEmpty()) {
            throw new IllegalArgumentException("Text input cannot be empty.");
        }

        boolean needsKey = "Encrypt".equals(form.getOperation()) || form.isUseKnownKey();

        if (needsKey) {
            if ("Caesar".equals(form.getCipher())) {
                if (form.getKey() < 0 || form.getKey() > 25) {
                    throw new IllegalArgumentException("Caesar key must be between 0 and 25.");
                }
            } else if ("Vigenère".equals(form.getCipher())) {
                if (form.getVigenereKey() == null || form.getVigenereKey().trim().isEmpty()) {
                    throw new IllegalArgumentException("Vigenère keyword cannot be empty.");
                }
            }
        }

        if ("Decrypt".equals(form.getOperation()) && !form.isUseKnownKey()) {
            if ("Vigenère".equals(form.getCipher())) {
                validateVigenereBruteForceParams(form);
            }
        }
    }

    private void validateVigenereBruteForceParams(CipherForm form) {
        if ("rockyou".equals(form.getBreakMethod())) {
            if (form.getNumPasswords() <= 0) {
                throw new IllegalArgumentException("Number of passwords must be greater than 0.");
            }
        } else if ("bruteForce".equals(form.getBreakMethod())) {
            if (form.getBruteForceLength() < 1 || form.getBruteForceLength() > 5) {
                throw new IllegalArgumentException("Brute-force key length must be between 1 and 5.");
            }
        } else if (form.getBreakMethod() == null || form.getBreakMethod().trim().isEmpty()) {
            throw new IllegalArgumentException("Breaking method must be selected for Vigenère brute force.");
        } else {
            throw new IllegalArgumentException("Invalid breaking method selected: " + form.getBreakMethod());
        }
    }

    private String processWithKnownKey(CipherForm form) {
        String key = "Caesar".equals(form.getCipher()) ? String.valueOf(form.getKey()) : form.getVigenereKey();

        return cipherService.processWithKnownKey(
                form.getCipher(),
                form.getOperation(),
                form.getText(),
                key);
    }

    private List<DecryptionAttempt> processBruteForce(CipherForm form) {
        return cipherService.bruteForceDecrypt(
                form.getCipher(),
                form.getText(),
                form,
                form.getLanguage());
    }
}
