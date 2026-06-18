package com.saqib.placementportal.controller;

import com.saqib.placementportal.dto.ApiDtos.ResumeResponse;
import com.saqib.placementportal.service.ResumeStorageService;
import com.saqib.placementportal.service.ResumeStorageService.ResumeFile;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/resumes")
public class ResumeController {
    private final ResumeStorageService resumeStorageService;

    public ResumeController(ResumeStorageService resumeStorageService) {
        this.resumeStorageService = resumeStorageService;
    }

    @PostMapping
    @PreAuthorize("hasRole('STUDENT')")
    public ResumeResponse upload(@RequestParam("file") MultipartFile file) {
        return resumeStorageService.upload(file);
    }

    @GetMapping("/me/download")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<?> downloadMine() {
        return fileResponse(resumeStorageService.downloadMine());
    }

    @GetMapping("/student/{studentId}/download")
    @PreAuthorize("hasAnyRole('ADMIN','COMPANY')")
    public ResponseEntity<?> downloadByStudent(@PathVariable Long studentId) {
        return fileResponse(resumeStorageService.downloadForStudent(studentId));
    }

    private ResponseEntity<?> fileResponse(ResumeFile file) {
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(file.contentType()))
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        ContentDisposition.inline().filename(file.filename()).build().toString())
                .body(file.resource());
    }
}
