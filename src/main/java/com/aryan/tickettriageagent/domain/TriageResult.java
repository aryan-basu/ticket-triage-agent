package com.aryan.tickettriageagent.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "ticket_triage_results")
@Getter @Setter @NoArgsConstructor
public class TriageResult {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @ManyToOne(fetch = FetchType.LAZY, optional = false) @JoinColumn(name = "ticket_id", nullable = false) private Ticket ticket;
    @Column(nullable = false, length = 50) private String category;
    @Column(nullable = false, length = 10) private String priority;
    @Column(name = "assigned_team", nullable = false, length = 100) private String assignedTeam;
    @Lob private String summary;
    @Lob @Column(name = "recommended_action") private String recommendedAction;
    @Column(precision = 4, scale = 3) private BigDecimal confidence;
    @Column(name = "model_name", length = 100) private String modelName;
    @CreationTimestamp @Column(name = "created_at", updatable = false) private LocalDateTime createdAt;
}
