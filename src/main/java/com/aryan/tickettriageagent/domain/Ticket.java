package com.aryan.tickettriageagent.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "tickets")
@Getter @Setter @NoArgsConstructor
public class Ticket {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false, length = 255) private String title;
    @Lob @Column(nullable = false) private String description;
    @Column(length = 50) private String category;
    @Column(name = "customer_impact", length = 50) private String customerImpact;
    @Enumerated(EnumType.STRING) @Column(nullable = false, length = 30) private TicketStatus status = TicketStatus.OPEN;
    @CreationTimestamp @Column(name = "created_at", updatable = false) private LocalDateTime createdAt;
}
