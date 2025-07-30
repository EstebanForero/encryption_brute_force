package com.brute_force.brute_force;

import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import java.io.BufferedReader;

@Controller
public class FileController {
    @GetMapping
    public String showUploadForm(Model model) {
        model.addAttribute("message", "Please select a .txt file to upload");
        return "index";
    }

    @PostMapping("/upload")
    public String handleFileUpload(@RequestParam("file") MultipartFile file, Model model) {
        List<String> lines = new ArrayList<>();

        try {
            if (!file.isEmpty() && file.getOriginalFilename().endsWith(".txt")) {
                BufferedReader reader = new BufferedReader(new InputStreamReader(file.getInputStream()));
                String line;

                while ((line = reader.readLine()) != null) {
                    lines.add(line);
                }
                reader.close();
                model.addAttribute("lines", lines);
                model.addAttribute("message", "File processed successfully");
            } else {
                model.addAttribute("message", "Please upload a valid txt file");
            }
        } catch (Exception e) {
            model.addAttribute("message", "Error processing file: " + e.getMessage());
        }

        return "result";
    }
}
