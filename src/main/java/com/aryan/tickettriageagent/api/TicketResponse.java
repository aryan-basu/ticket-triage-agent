package com.aryan.tickettriageagent.api;

import com.aryan.tickettriageagent.domain.TicketStatus;
import java.time.LocalDateTime;

public record TicketResponse(Long id, String title, String description, String category,
                             String customerImpact, TicketStatus status, LocalDateTime createdAt) { }
