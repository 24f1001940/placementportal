package com.saqib.placementportal.service;

import com.saqib.placementportal.dto.ApiDtos.AuthResponse;
import com.saqib.placementportal.dto.ApiDtos.LoginRequest;
import com.saqib.placementportal.dto.ApiDtos.RegisterRequest;
import com.saqib.placementportal.entity.Company;
import com.saqib.placementportal.entity.Role;
import com.saqib.placementportal.entity.RoleName;
import com.saqib.placementportal.entity.Student;
import com.saqib.placementportal.entity.UserAccount;
import com.saqib.placementportal.exception.ConflictException;
import com.saqib.placementportal.exception.ForbiddenActionException;
import com.saqib.placementportal.exception.ResourceNotFoundException;
import com.saqib.placementportal.repository.CompanyRepository;
import com.saqib.placementportal.repository.RoleRepository;
import com.saqib.placementportal.repository.StudentRepository;
import com.saqib.placementportal.repository.UserRepository;
import com.saqib.placementportal.security.JwtService;
import com.saqib.placementportal.util.ApiMapper;
import java.util.Set;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final StudentRepository studentRepository;
    private final CompanyRepository companyRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final CurrentUserService currentUserService;

    public AuthService(
            UserRepository userRepository,
            RoleRepository roleRepository,
            StudentRepository studentRepository,
            CompanyRepository companyRepository,
            PasswordEncoder passwordEncoder,
            JwtService jwtService,
            CurrentUserService currentUserService
    ) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.studentRepository = studentRepository;
        this.companyRepository = companyRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.currentUserService = currentUserService;
    }

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        if (request.role() == RoleName.ADMIN) {
            throw new ForbiddenActionException("Admin users are created by the system owner");
        }
        if (userRepository.existsByEmailIgnoreCase(request.email())) {
            throw new ConflictException("Email already registered");
        }
        Role role = roleRepository.findByName(request.role())
                .orElseThrow(() -> new ResourceNotFoundException("Role not configured"));
        UserAccount user = new UserAccount(
                request.fullName().trim(),
                request.email().trim().toLowerCase(),
                passwordEncoder.encode(request.password())
        );
        user.setRoles(Set.of(role));
        UserAccount saved = userRepository.save(user);

        if (request.role() == RoleName.STUDENT) {
            studentRepository.save(new Student(saved));
        }
        if (request.role() == RoleName.COMPANY) {
            companyRepository.save(new Company(saved, request.fullName().trim()));
        }
        return new AuthResponse(jwtService.generate(saved), ApiMapper.user(saved));
    }

    @Transactional(readOnly = true)
    public AuthResponse login(LoginRequest request) {
        UserAccount user = userRepository.findByEmailIgnoreCase(request.email())
                .orElseThrow(() -> new BadCredentialsException("Invalid email or password"));
        if (!passwordEncoder.matches(request.password(), user.getPasswordHash())) {
            throw new BadCredentialsException("Invalid email or password");
        }
        if (user.isBlacklisted()) {
            throw new ForbiddenActionException("This account is blacklisted");
        }
        return new AuthResponse(jwtService.generate(user), ApiMapper.user(user));
    }

    @Transactional(readOnly = true)
    public AuthResponse me() {
        UserAccount user = currentUserService.user();
        return new AuthResponse(null, ApiMapper.user(user));
    }
}
