package com.eze.gymanalytics.api.controller;

import com.eze.gymanalytics.api.service.ImportService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/import")
public class ImportController {

    private final ImportService importService;

    public ImportController(ImportService importService) {
        this.importService = importService;
    }

    @PostMapping("/hevy")
    public ResponseEntity<String> importHevy(
        @RequestParam("file") MultipartFile file,
        @RequestParam("email") String email
    ) {
        try {
            importService.importHevyCsv(file, email);
            return ResponseEntity.ok("Importación completada con éxito");
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(
                "Error: " + e.getMessage()
            );
        }
    }
}
