package com.example.webdisgn.service.serviceimpl;

import com.example.webdisgn.repository.AuditLogRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.mockito.Mockito.*;

class AuditServiceImplTest {

    @Mock
    private AuditLogRepository auditLogRepository;

    @InjectMocks
    private AuditServiceImpl auditService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void log_shouldSaveAuditEntry() {
        // Given
        String username = "admin";
        String action = "DELETE_USER";
        String target = "ID=23";

        // When
        auditService.log(username, action, target);

        // Then
        verify(auditLogRepository, times(1)).save(argThat(log ->
                log.getUsername().equals("admin") &&
                        log.getAction().equals("DELETE_USER") &&
                        log.getTarget().equals("ID=23")
        ));
    }
}
