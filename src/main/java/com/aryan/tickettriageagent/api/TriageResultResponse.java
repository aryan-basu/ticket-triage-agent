package com.aryan.tickettriageagent.api;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record TriageResultResponse(Long id, Long ticketId, String category, String priority,
                                   String assignedTeam, String summary, String recommendedAction,
                                   BigDecimal confidence, String modelName, LocalDateTime createdAt) { }
