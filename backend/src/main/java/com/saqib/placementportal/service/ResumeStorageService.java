package com.saqib.placementportal.service;

import com.saqib.placementportal.dto.ApiDtos.ResumeResponse;
import com.saqib.placementportal.entity.ResumeDocument;
import com.saqib.placementportal.entity.RoleName;
import com.saqib.placementportal.entity.Student;
import com.saqib.placementportal.entity.UserAccount;
import com.saqib.placementportal.exception.ForbiddenActionException;
import com.saqib.placementportal.exception.ResourceNotFoundException;
import com.saqib.placementportal.repository.ResumeRepository;
import com.saqib.placementportal.repository.StudentRepository;
import com.saqib.placementportal.util.ApiMapper;
import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import com.saqib.placementportal.exception.ApiException;

@Service
public class ResumeStorageService {
    private final ResumeRepository resumeRepository;
    private final StudentRepository studentRepository;
    private final StudentService studentService;
    private final CurrentUserService currentUserService;
    private final Path uploadRoot;

    public ResumeStorageService(
            ResumeRepository resumeRepository,
            StudentRepository studentRepository,
            StudentService studentService,
            CurrentUserService currentUserService,
            @Value("${app.upload-dir}") String uploadDir
    ) {
        this.resumeRepository = resumeRepository;
        this.studentRepository = studentRepository;
        this.studentService = studentService;
        this.currentUserService = currentUserService;
        this.uploadRoot = Path.of(uploadDir).toAbsolutePath().normalize();
    }

    @Transactional
    public ResumeResponse upload(MultipartFile file) {
        Student student = studentService.requireCurrentStudent();
        if (file.isEmpty()) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Resume file is required");
        }
        String original = file.getOriginalFilename() == null ? "resume.pdf" : file.getOriginalFilename();
        if (!original.toLowerCase().endsWith(".pdf")) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Only PDF resumes are allowed");
        }
        try {
            Files.createDirectories(uploadRoot);
            resumeRepository.findByStudentId(student.getId()).ifPresent(existing -> {
                try {
                    Files.deleteIfExists(Path.of(existing.getStoragePath()));
                } catch (IOException ignored) {
                    // Old file cleanup failure should not block replacing the resume.
                }
                resumeRepository.delete(existing);
            });
            String safeName = original.replaceAll("[^a-zA-Z0-9._-]", "_");
            Path target = uploadRoot.resolve(student.getId() + "-" + UUID.randomUUID() + "-" + safeName).normalize();
            Files.copy(file.getInputStream(), target, StandardCopyOption.REPLACE_EXISTING);
            ResumeDocument resume = resumeRepository.save(new ResumeDocument(
                    student,
                    original,
                    target.toString(),
                    file.getContentType() == null ? "application/pdf" : file.getContentType(),
                    file.getSize()
            ));
            return ApiMapper.resume(resume);
        } catch (IOException ex) {
            throw new ApiException(HttpStatus.INTERNAL_SERVER_ERROR, "Could not store resume");
        }
    }

    @Transactional(readOnly = true)
    public ResumeFile downloadForStudent(Long studentId) {
        UserAccount user = currentUserService.user();
        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new ResourceNotFoundException("Student not found"));
        boolean owner = user.primaryRole() == RoleName.STUDENT && student.getUser().getId().equals(user.getId());
        boolean recruiter = user.primaryRole() == RoleName.COMPANY || user.primaryRole() == RoleName.ADMIN;
        if (!owner && !recruiter) {
            throw new ForbiddenActionException("Not allowed to download this resume");
        }
        ResumeDocument resume = resumeRepository.findByStudentId(studentId)
                .orElseThrow(() -> new ResourceNotFoundException("Resume not uploaded"));
        try {
            Resource resource = new UrlResource(Path.of(resume.getStoragePath()).toUri());
            if (!resource.exists()) {
                throw new ResourceNotFoundException("Resume file missing from disk");
            }
            return new ResumeFile(resource, resume.getOriginalFileName(), resume.getContentType());
        } catch (MalformedURLException ex) {
            throw new ApiException(HttpStatus.INTERNAL_SERVER_ERROR, "Could not load resume");
        }
    }

    @Transactional(readOnly = true)
    public ResumeFile downloadMine() {
        Student student = studentService.requireCurrentStudent();
        return downloadForStudent(student.getId());
    }

    public record ResumeFile(Resource resource, String filename, String contentType) {
    }
}
