package com.example.webdisgn.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StockMovement {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @ManyToOne
    private Product product;

    private int quantityChanged; // es: -3, +10

    @Enumerated(EnumType.STRING)
    private MovementType type; // IN o OUT

    private String reason; // "Ordine", "Aggiornamento Admin", ecc.

    private LocalDateTime timestamp;
}
