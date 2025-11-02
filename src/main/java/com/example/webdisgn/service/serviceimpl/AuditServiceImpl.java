package com.example.webdisgn.service.serviceimpl;

import com.example.webdisgn.model.AuditLog;
import com.example.webdisgn.repository.AuditLogRepository;
import com.example.webdisgn.service.AuditService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class AuditServiceImpl implements AuditService {

    private final AuditLogRepository auditLogRepository;

    @Autowired
    public AuditServiceImpl(AuditLogRepository auditLogRepository) {
        this.auditLogRepository = auditLogRepository;
    }

    @Override
    public void log(String username, String action, String target) {
        AuditLog entity = new AuditLog();
        entity.setUsername(username);
        entity.setAction(action);
        entity.setTarget(target);
        auditLogRepository.save(entity);
        log.debug("Audit log registrato: [{}] action={} target={}", username, action, target);
    }
}
