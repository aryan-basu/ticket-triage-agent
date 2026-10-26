package com.aryan.tickettriageagent.service;

import com.aryan.tickettriageagent.api.TriageAnalysis;
import com.aryan.tickettriageagent.domain.Ticket;

public interface TriageClient {
    TriageAnalysis analyze(Ticket ticket);
}
