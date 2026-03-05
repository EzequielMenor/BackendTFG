package com.eze.gymanalytics.api.controller;

import com.eze.gymanalytics.api.dto.ImportResultDTO;
import com.eze.gymanalytics.api.service.ImportService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
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
    public ResponseEntity<ImportResultDTO> importHevy(
        @RequestParam("file") MultipartFile file,
        @AuthenticationPrincipal String email
    ) {
        try {
            ImportResultDTO result = importService.importHevyCsv(file, email);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }
}
