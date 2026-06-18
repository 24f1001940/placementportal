package com.saqib.placementportal.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.saqib.placementportal.dto.ApiDtos.LoginRequest;
import com.saqib.placementportal.dto.ApiDtos.RegisterRequest;
import com.saqib.placementportal.entity.RoleName;
import com.saqib.placementportal.exception.ConflictException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@ActiveProfiles("test")
@SpringBootTest
class AuthServiceTest {
    @Autowired
    private AuthService authService;

    @Test
    void registersAndLogsInStudent() {
        authService.register(new RegisterRequest("Test Student", "newstudent@example.com", "secret123", RoleName.STUDENT));

        var response = authService.login(new LoginRequest("newstudent@example.com", "secret123"));

        assertThat(response.token()).isNotBlank();
        assertThat(response.user().role()).isEqualTo(RoleName.STUDENT);
    }

    @Test
    void rejectsDuplicateEmail() {
        authService.register(new RegisterRequest("Duplicate Student", "duplicate@example.com", "secret123", RoleName.STUDENT));

        assertThatThrownBy(() -> authService.register(new RegisterRequest("Duplicate Student", "duplicate@example.com", "secret123", RoleName.STUDENT)))
                .isInstanceOf(ConflictException.class);
    }
}
