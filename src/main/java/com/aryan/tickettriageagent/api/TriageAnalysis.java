package com.aryan.tickettriageagent.api;

import java.math.BigDecimal;

public record TriageAnalysis(String category, String priority, String assignedTeam,
                             String summary, String recommendedAction, BigDecimal confidence) { }
