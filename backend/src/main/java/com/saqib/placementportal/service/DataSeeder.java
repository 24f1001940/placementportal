package com.saqib.placementportal.service;

import com.saqib.placementportal.entity.Company;
import com.saqib.placementportal.entity.DriveStatus;
import com.saqib.placementportal.entity.PlacementDrive;
import com.saqib.placementportal.entity.Role;
import com.saqib.placementportal.entity.RoleName;
import com.saqib.placementportal.entity.Student;
import com.saqib.placementportal.entity.UserAccount;
import com.saqib.placementportal.repository.CompanyRepository;
import com.saqib.placementportal.repository.PlacementDriveRepository;
import com.saqib.placementportal.repository.RoleRepository;
import com.saqib.placementportal.repository.StudentRepository;
import com.saqib.placementportal.repository.UserRepository;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Set;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class DataSeeder implements CommandLineRunner {
    private final RoleRepository roleRepository;
    private final UserRepository userRepository;
    private final StudentRepository studentRepository;
    private final CompanyRepository companyRepository;
    private final PlacementDriveRepository driveRepository;
    private final PasswordEncoder passwordEncoder;

    public DataSeeder(
            RoleRepository roleRepository,
            UserRepository userRepository,
            StudentRepository studentRepository,
            CompanyRepository companyRepository,
            PlacementDriveRepository driveRepository,
            PasswordEncoder passwordEncoder
    ) {
        this.roleRepository = roleRepository;
        this.userRepository = userRepository;
        this.studentRepository = studentRepository;
        this.companyRepository = companyRepository;
        this.driveRepository = driveRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional
    public void run(String... args) {
        Role adminRole = role(RoleName.ADMIN);
        Role companyRole = role(RoleName.COMPANY);
        Role studentRole = role(RoleName.STUDENT);

        UserAccount admin = userRepository.findByEmailIgnoreCase("admin@placement.local")
                .orElseGet(() -> {
                    UserAccount user = new UserAccount("Portal Admin", "admin@placement.local", passwordEncoder.encode("Admin@123"));
                    user.setRoles(Set.of(adminRole));
                    return userRepository.save(user);
                });

        UserAccount companyUser = userRepository.findByEmailIgnoreCase("hr@novacore.local")
                .orElseGet(() -> {
                    UserAccount user = new UserAccount("NovaCore HR", "hr@novacore.local", passwordEncoder.encode("Company@123"));
                    user.setRoles(Set.of(companyRole));
                    return userRepository.save(user);
                });
        Company company = companyRepository.findByUserId(companyUser.getId())
                .orElseGet(() -> companyRepository.save(new Company(companyUser, "NovaCore Systems")));
        company.setApproved(true);
        company.setLocation("Bengaluru");
        company.setWebsite("https://novacore.local");
        company.setDescription("Product engineering company hiring full-stack graduates.");

        UserAccount studentUser = userRepository.findByEmailIgnoreCase("student@placement.local")
                .orElseGet(() -> {
                    UserAccount user = new UserAccount("Demo Student", "student@placement.local", passwordEncoder.encode("Student@123"));
                    user.setRoles(Set.of(studentRole));
                    return userRepository.save(user);
                });
        Student student = studentRepository.findByUserId(studentUser.getId())
                .orElseGet(() -> studentRepository.save(new Student(studentUser)));
        student.setCollege("Placement Institute of Technology");
        student.setBranch("Computer Science");
        student.setGraduationYear(2026);
        student.setCgpa(new BigDecimal("8.4"));
        student.setSkills("Java, Spring Boot, React, SQL");

        if (driveRepository.count() == 0) {
            PlacementDrive drive = new PlacementDrive(company);
            drive.setTitle("Java Full Stack Trainee");
            drive.setJobRole("Software Engineer Trainee");
            drive.setLocation("Bengaluru");
            drive.setDescription("Build Spring Boot and React features with mentorship from senior engineers.");
            drive.setMinCgpa(new BigDecimal("7.0"));
            drive.setEligibleBranches("CSE, IT, ECE");
            drive.setAnnualPackage(new BigDecimal("650000"));
            drive.setDeadline(LocalDate.now().plusDays(30));
            drive.setStatus(DriveStatus.APPROVED);
            driveRepository.save(drive);
        }

        admin.setApproved(true);
    }

    private Role role(RoleName name) {
        return roleRepository.findByName(name).orElseGet(() -> roleRepository.save(new Role(name)));
    }
}
