package com.example.webdisgn.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;

@Data
@Entity
@Table(name = "invalidated_tokens")
public class InvalidatedToken {

    @Id
    private String token;
}
