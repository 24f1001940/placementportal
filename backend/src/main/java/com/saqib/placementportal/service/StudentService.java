package com.saqib.placementportal.service;

import com.saqib.placementportal.dto.ApiDtos.PageResponse;
import com.saqib.placementportal.dto.ApiDtos.StudentProfileRequest;
import com.saqib.placementportal.dto.ApiDtos.StudentResponse;
import com.saqib.placementportal.entity.RoleName;
import com.saqib.placementportal.entity.Student;
import com.saqib.placementportal.entity.UserAccount;
import com.saqib.placementportal.exception.ForbiddenActionException;
import com.saqib.placementportal.exception.ResourceNotFoundException;
import com.saqib.placementportal.repository.StudentRepository;
import com.saqib.placementportal.util.ApiMapper;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class StudentService {
    private final StudentRepository studentRepository;
    private final CurrentUserService currentUserService;

    public StudentService(StudentRepository studentRepository, CurrentUserService currentUserService) {
        this.studentRepository = studentRepository;
        this.currentUserService = currentUserService;
    }

    @Transactional(readOnly = true)
    public Student requireCurrentStudent() {
        UserAccount user = currentUserService.user();
        if (user.primaryRole() != RoleName.STUDENT) {
            throw new ForbiddenActionException("Student role is required");
        }
        return studentRepository.findByUserId(user.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Student profile not found"));
    }

    @Transactional(readOnly = true)
    public StudentResponse myProfile() {
        return ApiMapper.student(requireCurrentStudent());
    }

    @Transactional
    public StudentResponse updateMyProfile(StudentProfileRequest request) {
        Student student = requireCurrentStudent();
        student.setPhone(request.phone());
        student.setCollege(request.college());
        student.setBranch(request.branch());
        student.setGraduationYear(request.graduationYear());
        student.setCgpa(request.cgpa());
        student.setSkills(request.skills());
        return ApiMapper.student(studentRepository.save(student));
    }

    @Transactional(readOnly = true)
    public PageResponse<StudentResponse> list(int page, int size) {
        return ApiMapper.page(studentRepository.findAll(PageRequest.of(page, size)), ApiMapper::student);
    }

    @Transactional(readOnly = true)
    public StudentResponse get(Long id) {
        return ApiMapper.student(studentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Student not found")));
    }
}
