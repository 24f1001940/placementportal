package com.saqib.placementportal.controller;

import com.saqib.placementportal.dto.ApiDtos.PageResponse;
import com.saqib.placementportal.dto.ApiDtos.StudentProfileRequest;
import com.saqib.placementportal.dto.ApiDtos.StudentResponse;
import com.saqib.placementportal.service.StudentService;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/students")
public class StudentController {
    private final StudentService studentService;

    public StudentController(StudentService studentService) {
        this.studentService = studentService;
    }

    @GetMapping("/me")
    @PreAuthorize("hasRole('STUDENT')")
    public StudentResponse me() {
        return studentService.myProfile();
    }

    @PutMapping("/me")
    @PreAuthorize("hasRole('STUDENT')")
    public StudentResponse updateMe(@Valid @RequestBody StudentProfileRequest request) {
        return studentService.updateMyProfile(request);
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public PageResponse<StudentResponse> list(@RequestParam(defaultValue = "0") int page,
                                              @RequestParam(defaultValue = "20") int size) {
        return studentService.list(page, size);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','COMPANY')")
    public StudentResponse get(@PathVariable Long id) {
        return studentService.get(id);
    }
}
