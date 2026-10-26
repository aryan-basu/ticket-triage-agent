package com.aryan.tickettriageagent.service;

import com.aryan.tickettriageagent.api.BatchTriageResponse;
import com.aryan.tickettriageagent.api.TriageAnalysis;
import com.aryan.tickettriageagent.api.TriageResultResponse;
import com.aryan.tickettriageagent.domain.Ticket;
import com.aryan.tickettriageagent.domain.TicketStatus;
import com.aryan.tickettriageagent.domain.TriageResult;
import com.aryan.tickettriageagent.repository.TicketRepository;
import com.aryan.tickettriageagent.repository.TriageResultRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.beans.factory.annotation.Value;

import java.math.BigDecimal;
import java.util.Set;
import java.util.ArrayList;
import org.springframework.data.domain.PageRequest;
import org.springframework.beans.factory.annotation.Value;

@Service @RequiredArgsConstructor
public class TriageService {
    private static final Set<String> CATEGORIES = Set.of("PAYMENT", "AUTHENTICATION", "PERFORMANCE", "BUG", "FEATURE_REQUEST", "INFRASTRUCTURE", "SECURITY", "OTHER");
    private static final Set<String> PRIORITIES = Set.of("P1", "P2", "P3", "P4");
    private final TicketRepository ticketRepository;
    private final TriageResultRepository resultRepository;
    private final TriageClient triageClient;
    @Value("${spring.ai.google.genai.chat.options.model}")
    private String modelName;
    @Value("${app.triage.confidence-review-threshold:0.70}") private BigDecimal reviewThreshold;

    public TriageResultResponse triage(Long ticketId) {
        Ticket ticket = ticketRepository.findById(ticketId).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Ticket not found: " + ticketId));
        TriageAnalysis analysis = triageClient.analyze(ticket);
        validate(analysis);
        analysis = applySafetyRules(ticket, analysis);
        if (analysis.confidence().compareTo(reviewThreshold) < 0) {
            ticket.setStatus(TicketStatus.IN_PROGRESS);
            ticketRepository.save(ticket);
        }
        TriageResult result = new TriageResult(); result.setTicket(ticket); result.setCategory(analysis.category());
        result.setPriority(analysis.priority()); result.setAssignedTeam(analysis.assignedTeam()); result.setSummary(analysis.summary());
        result.setRecommendedAction(analysis.recommendedAction()); result.setConfidence(analysis.confidence());
        result.setModelName(modelName);
        return toResponse(resultRepository.save(result));
    }

    public BatchTriageResponse batch(Integer requestedLimit) {
        int limit = Math.min(requestedLimit == null ? 10 : requestedLimit, 50);
        var candidates = ticketRepository.findByStatusOrderByCreatedAtAsc(TicketStatus.OPEN, PageRequest.of(0, limit));
        var succeeded = new ArrayList<Long>();
        var failures = new ArrayList<BatchTriageResponse.BatchFailure>();
        for (Ticket ticket : candidates) {
            if (resultRepository.findFirstByTicketIdOrderByCreatedAtDesc(ticket.getId()).isPresent()) continue;
            try {
                triage(ticket.getId());
                succeeded.add(ticket.getId());
            } catch (Exception exception) {
                String message = exception.getMessage() == null ? "Triage failed" : exception.getMessage();
                failures.add(new BatchTriageResponse.BatchFailure(ticket.getId(), message));
            }
        }
        return new BatchTriageResponse(limit, succeeded.size() + failures.size(), succeeded.size(), failures.size(), succeeded, failures);
    }

    private void validate(TriageAnalysis a) {
        if (a == null || !CATEGORIES.contains(a.category()) || !PRIORITIES.contains(a.priority()) || blank(a.assignedTeam())
                || blank(a.summary()) || blank(a.recommendedAction()) || a.confidence() == null
                || a.confidence().compareTo(BigDecimal.ZERO) < 0 || a.confidence().compareTo(BigDecimal.ONE) > 0)
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "OpenAI returned invalid triage output");
    }
    private TriageAnalysis applySafetyRules(Ticket ticket, TriageAnalysis analysis) {
        String text = (ticket.getTitle() + " " + ticket.getDescription()).toLowerCase();
        if (text.contains("data breach") || text.contains("hacked") || text.contains("stolen credentials")) {
            return new TriageAnalysis("SECURITY", "P1", analysis.assignedTeam(), analysis.summary(), analysis.recommendedAction(), analysis.confidence());
        }
        return analysis;
    }
    private boolean blank(String value) { return value == null || value.isBlank(); }
    private TriageResultResponse toResponse(TriageResult r) { return new TriageResultResponse(r.getId(), r.getTicket().getId(), r.getCategory(), r.getPriority(), r.getAssignedTeam(), r.getSummary(), r.getRecommendedAction(), r.getConfidence(), r.getModelName(), r.getCreatedAt()); }
}
