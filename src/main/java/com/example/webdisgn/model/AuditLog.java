package com.example.webdisgn.model;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Entity
@Data
public class AuditLog {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    private String username;
    private String action;
    private String target;
    private LocalDateTime timestamp = LocalDateTime.now();
}
